/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.internal.world.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.collections.PooledLongQueue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

import java.util.concurrent.locks.ReentrantLock;

import static com.falsepattern.lumina.internal.world.lighting.LightingEngineHelpers.*;

public class LightingEngine implements LumiLightingEngine {
    private static final boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS = true;

    private static final int MIN_LIGHT_VALUE = 0;
    private static final int MAX_LIGHT_VALUE = 15;

    private static final int MAX_SCHEDULED_COUNT = 1 << 22;

    private static final int POS_Z_BIT_SIZE = 26;
    private static final int POS_X_BIT_SIZE = 8;
    private static final int POS_Y_BIT_SIZE = 26;
    private static final int LIGHT_VALUE_BIT_SIZE = 4;

    private static final int POS_Z_BIT_SHIFT = 0;
    private static final int POS_X_BIT_SHIFT = POS_Z_BIT_SHIFT + POS_Y_BIT_SIZE;
    private static final int POS_Y_BIT_SHIFT = POS_X_BIT_SHIFT + POS_Z_BIT_SIZE;
    private static final int LIGHT_VALUE_BIT_SHIFT = POS_Y_BIT_SHIFT + POS_X_BIT_SIZE;

    private static final long yCheck = 1L << (POS_Y_BIT_SHIFT + POS_X_BIT_SIZE);

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
    private final PooledLongQueue initialBrighteningQueue;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue initialDarkeningsQueue;

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

    public LightingEngine(LumiWorld world) {
        this.world = world;
        this.profiler = world.lumi$root().lumi$profiler();

        val queuePool = new PooledLongQueue.Pool();
        this.initialDarkeningsQueue = new PooledLongQueue(queuePool);
        this.initialBrighteningQueue = new PooledLongQueue(queuePool);
        for (var i = 0; i < updateQueues.length; i++)
            updateQueues[i] = new PooledLongQueue(queuePool);
        for (var i = 0; i < brighteningQueues.length; i++)
            brighteningQueues[i] = new PooledLongQueue(queuePool);
        for (var i = 0; i < darkeningQueues.length; i++)
            darkeningQueues[i] = new PooledLongQueue(queuePool);

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
        //avoid nested calls
        if (this.isUpdating) {
            throw new IllegalStateException("Already processing updates!");
        }

        this.isUpdating = true;

        this.cursorChunkPosLong = -1; //reset chunk cache

        this.profiler.startSection("lighting");

        this.profiler.startSection("checking");

        this.queueIterator = queue.iterator();

        //process the queued updates and enqueue them for further processing
        while (this.nextItem()) {
            if (this.cursorChunk == null) {
                continue;
            }

            final int oldLight = this.getCursorCurrentLightValue(lightType);
            final int newLight = this.calculateNewLightFromCursor(lightType);

            if (oldLight < newLight) {
                //don't enqueue directly for brightening in order to avoid duplicate scheduling
                this.initialBrighteningQueue.add(((long) newLight << LIGHT_VALUE_BIT_SHIFT) | this.cursorData);
            } else if (oldLight > newLight) {
                //don't enqueue directly for darkening in order to avoid duplicate scheduling
                this.initialDarkeningsQueue.add(this.cursorData);
            }
        }

        this.queueIterator = this.initialBrighteningQueue.iterator();

        while (this.nextItem()) {
            final int newLight = (int) (this.cursorData >> LIGHT_VALUE_BIT_SHIFT & LIGHT_VALUE_BIT_MASK);

            if (newLight > this.getCursorCurrentLightValue(lightType)) {
                //Sets the light to newLight to only schedule once. Clear leading bits of curData for later
                this.enqueueBrightening(this.cursorBlockPos, this.cursorData & BLOCK_POS_MASK, newLight, this.cursorChunk, lightType);
            }
        }

        this.queueIterator = this.initialDarkeningsQueue.iterator();

        while (this.nextItem()) {
            final int oldLight = this.getCursorCurrentLightValue(lightType);

            if (oldLight != 0) {
                //Sets the light to 0 to only schedule once
                this.enqueueDarkening(this.cursorBlockPos, this.cursorData, oldLight, this.cursorChunk, lightType);
            }
        }

        this.profiler.endSection();

        //Iterate through enqueued updates (brightening and darkening in parallel) from brightest to darkest so that we only need to iterate once
        for (int curLight = MAX_LIGHT_VALUE; curLight >= 0; --curLight) {
            this.profiler.startSection("darkening");

            this.queueIterator = this.brighteningQueues[curLight].iterator();

            while (this.nextItem()) {
                if (this.getCursorCurrentLightValue(lightType) >= curLight) //don't darken if we got brighter due to some other change
                {
                    continue;
                }

                final Block block = LightingEngineHelpers.getBlockFromChunk(this.cursorChunk, this.cursorBlockPos);
                final int meta = LightingEngineHelpers.getBlockMetaFromChunk(this.cursorChunk, this.cursorBlockPos);
                final int luminosity = this.getCursorBlockLightValue(block, meta, lightType);
                final int opacity; //if luminosity is high enough, opacity is irrelevant

                if (luminosity >= MAX_LIGHT_VALUE - 1) {
                    opacity = 1;
                } else {
                    opacity = this.getBlockOpacity(this.cursorBlockPos, block, meta);
                }

                //only darken neighbors if we indeed became darker
                if (this.calculateNewLightFromCursor(luminosity, opacity, lightType) < curLight) {
                    //need to calculate new light value from neighbors IGNORING neighbors which are scheduled for darkening
                    int newLight = luminosity;

                    this.updateNeighborBlocks(lightType);

                    for (NeighborBlock info : this.neighborBlocks) {
                        final LumiChunk nChunk = info.chunk;

                        if (nChunk == null) {
                            continue;
                        }

                        final int nLight = info.lightValue;

                        if (nLight == 0) {
                            continue;
                        }

                        final BlockPos.MutableBlockPos nPos = info.blockPos;

                        if (curLight - this.getBlockOpacity(nPos, getBlockFromSubChunk(info.subChunk, nPos), getBlockMetaFromSubChunk(info.subChunk, nPos)) >= nLight) //schedule neighbor for darkening if we possibly light it
                        {
                            this.enqueueDarkening(nPos, info.posLong, nLight, nChunk, lightType);
                        } else //only use for new light calculation if not
                        {
                            //if we can't darken the neighbor, no one else can (because of processing order) -> safe to let us be illuminated by it
                            newLight = Math.max(newLight, nLight - opacity);
                        }
                    }

                    //schedule brightening since light level was set to 0
                    this.enqueueBrighteningFromCursor(newLight, lightType);
                } else //we didn't become darker, so we need to re-set our initial light value (was set to 0) and notify neighbors
                {
                    this.enqueueBrighteningFromCursor(curLight, lightType); //do not spread to neighbors immediately to avoid scheduling multiple times
                }
            }

            this.profiler.endStartSection("brightening");

            this.queueIterator = this.darkeningQueues[curLight].iterator();

            while (this.nextItem()) {
                final int oldLight = this.getCursorCurrentLightValue(lightType);

                if (oldLight == curLight) //only process this if nothing else has happened at this position since scheduling
                {
                    this.world.lumi$root().lumi$markBlockForRenderUpdate(this.cursorBlockPos.getX(), this.cursorBlockPos.getY(), this.cursorBlockPos.getZ());

                    if (curLight > 1) {
                        this.spreadLightFromCursor(curLight, lightType);
                    }
                }
            }

            this.profiler.endSection();
        }

        this.profiler.endSection();

        this.isUpdating = false;
    }

    /**
     * Gets data for neighbors of <code>curPos</code> and saves the results into neighbor state data members. If a neighbor can't be accessed/doesn't exist, the corresponding entry in <code>neighborChunks</code> is <code>null</code> - others are not reset
     */
    private void updateNeighborBlocks(final EnumSkyBlock lightType) {
        //only update if curPos was changed
        if (this.areNeighboursBlocksValid) {
            return;
        }

        this.areNeighboursBlocksValid = true;

        for (int i = 0; i < this.neighborBlocks.length; ++i) {
            NeighborBlock info = this.neighborBlocks[i];

            final long nLongPos = info.posLong = this.cursorData + BLOCK_SIDE_BIT_OFFSET[i];

            if ((nLongPos & yCheck) != 0) {
                info.chunk = null;
                info.subChunk = null;
                continue;
            }

            final BlockPos.MutableBlockPos nPos = blockPosFromPosLong(info.blockPos, nLongPos);

            final LumiChunk nChunk;

            if ((nLongPos & CHUNK_POS_MASK) == this.cursorChunkPosLong) {
                nChunk = info.chunk = this.cursorChunk;
            } else {
                nChunk = info.chunk = this.getChunk(nPos);
            }

            if (nChunk != null) {
                LumiSubChunk nSection = nChunk.lumi$subChunk(nPos.getY() >> 4);

                info.lightValue = getCachedLightFor(nChunk, nSection, nPos, lightType);
                info.subChunk = nSection;
            }
        }
    }


    private static int getCachedLightFor(LumiChunk chunk, LumiSubChunk storage, BlockPos pos, EnumSkyBlock type) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;

        if (storage == null) {
            if (type == EnumSkyBlock.Sky && LightingHooks.lumiCanBlockSeeTheSky(chunk, i, j, k)) {
                return type.defaultLightValue;
            } else {
                return 0;
            }
        } else if (type == EnumSkyBlock.Sky) {
            if (!chunk.lumi$world().lumi$root().lumi$hasSky()) {
                return 0;
            } else {
                return storage.lumi$getSkyLightValue(i, j & 15, k);
            }
        } else {
            if (type == EnumSkyBlock.Block) {
                return storage.lumi$getBlockLightValue(i, j & 15, k);
            } else {
                return type.defaultLightValue;
            }
        }
    }


    private int calculateNewLightFromCursor(final EnumSkyBlock lightType) {
        final Block block = LightingEngineHelpers.getBlockFromChunk(this.cursorChunk, this.cursorBlockPos);
        final int meta = LightingEngineHelpers.getBlockMetaFromChunk(this.cursorChunk, this.cursorBlockPos);

        final int luminosity = this.getCursorBlockLightValue(block, meta, lightType);
        final int opacity;

        if (luminosity >= MAX_LIGHT_VALUE - 1) {
            opacity = 1;
        } else {
            opacity = this.getBlockOpacity(this.cursorBlockPos, block, meta);
        }

        return this.calculateNewLightFromCursor(luminosity, opacity, lightType);
    }

    private int calculateNewLightFromCursor(final int luminosity, final int opacity, final EnumSkyBlock lightType) {
        if (luminosity >= MAX_LIGHT_VALUE - opacity) {
            return luminosity;
        }

        int newLight = luminosity;

        this.updateNeighborBlocks(lightType);

        for (NeighborBlock info : this.neighborBlocks) {
            if (info.chunk == null) {
                continue;
            }

            final int nLight = info.lightValue;

            newLight = Math.max(nLight - opacity, newLight);
        }

        return newLight;
    }

    private void spreadLightFromCursor(final int curLight, final EnumSkyBlock lightType) {
        updateNeighborBlocks(lightType);

        for (NeighborBlock info : this.neighborBlocks) {
            final LumiChunk nChunk = info.chunk;

            if (nChunk == null) {
                continue;
            }

            final int newLight = curLight - this.getBlockOpacity(info.blockPos, LightingEngineHelpers.getBlockFromSubChunk(info.subChunk, info.blockPos), LightingEngineHelpers.getBlockMetaFromSubChunk(info.subChunk, info.blockPos));

            if (newLight > info.lightValue) {
                this.enqueueBrightening(info.blockPos, info.posLong, newLight, nChunk, lightType);
            }
        }
    }

    private void enqueueBrighteningFromCursor(final int newLight, final EnumSkyBlock lightType) {
        this.enqueueBrightening(this.cursorBlockPos, this.cursorData, newLight, this.cursorChunk, lightType);
    }

    /**
     * Enqueues the pos for brightening and sets its light value to <code>newLight</code>
     */
    private void enqueueBrightening(BlockPos blockPos, long posLong, int newLight, LumiChunk chunk, EnumSkyBlock lightType) {
        this.darkeningQueues[newLight].add(posLong);

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, newLight);
    }

    /**
     * Enqueues the pos for darkening and sets its light value to 0
     */
    private void enqueueDarkening(BlockPos blockPos, long posLong, int oldLight, LumiChunk chunk, EnumSkyBlock lightType) {
        this.brighteningQueues[oldLight].add(posLong);

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, 0);
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
            queueIterator.finish();
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
        return MathHelper.clamp_int(cursorBlockLightValueVal, MIN_LIGHT_VALUE, MAX_LIGHT_VALUE);
    }

    private int getBlockOpacity(BlockPos blockPos, Block block, int meta) {
        val posX = blockPos.getX();
        val posY = blockPos.getY();
        val posZ = blockPos.getZ();
        val blockOpacity = world.lumi$getBlockOpacity(block, meta, posX, posY, posZ);
        return MathHelper.clamp_int(blockOpacity, 1, MAX_LIGHT_VALUE);//TODO: This is clamping between (1, 15) because some other math is messed up.
    }

    private LumiChunk getChunk(final BlockPos pos) {
        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;
        return getLoadedChunk(world, chunkX, chunkZ);
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

