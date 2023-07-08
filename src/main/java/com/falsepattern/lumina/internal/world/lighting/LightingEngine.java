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
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

import java.util.concurrent.locks.ReentrantLock;

public class LightingEngine implements LumiLightingEngine {
    private static final boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS = true;
    private static final int MAX_SCHEDULED_COUNT = 1 << 22;

    private static final int MAX_LIGHT = 15;

    private final Thread ownedThread = Thread.currentThread();

    private final LumiWorld world;

    private final Profiler profiler;

    //Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
    private final PooledLongQueue[] queuedLightUpdates = new PooledLongQueue[EnumSkyBlock.values().length];

    //Layout of longs: see above
    private final PooledLongQueue[] queuedDarkenings = new PooledLongQueue[MAX_LIGHT + 1];
    private final PooledLongQueue[] queuedBrightenings = new PooledLongQueue[MAX_LIGHT + 1];

    //Layout of longs: [newLight(4)] [pos(60)]
    private final PooledLongQueue initialBrightenings;
    //Layout of longs: [padding(4)] [pos(60)]
    private final PooledLongQueue initialDarkenings;

    private boolean updating = false;

    //Layout parameters
    //Length of bit segments
    private static final int
            lX = 26,
            lY = 8,
            lZ = 26,
            lL = 4;

    //Bit segment shifts/positions
    private static final int
            sZ = 0,
            sX = sZ + lZ,
            sY = sX + lX,
            sL = sY + lY;

    //Bit segment masks
    private static final long
            mX = (1L << lX) - 1,
            mY = (1L << lY) - 1,
            mZ = (1L << lZ) - 1,
            mL = (1L << lL) - 1,
            mPos = (mY << sY) | (mX << sX) | (mZ << sZ);

    //Bit to check whether y had overflow
    private static final long yCheck = 1L << (sY + lY);

    private static final long[] neighborShifts = new long[6];

    static {
        EnumFacing[] values = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
        for (int i = 0; i < 6; ++i) {
            neighborShifts[i] = ((long) values[i].getFrontOffsetY() << sY) | ((long) values[i].getFrontOffsetX() << sX) | ((long) values[i].getFrontOffsetZ() << sZ);
        }
    }

    //Mask to extract chunk identifier
    private static final long mChunk = ((mX >> 4) << (4 + sX)) | ((mZ >> 4) << (4 + sZ));

    //Iteration state data
    //Cache position to avoid allocation of new object each time
    private final BlockPos.MutableBlockPos curPos = new BlockPos.MutableBlockPos();
    private LumiChunk curChunk;
    private long curChunkIdentifier;
    private long curData;
    static boolean isDynamicLightsLoaded;

    //Cached data about neighboring blocks (of tempPos)
    private boolean isNeighborDataValid = false;

    private final NeighborInfo[] neighborInfos = new NeighborInfo[6];
    private PooledLongQueue.LongQueueIterator queueIt;

    private final ReentrantLock lock = new ReentrantLock();

    public LightingEngine(final LumiWorld world) {
        this.world = world;
        this.profiler = world.rootWorld().profiler();
        isDynamicLightsLoaded = Loader.isModLoaded("DynamicLights");

        PooledLongQueue.Pool pool = new PooledLongQueue.Pool();

        this.initialBrightenings = new PooledLongQueue(pool);
        this.initialDarkenings = new PooledLongQueue(pool);

        for (int i = 0; i < EnumSkyBlock.values().length; ++i) {
            this.queuedLightUpdates[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.queuedDarkenings.length; ++i) {
            this.queuedDarkenings[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.queuedBrightenings.length; ++i) {
            this.queuedBrightenings[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.neighborInfos.length; ++i) {
            this.neighborInfos[i] = new NeighborInfo();
        }
    }

    /**
     * Schedules a light update for the specified light type and position to be processed later by {@link LumiLightingEngine#processLightUpdatesForType(EnumSkyBlock)}
     */
    @Override
    public void scheduleLightUpdate(final EnumSkyBlock lightType, final int posX, final int posY, final int posZ) {
        this.acquireLock();

        try {
            this.scheduleLightUpdate(lightType, encodeWorldCoord(posX, posY, posZ));
        } finally {
            this.releaseLock();
        }
    }

    /**
     * Schedules a light update for the specified light type and position to be processed later by {@link LumiLightingEngine#processLightUpdates()}
     */
    private void scheduleLightUpdate(final EnumSkyBlock lightType, final long pos) {
        final PooledLongQueue queue = this.queuedLightUpdates[lightType.ordinal()];

        queue.add(pos);

        //make sure there are not too many queued light updates
        if (queue.size() >= MAX_SCHEDULED_COUNT) {
            this.processLightUpdatesForType(lightType);
        }
    }

    /**
     * Calls {@link LumiLightingEngine#processLightUpdatesForType(EnumSkyBlock)} for both light types
     *
     */
    @Override
    public void processLightUpdates() {
        this.processLightUpdatesForType(EnumSkyBlock.Sky);
        this.processLightUpdatesForType(EnumSkyBlock.Block);
    }

    /**
     * Processes light updates of the given light type
     */
    @Override
    public void processLightUpdatesForType(final EnumSkyBlock lightType) {
        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (this.world.rootWorld().isClientSide() && !this.isCallingFromMainThread()) {
            return;
        }

        final PooledLongQueue queue = this.queuedLightUpdates[lightType.ordinal()];

        // Quickly check if the queue is empty before we acquire a more expensive lock.
        if (queue.isEmpty()) {
            return;
        }

        this.acquireLock();

        try {
            this.processLightUpdatesForTypeInner(lightType, queue);
        } finally {
            this.releaseLock();
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean isCallingFromMainThread() {
        return Minecraft.getMinecraft().func_152345_ab();
    }

    private void acquireLock() {
        if (!this.lock.tryLock()) {
            // If we cannot lock, something has gone wrong... Only one thread should ever acquire the lock.
            // Validate that we're on the right thread immediately so we can gather information.
            // It is NEVER valid to call World methods from a thread other than the owning thread of the world instance.
            // Users can safely disable this warning, however it will not resolve the issue.
            if (ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS) {
                Thread current = Thread.currentThread();

                if (current != this.ownedThread) {
                    IllegalAccessException e = new IllegalAccessException(String.format("World is owned by '%s' (ID: %s)," +
                                    " but was accessed from thread '%s' (ID: %s)",
                            this.ownedThread.getName(), this.ownedThread.getId(), current.getName(), current.getId()));

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
            this.lock.lock();
        }
    }

    private void releaseLock() {
        this.lock.unlock();
    }

    private void processLightUpdatesForTypeInner(final EnumSkyBlock lightType, final PooledLongQueue queue) {
        //avoid nested calls
        if (this.updating) {
            throw new IllegalStateException("Already processing updates!");
        }

        this.updating = true;

        this.curChunkIdentifier = -1; //reset chunk cache

        this.profiler.startSection("lighting");

        this.profiler.startSection("checking");

        this.queueIt = queue.iterator();

        //process the queued updates and enqueue them for further processing
        while (this.nextItem()) {
            if (this.curChunk == null) {
                continue;
            }

            final int oldLight = this.getCursorCachedLight(lightType);
            final int newLight = this.calculateNewLightFromCursor(lightType);

            if (oldLight < newLight) {
                //don't enqueue directly for brightening in order to avoid duplicate scheduling
                this.initialBrightenings.add(((long) newLight << sL) | this.curData);
            } else if (oldLight > newLight) {
                //don't enqueue directly for darkening in order to avoid duplicate scheduling
                this.initialDarkenings.add(this.curData);
            }
        }

        this.queueIt = this.initialBrightenings.iterator();

        while (this.nextItem()) {
            final int newLight = (int) (this.curData >> sL & mL);

            if (newLight > this.getCursorCachedLight(lightType)) {
                //Sets the light to newLight to only schedule once. Clear leading bits of curData for later
                this.enqueueBrightening(this.curPos, this.curData & mPos, newLight, this.curChunk, lightType);
            }
        }

        this.queueIt = this.initialDarkenings.iterator();

        while (this.nextItem()) {
            final int oldLight = this.getCursorCachedLight(lightType);

            if (oldLight != 0) {
                //Sets the light to 0 to only schedule once
                this.enqueueDarkening(this.curPos, this.curData, oldLight, this.curChunk, lightType);
            }
        }

        this.profiler.endSection();

        //Iterate through enqueued updates (brightening and darkening in parallel) from brightest to darkest so that we only need to iterate once
        for (int curLight = MAX_LIGHT; curLight >= 0; --curLight) {
            this.profiler.startSection("darkening");

            this.queueIt = this.queuedDarkenings[curLight].iterator();

            while (this.nextItem()) {
                if (this.getCursorCachedLight(lightType) >= curLight) //don't darken if we got brighter due to some other change
                {
                    continue;
                }

                final Block block = LightingEngineHelpers.posToBlock(this.curPos, this.curChunk);
                final int meta = LightingEngineHelpers.posToMeta(this.curPos, this.curChunk);
                final int luminosity = this.getCursorLuminosity(block, meta, lightType);
                final int opacity; //if luminosity is high enough, opacity is irrelevant

                if (luminosity >= MAX_LIGHT - 1) {
                    opacity = 1;
                } else {
                    opacity = this.getPosOpacity(this.curPos, block, meta);
                }

                //only darken neighbors if we indeed became darker
                if (this.calculateNewLightFromCursor(luminosity, opacity, lightType) < curLight) {
                    //need to calculate new light value from neighbors IGNORING neighbors which are scheduled for darkening
                    int newLight = luminosity;

                    this.fetchNeighborDataFromCursor(lightType);

                    for (NeighborInfo info : this.neighborInfos) {
                        final LumiChunk nChunk = info.chunk;

                        if (nChunk == null) {
                            continue;
                        }

                        final int nLight = info.light;

                        if (nLight == 0) {
                            continue;
                        }

                        final BlockPos.MutableBlockPos nPos = info.pos;

                        if (curLight - this.getPosOpacity(nPos, LightingEngineHelpers.posToBlock(nPos, info.section), LightingEngineHelpers.posToMeta(nPos, info.section)) >= nLight) //schedule neighbor for darkening if we possibly light it
                        {
                            this.enqueueDarkening(nPos, info.key, nLight, nChunk, lightType);
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

            this.queueIt = this.queuedBrightenings[curLight].iterator();

            while (this.nextItem()) {
                final int oldLight = this.getCursorCachedLight(lightType);

                if (oldLight == curLight) //only process this if nothing else has happened at this position since scheduling
                {
                    this.world.rootWorld().markBlockForRenderUpdate(this.curPos.getX(), this.curPos.getY(), this.curPos.getZ());

                    if (curLight > 1) {
                        this.spreadLightFromCursor(curLight, lightType);
                    }
                }
            }

            this.profiler.endSection();
        }

        this.profiler.endSection();

        this.updating = false;
    }

    /**
     * Gets data for neighbors of <code>curPos</code> and saves the results into neighbor state data members. If a neighbor can't be accessed/doesn't exist, the corresponding entry in <code>neighborChunks</code> is <code>null</code> - others are not reset
     */
    private void fetchNeighborDataFromCursor(final EnumSkyBlock lightType) {
        //only update if curPos was changed
        if (this.isNeighborDataValid) {
            return;
        }

        this.isNeighborDataValid = true;

        for (int i = 0; i < this.neighborInfos.length; ++i) {
            NeighborInfo info = this.neighborInfos[i];

            final long nLongPos = info.key = this.curData + neighborShifts[i];

            if ((nLongPos & yCheck) != 0) {
                info.chunk = null;
                info.section = null;
                continue;
            }

            final BlockPos.MutableBlockPos nPos = decodeWorldCoord(info.pos, nLongPos);

            final LumiChunk nChunk;

            if ((nLongPos & mChunk) == this.curChunkIdentifier) {
                nChunk = info.chunk = this.curChunk;
            } else {
                nChunk = info.chunk = this.getChunk(nPos);
            }

            if (nChunk != null) {
                LumiSubChunk nSection = nChunk.subChunk(nPos.getY() >> 4);

                info.light = getCachedLightFor(nChunk, nSection, nPos, lightType);
                info.section = nSection;
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
            if (!chunk.lumiWorld().rootWorld().hasSky()) {
                return 0;
            } else {
                return storage.getSkyLightValue(i, j & 15, k);
            }
        } else {
            if (type == EnumSkyBlock.Block) {
                return storage.getBlockLightValue(i, j & 15, k);
            } else {
                return type.defaultLightValue;
            }
        }
    }


    private int calculateNewLightFromCursor(final EnumSkyBlock lightType) {
        final Block block = LightingEngineHelpers.posToBlock(this.curPos, this.curChunk);
        final int meta = LightingEngineHelpers.posToMeta(this.curPos, this.curChunk);

        final int luminosity = this.getCursorLuminosity(block, meta, lightType);
        final int opacity;

        if (luminosity >= MAX_LIGHT - 1) {
            opacity = 1;
        } else {
            opacity = this.getPosOpacity(this.curPos, block, meta);
        }

        return this.calculateNewLightFromCursor(luminosity, opacity, lightType);
    }

    private int calculateNewLightFromCursor(final int luminosity, final int opacity, final EnumSkyBlock lightType) {
        if (luminosity >= MAX_LIGHT - opacity) {
            return luminosity;
        }

        int newLight = luminosity;

        this.fetchNeighborDataFromCursor(lightType);

        for (NeighborInfo info : this.neighborInfos) {
            if (info.chunk == null) {
                continue;
            }

            final int nLight = info.light;

            newLight = Math.max(nLight - opacity, newLight);
        }

        return newLight;
    }

    private void spreadLightFromCursor(final int curLight, final EnumSkyBlock lightType) {
        this.fetchNeighborDataFromCursor(lightType);

        for (NeighborInfo info : this.neighborInfos) {
            final LumiChunk nChunk = info.chunk;

            if (nChunk == null) {
                continue;
            }

            final int newLight = curLight - this.getPosOpacity(info.pos, LightingEngineHelpers.posToBlock(info.pos, info.section), LightingEngineHelpers.posToMeta(info.pos, info.section));

            if (newLight > info.light) {
                this.enqueueBrightening(info.pos, info.key, newLight, nChunk, lightType);
            }
        }
    }

    private void enqueueBrighteningFromCursor(final int newLight, final EnumSkyBlock lightType) {
        this.enqueueBrightening(this.curPos, this.curData, newLight, this.curChunk, lightType);
    }

    /**
     * Enqueues the pos for brightening and sets its light value to <code>newLight</code>
     */
    private void enqueueBrightening(final BlockPos pos, final long longPos, final int newLight, final LumiChunk chunk, final EnumSkyBlock lightType) {
        this.queuedBrightenings[newLight].add(longPos);

        LightingHooks.lumiSetLightValue(chunk, lightType, pos.getX() & 15, pos.getY(), pos.getZ() & 15, newLight);
    }

    /**
     * Enqueues the pos for darkening and sets its light value to 0
     */
    private void enqueueDarkening(final BlockPos pos, final long longPos, final int oldLight, final LumiChunk chunk, final EnumSkyBlock lightType) {
        this.queuedDarkenings[oldLight].add(longPos);

        LightingHooks.lumiSetLightValue(chunk, lightType, pos.getX() & 15, pos.getY(), pos.getZ() & 15, 0);
    }

    private static BlockPos.MutableBlockPos decodeWorldCoord(final BlockPos.MutableBlockPos pos, final long longPos) {
        final int posX = (int) (longPos >> sX & mX) - (1 << lX - 1);
        final int posY = (int) (longPos >> sY & mY);
        final int posZ = (int) (longPos >> sZ & mZ) - (1 << lZ - 1);

        return pos.setPos(posX, posY, posZ);
    }

    private static long encodeWorldCoord(final long x, final long y, final long z) {
        return (y << sY) | (x + (1 << lX - 1) << sX) | (z + (1 << lZ - 1) << sZ);
    }

    private static int ITEMS_PROCESSED = 0, CHUNKS_FETCHED = 0;

    /**
     * Polls a new item from <code>curQueue</code> and fills in state data members
     *
     * @return If there was an item to poll
     */
    private boolean nextItem() {
        if (!this.queueIt.hasNext()) {
            this.queueIt.finish();
            this.queueIt = null;

            return false;
        }

        this.curData = this.queueIt.next();
        this.isNeighborDataValid = false;

        decodeWorldCoord(this.curPos, this.curData);

        final long chunkIdentifier = this.curData & mChunk;

        if (this.curChunkIdentifier != chunkIdentifier) {
            this.curChunk = this.getChunk(this.curPos);
            this.curChunkIdentifier = chunkIdentifier;
            CHUNKS_FETCHED++;
        }

        ITEMS_PROCESSED++;

        return true;
    }

    private int getCursorCachedLight(final EnumSkyBlock lightType) {
        return LightingHooks.getCachedLightFor(this.curChunk, lightType, this.curPos.getX(), this.curPos.getY(), this.curPos.getZ());
    }

    /**
     * Calculates the luminosity for <code>curPos</code>, taking into account <code>lightType</code>
     */
    private int getCursorLuminosity(final Block block, final int meta, final EnumSkyBlock lightType) {
        if (lightType == EnumSkyBlock.Sky) {
            if (LightingHooks.lumiCanBlockSeeTheSky(this.curChunk, this.curPos.getX() & 15, this.curPos.getY(), this.curPos.getZ() & 15)) {
                return EnumSkyBlock.Sky.defaultLightValue;
            } else {
                return 0;
            }
        }

        return MathHelper.clamp_int(world.getBlockBrightness(block, meta, this.curPos.getX(), this.curPos.getY(), this.curPos.getZ()), 0, MAX_LIGHT);
    }

    private int getPosOpacity(final BlockPos pos, final Block block, final int meta) {
        return MathHelper.clamp_int(world.getBlockOpacity(block, meta, pos.getX(), pos.getY(), pos.getZ()), 1, MAX_LIGHT);
    }

    private LumiChunk getChunk(final BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        return LightingEngineHelpers.getLoadedChunk(world, chunkX, chunkZ);
    }

    private static class NeighborInfo {
        LumiChunk chunk;
        LumiSubChunk section;

        int light;

        long key;

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    }
}

