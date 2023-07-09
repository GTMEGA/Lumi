/*
 * Copyright (c) 2023 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.engine;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.collection.PooledLongQueue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;

import java.util.concurrent.locks.ReentrantLock;

import static com.falsepattern.lumina.internal.engine.LightingHooks.getLoadedChunk;


public final class PhosphorLightingEngine implements LumiLightingEngine {
    private static final boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS = true;

    private static final int MIN_LIGHT_VALUE = 0;
    private static final int MAX_LIGHT_VALUE = 15;

    private static final int MIN_BLOCK_OPACITY = 1;
    private static final int MAX_BLOCK_OPACITY = 15;

    private static final int MAX_SCHEDULED_COUNT = 1 << 22;

    private static final int POS_Z_BIT_SIZE = 26;
    private static final int POS_X_BIT_SIZE = 8;
    private static final int POS_Y_BIT_SIZE = 26;
    private static final int LIGHT_VALUE_BIT_SIZE = 4;

    private static final int POS_Z_BIT_SHIFT = 0;
    private static final int POS_X_BIT_SHIFT = POS_Z_BIT_SHIFT + POS_Y_BIT_SIZE;
    private static final int POS_Y_BIT_SHIFT = POS_X_BIT_SHIFT + POS_Z_BIT_SIZE;
    private static final int LIGHT_VALUE_BIT_SHIFT = POS_Y_BIT_SHIFT + POS_X_BIT_SIZE;

    private static final long Y_POS_OVERFLOW_CHECK_BIT_MASK = 1L << (POS_Y_BIT_SHIFT + POS_X_BIT_SIZE);

    private static final long POS_X_BIT_MASK = (1L << POS_Z_BIT_SIZE) - 1;
    private static final long POS_Y_BIT_MASK = (1L << POS_X_BIT_SIZE) - 1;
    private static final long POS_Z_BIT_MASK = (1L << POS_Y_BIT_SIZE) - 1;
    private static final long LIGHT_VALUE_BIT_MASK = (1L << LIGHT_VALUE_BIT_SIZE) - 1;

    private static final long BLOCK_POS_MASK = (POS_Y_BIT_MASK << POS_Y_BIT_SHIFT) |
                                               (POS_X_BIT_MASK << POS_X_BIT_SHIFT) |
                                               (POS_Z_BIT_MASK << POS_Z_BIT_SHIFT);
    private static final long CHUNK_POS_MASK = ((POS_X_BIT_MASK >> 4) << (4 + POS_X_BIT_SHIFT)) |
                                               ((POS_Z_BIT_MASK >> 4) << (4 + POS_Z_BIT_SHIFT));

    private static final long[] BLOCK_SIDE_BIT_OFFSET;

    static {
        BLOCK_SIDE_BIT_OFFSET = new long[6];

        val sides = EnumFacing.values();
        val sideCount = sides.length;
        for (var i = 0; i < sideCount; i++) {
            val side = sides[i];
            val offsetX = (long) side.getFrontOffsetX();
            val offsetY = (long) side.getFrontOffsetY();
            val offsetZ = (long) side.getFrontOffsetZ();

            BLOCK_SIDE_BIT_OFFSET[i] = (offsetX << POS_X_BIT_SHIFT) |
                                       (offsetY << POS_Y_BIT_SHIFT) |
                                       (offsetZ << POS_Z_BIT_SHIFT);
        }
    }

    private final Thread updateThread = Thread.currentThread();
    private final ReentrantLock lock = new ReentrantLock();

    private final PooledLongQueue.Pool queuePool = PooledLongQueue.createPool();
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] updateQueues = new PooledLongQueue[EnumSkyBlock.values().length];
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] brighteningQueues = new PooledLongQueue[MAX_LIGHT_VALUE + 1];
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] darkeningQueues = new PooledLongQueue[MAX_LIGHT_VALUE + 1];
    /**
     * Layout of longs: [newLight(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue initialBrighteningQueue = queuePool.createQueue();
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue initialDarkeningQueue = queuePool.createQueue();

    private PooledLongQueue.LongQueueIterator queueIterator = null;

    private final BlockPos.MutableBlockPos cursorBlockPos = new BlockPos.MutableBlockPos();
    private LumiChunk cursorChunk;
    private long cursorChunkPosLong;
    private long cursorData;

    private final NeighborBlock[] neighborBlocks = new NeighborBlock[6];
    private boolean areNeighboursBlocksValid = false;

    private boolean isUpdating = false;

    private final LumiWorld world;
    private final Profiler profiler;

    public PhosphorLightingEngine(LumiWorld world) {
        this.world = world;
        this.profiler = world.lumi$root().lumi$profiler();

        for (var i = 0; i < updateQueues.length; i++)
            updateQueues[i] = queuePool.createQueue();
        for (var i = 0; i < brighteningQueues.length; i++)
            brighteningQueues[i] = queuePool.createQueue();
        for (var i = 0; i < darkeningQueues.length; i++)
            darkeningQueues[i] = queuePool.createQueue();

        for (var i = 0; i < neighborBlocks.length; i++)
            neighborBlocks[i] = new NeighborBlock();
    }

    @Override
    public void scheduleLightUpdate(EnumSkyBlock lightType, int posX, int posY, int posZ) {
        acquireLock();

        try {
            scheduleLightUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void processLightUpdate() {
        processLightUpdate(EnumSkyBlock.Sky);
        processLightUpdate(EnumSkyBlock.Block);
    }

    @Override
    public void processLightUpdate(EnumSkyBlock lightType) {
        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (world.lumi$root().lumi$isClientSide() && !isCallingFromMainThread()) {
            return;
        }

        // Quickly check if the queue is empty before we acquire a more expensive lock.
        val queue = updateQueues[lightType.ordinal()];
        if (queue.isEmpty())
            return;

        acquireLock();

        try {
            processLightUpdateQueue(lightType, queue);
        } finally {
            releaseLock();
        }
    }

    private void scheduleLightUpdate(EnumSkyBlock lightType, long posLong) {
        val queue = updateQueues[lightType.ordinal()];
        queue.add(posLong);

        //make sure there are not too many queued light updates
        if (queue.size() >= MAX_SCHEDULED_COUNT)
            processLightUpdate(lightType);
    }

    @SideOnly(Side.CLIENT)
    private boolean isCallingFromMainThread() {
        return Minecraft.getMinecraft().func_152345_ab();
    }

    private void acquireLock() {
        if (!lock.tryLock()) {
            // If we cannot lock, something has gone wrong... Only one thread should ever acquire the lock.
            // Validate that we're on the right thread immediately so we can gather information.
            // It is NEVER valid to call World methods from a thread other than the owning thread of the world instance.
            // Users can safely disable this warning, however it will not resolve the issue.
            if (ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS) {
                final Thread current = Thread.currentThread();

                if (current != updateThread) {
                    final IllegalAccessException e = new IllegalAccessException(String.format("World is owned by '%s' (ID: %s)," +
                                                                                              " but was accessed from thread '%s' (ID: %s)",
                                                                                              updateThread.getName(), updateThread.getId(), current.getName(), current.getId()));

                    Share.LOG.warn(
                            "Something (likely another mod) has attempted to modify the world's state from the wrong thread!\n" +
                            "This is *bad practice* and can cause severe issues in your game. Phosphor has done as best as it can to mitigate this violation," +
                            " but it may negatively impact performance or introduce stalls.\nIn a future release, this violation may result in a hard crash instead" +
                            " of the current soft warning. You should report this issue to our issue tracker with the following stacktrace information.\n(If you are" +
                            " aware you have misbehaving mods and cannot resolve this issue, you can safely disable this warning by setting" +
                            " `enable_illegal_thread_access_warnings` to `false` in Phosphor's configuration file for the time being.)", e);

                }

            }

            // Wait for the lock to be released. This will likely introduce unwanted stalls, but will mitigate the issue.
            lock.lock();
        }
    }

    private void releaseLock() {
        lock.unlock();
    }

    private void processLightUpdateQueue(final EnumSkyBlock lightType, final PooledLongQueue queue) {
        if (isUpdating)
            return;
        isUpdating = true;

        // Reset chunk cache
        cursorChunkPosLong = -1;

        profiler.startSection("lighting");
        profiler.startSection("checking");

        // Process the queued updates and enqueue them for further processing
        queueIterator = queue.iterator();
        while (nextItem()) {
            if (cursorChunk == null)
                continue;

            val cursorCurrentLightValue = getCursorCurrentLightValue(lightType);
            val cursorUpdatedLightValue = getCursorUpdatedLightValue(lightType);
            if (cursorCurrentLightValue < cursorUpdatedLightValue) {
                // Don't enqueue directly for brightening in order to avoid duplicate scheduling
                val newData = ((long) cursorUpdatedLightValue << LIGHT_VALUE_BIT_SHIFT) | cursorData;
                initialBrighteningQueue.add(newData);
            } else if (cursorCurrentLightValue > cursorUpdatedLightValue) {
                // Don't enqueue directly for darkening in order to avoid duplicate scheduling
                initialDarkeningQueue.add(cursorData);
            }
        }

        queueIterator = initialBrighteningQueue.iterator();
        while (nextItem()) {
            // Sets the light to newLight to only schedule once. Clear leading bits of curData for later
            val cursorDataLightValue = (int) (cursorData >> LIGHT_VALUE_BIT_SHIFT & LIGHT_VALUE_BIT_MASK);
            if (cursorDataLightValue > getCursorCurrentLightValue(lightType)) {
                val posLong = cursorData & BLOCK_POS_MASK;
                enqueueBrightening(cursorBlockPos, posLong, cursorDataLightValue, cursorChunk, lightType);
            }
        }

        queueIterator = initialDarkeningQueue.iterator();
        while (nextItem()) {
            // Sets the light to 0 to only schedule once
            val cursorCurrentLightValue = getCursorCurrentLightValue(lightType);
            if (cursorCurrentLightValue != 0)
                enqueueDarkening(cursorBlockPos, cursorData, cursorCurrentLightValue, cursorChunk, lightType);
        }

        profiler.endSection();
        val rootWorld = world.lumi$root();

        // Iterate through enqueued updates (brightening and darkening in parallel)
        // from brightest to darkest so that we only need to iterate once
        for (var queueIndex = MAX_LIGHT_VALUE; queueIndex >= 0; queueIndex--) {
            profiler.startSection("darkening");

            queueIterator = brighteningQueues[queueIndex].iterator();
            while (nextItem()) {
                // Don't darken if we got brighter due to some other change
                if (getCursorCurrentLightValue(lightType) >= queueIndex)
                    continue;

                val cursorBlock = getBlockFromChunk(cursorChunk, cursorBlockPos);
                val cursorBlockMeta = getBlockMetaFromChunk(cursorChunk, cursorBlockPos);
                val cursorBlockLightValue = getCursorBlockLightValue(cursorBlock, cursorBlockMeta, lightType);

                // If luminosity is high enough, opacity is irrelevant
                final int cursorBlockOpacity;
                if (cursorBlockLightValue >= MAX_LIGHT_VALUE - 1) {
                    cursorBlockOpacity = 1;
                } else {
                    cursorBlockOpacity = getBlockOpacity(cursorBlockPos, cursorBlock, cursorBlockMeta);
                }

                // Only darken neighbors if we indeed became darker
                // If we didn't become darker, so we need to re-set our initial light value (was set to 0) and notify neighbors
                if (getCursorUpdatedLightValue(cursorBlockLightValue, cursorBlockOpacity, lightType) >= queueIndex) {
                    // Do not spread to neighbors immediately to avoid scheduling multiple times
                    enqueueBrighteningFromCursor(queueIndex, lightType);
                    continue;
                }

                // Need to calculate new light value from neighbors IGNORING neighbors which are scheduled for darkening
                var newLightValue = cursorBlockLightValue;
                updateNeighborBlocks(lightType);
                for (val neighbor : neighborBlocks) {
                    if (neighbor.chunk == null)
                        continue;
                    if (neighbor.lightValue == 0)
                        continue;

                    val neighborBlock = getBlockFromSubChunk(neighbor.subChunk, neighbor.blockPos);
                    val neighborBlockMeta = getBlockMetaFromSubChunk(neighbor.subChunk, neighbor.blockPos);
                    val neighborBlockOpacity = getBlockOpacity(neighbor.blockPos, neighborBlock, neighborBlockMeta);

                    // If we can't darken the neighbor, no one else can (because of processing order) -> safe to let us be illuminated by it
                    if (queueIndex - neighborBlockOpacity >= neighbor.lightValue) {
                        // Schedule neighbor for darkening if we possibly light it
                        enqueueDarkening(neighbor.blockPos, neighbor.posLong, neighbor.lightValue, neighbor.chunk, lightType);
                    } else {
                        // Only use for new light calculation if not
                        newLightValue = Math.max(newLightValue, neighbor.lightValue - cursorBlockOpacity);
                    }
                }

                // Schedule brightening since light level was set to 0
                enqueueBrighteningFromCursor(newLightValue, lightType);
            }

            profiler.endStartSection("brightening");
            queueIterator = darkeningQueues[queueIndex].iterator();
            while (nextItem()) {
                // Only process this if nothing else has happened at this position since scheduling
                if (getCursorCurrentLightValue(lightType) == queueIndex) {
                    val posX = cursorBlockPos.getX();
                    val posY = cursorBlockPos.getY();
                    val posZ = cursorBlockPos.getZ();
                    rootWorld.lumi$markBlockForRenderUpdate(posX, posY, posZ);
                    if (queueIndex > 1)
                        spreadLightFromCursor(queueIndex, lightType);
                }
            }
            profiler.endSection();
        }

        profiler.endSection();
        isUpdating = false;
    }

    private void updateNeighborBlocks(EnumSkyBlock lightType) {
        if (areNeighboursBlocksValid)
            return;
        areNeighboursBlocksValid = true;

        for (var i = 0; i < neighborBlocks.length; ++i) {
            val neighbor = neighborBlocks[i];

            neighbor.posLong = cursorData + BLOCK_SIDE_BIT_OFFSET[i];
            if ((neighbor.posLong & Y_POS_OVERFLOW_CHECK_BIT_MASK) != 0) {
                neighbor.chunk = null;
                neighbor.subChunk = null;
                continue;
            }

            blockPosFromPosLong(neighbor.blockPos, neighbor.posLong);
            if ((neighbor.posLong & CHUNK_POS_MASK) == cursorChunkPosLong) {
                neighbor.chunk = cursorChunk;
            } else {
                neighbor.chunk = getChunk(neighbor.blockPos);
            }

            if (neighbor.chunk == null)
                continue;

            val chunkPosY = neighbor.blockPos.getY() / 16;
            neighbor.subChunk = neighbor.chunk.lumi$subChunk(chunkPosY);
            neighbor.lightValue = getCachedLightFor(neighbor.chunk, neighbor.subChunk, lightType, neighbor.blockPos);
        }
    }

    private static int getCachedLightFor(LumiChunk chunk,
                                         LumiSubChunk subChunk,
                                         EnumSkyBlock lightType,
                                         BlockPos blockPos) {
        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;

        if (subChunk != null) {
            val subChunkPosY = posY & 15;
            return subChunk.lumi$getLightValue(lightType, subChunkPosX, subChunkPosY, subChunkPosZ);
        }
        return chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    private int getCursorUpdatedLightValue(EnumSkyBlock lightType) {
        val block = getBlockFromChunk(cursorChunk, cursorBlockPos);
        val blockMeta = getBlockMetaFromChunk(cursorChunk, cursorBlockPos);

        val cursorBlockLightValue = getCursorBlockLightValue(block, blockMeta, lightType);
        final int cursorBlockOpacity;
        if (cursorBlockLightValue >= MAX_LIGHT_VALUE - 1) {
            cursorBlockOpacity = 1;
        } else {
            cursorBlockOpacity = getBlockOpacity(cursorBlockPos, block, blockMeta);
        }

        return getCursorUpdatedLightValue(cursorBlockLightValue, cursorBlockOpacity, lightType);
    }

    private int getCursorUpdatedLightValue(int cursorBlockLightValue, int cursorBlockOpacity, EnumSkyBlock lightType) {
        if (cursorBlockLightValue >= MAX_LIGHT_VALUE - cursorBlockOpacity)
            return cursorBlockLightValue;

        updateNeighborBlocks(lightType);
        var newCursorLightValue = cursorBlockLightValue;
        for (val neighborBlock : neighborBlocks) {
            if (neighborBlock.chunk == null)
                continue;
            val providedLightValue = neighborBlock.lightValue - cursorBlockOpacity;
            newCursorLightValue = Math.max(providedLightValue, newCursorLightValue);
        }
        return newCursorLightValue;
    }

    private void spreadLightFromCursor(int cursorLightValue, EnumSkyBlock lightType) {
        updateNeighborBlocks(lightType);

        for (val neighbor : neighborBlocks) {
            if (neighbor.chunk == null)
                continue;

            val block = getBlockFromSubChunk(neighbor.subChunk, neighbor.blockPos);
            val blockMeta = getBlockMetaFromSubChunk(neighbor.subChunk, neighbor.blockPos);
            val blockOpacity = getBlockOpacity(neighbor.blockPos, block, blockMeta);

            val newLightValue = cursorLightValue - blockOpacity;
            if (newLightValue > neighbor.lightValue)
                enqueueBrightening(neighbor.blockPos, neighbor.posLong, newLightValue, neighbor.chunk, lightType);
        }
    }

    private void enqueueBrighteningFromCursor(int lightValue, EnumSkyBlock lightType) {
        enqueueBrightening(cursorBlockPos, cursorData, lightValue, cursorChunk, lightType);
    }

    private void enqueueBrightening(BlockPos blockPos,
                                    long posLong,
                                    int lightValue,
                                    LumiChunk chunk,
                                    EnumSkyBlock lightType) {
        darkeningQueues[lightValue].add(posLong);

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, lightValue);
    }

    private void enqueueDarkening(BlockPos blockPos,
                                  long posLong,
                                  int oldLightValue,
                                  LumiChunk chunk,
                                  EnumSkyBlock lightType) {
        brighteningQueues[oldLightValue].add(posLong);

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, MIN_LIGHT_VALUE);
    }

    private static BlockPos.MutableBlockPos blockPosFromPosLong(BlockPos.MutableBlockPos blockPos, long longPos) {
        val posX = (int) (longPos >> POS_X_BIT_SHIFT & POS_X_BIT_MASK) - (1 << POS_X_BIT_SIZE - 1);
        val posY = (int) (longPos >> POS_Y_BIT_SHIFT & POS_Y_BIT_MASK);
        val posZ = (int) (longPos >> POS_Z_BIT_SHIFT & POS_Z_BIT_MASK) - (1 << POS_Z_BIT_SIZE - 1);
        return blockPos.setPos(posX, posY, posZ);
    }

    private static long posLongFromPosXYZ(long posX, long posY, long posZ) {
        return (posY << POS_Y_BIT_SHIFT) |
               (posX + (1 << POS_X_BIT_SIZE - 1) << POS_X_BIT_SHIFT) |
               (posZ + (1 << POS_Z_BIT_SIZE - 1) << POS_Z_BIT_SHIFT);
    }

    private boolean nextItem() {
        if (!queueIterator.hasNext()) {
            queueIterator = null;
            return false;
        }

        cursorData = queueIterator.next();
        areNeighboursBlocksValid = false;

        blockPosFromPosLong(cursorBlockPos, cursorData);

        val chunkPosLong = cursorData & CHUNK_POS_MASK;
        if (cursorChunkPosLong != chunkPosLong) {
            cursorChunk = getChunk(cursorBlockPos);
            cursorChunkPosLong = chunkPosLong;
        }

        return true;
    }

    private int getCursorCurrentLightValue(EnumSkyBlock lightType) {
        val posY = cursorBlockPos.getY();
        val subChunkPosX = cursorBlockPos.getX() & 15;
        val subChunkPosZ = cursorBlockPos.getZ() & 15;
        return cursorChunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    private int getCursorBlockLightValue(Block cursorBlock, int cursorBlockMeta, EnumSkyBlock lightType) {
        val posX = cursorBlockPos.getX();
        val posY = cursorBlockPos.getY();
        val posZ = cursorBlockPos.getZ();

        if (lightType == EnumSkyBlock.Sky) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            if (LightingHooks.lumiCanBlockSeeTheSky(cursorChunk, subChunkPosX, posY, subChunkPosZ)) {
                return EnumSkyBlock.Sky.defaultLightValue;
            } else {
                return 0;
            }
        }

        val cursorBlockLightValueVal = world.lumi$getBlockBrightness(cursorBlock, cursorBlockMeta, posX, posY, posZ);
        return MathUtil.clamp(cursorBlockLightValueVal, MIN_LIGHT_VALUE, MAX_LIGHT_VALUE);
    }

    private int getBlockOpacity(BlockPos blockPos, Block block, int blockMeta) {
        val posX = blockPos.getX();
        val posY = blockPos.getY();
        val posZ = blockPos.getZ();
        val blockOpacity = world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
        return MathUtil.clamp(blockOpacity, MIN_BLOCK_OPACITY, MAX_BLOCK_OPACITY);
    }

    private LumiChunk getChunk(BlockPos blockPos) {
        val chunkPosX = blockPos.getX() >> 4;
        val chunkPosZ = blockPos.getZ() >> 4;
        return getLoadedChunk(world, chunkPosX, chunkPosZ);
    }

    public static Block getBlockFromChunk(LumiChunk chunk, BlockPos blockPos) {
        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$subChunk(chunkPosY);
        return getBlockFromSubChunk(subChunk, blockPos);
    }

    public static Block getBlockFromSubChunk(LumiSubChunk subChunk, BlockPos blockPos) {
        if (subChunk == null)
            return Blocks.air;

        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosY = blockPos.getY() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        return subChunk.lumi$root().lumi$getBlock(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    public static int getBlockMetaFromChunk(LumiChunk chunk, BlockPos blockPos) {
        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$subChunk(chunkPosY);
        return getBlockMetaFromSubChunk(subChunk, blockPos);
    }

    public static int getBlockMetaFromSubChunk(LumiSubChunk subChunk, BlockPos blockPos) {
        if (subChunk == null)
            return 0;

        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosY = blockPos.getY() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        return subChunk.lumi$root().lumi$getBlockMeta(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @NoArgsConstructor
    private static class NeighborBlock {
        private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        private long posLong;

        private LumiChunk chunk;
        private LumiSubChunk subChunk;

        private int lightValue;
    }
}
