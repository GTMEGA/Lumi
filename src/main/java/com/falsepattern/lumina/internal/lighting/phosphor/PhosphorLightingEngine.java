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

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.falsepattern.lumina.api.chunk.LumiChunk.MAX_QUEUED_RANDOM_LIGHT_UPDATES;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.values;
import static com.falsepattern.lumina.internal.lighting.phosphor.PhosporUtil.getLoadedChunk;
import static cpw.mods.fml.relauncher.Side.CLIENT;


public final class PhosphorLightingEngine implements LumiLightingEngine {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Phosphor");

    private static final boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS = true;

    private static final int MIN_LIGHT_VALUE = 0;
    private static final int MAX_LIGHT_VALUE = 15;
    private static final int LIGHT_VALUE_RANGE = (MAX_LIGHT_VALUE - MIN_LIGHT_VALUE) + 1;

    private static final int LIGHT_VALUE_TYPES_COUNT = values().length;

    private static final int MIN_BLOCK_OPACITY = 1;
    private static final int MAX_BLOCK_OPACITY = 15;

    /**
     * Maximum scheduled lighting updates before processing the updates is forced.
     */
    private static final int MAX_SCHEDULED_UPDATES = 1 << 22;

    /**
     * Bit length of the Z coordinate in a pos long.
     */
    private static final int POS_Z_BIT_LENGTH = 26;
    /**
     * Bit length of the X coordinate in a pos long.
     */
    private static final int POS_X_BIT_LENGTH = 26;
    /**
     * Bit length of the Y coordinate in a pos long.
     */
    private static final int POS_Y_BIT_LENGTH = 8;
    /**
     * Bit length of the light value in a pos long.
     */
    private static final int LIGHT_VALUE_BIT_LENGTH = 4;
    /**
     * Bit shift for the Z coordinate in a pos long.
     */
    private static final int POS_Z_BIT_SHIFT = 0;
    /**
     * Bit shift the X coordinate in a pos long.
     */
    private static final int POS_X_BIT_SHIFT = POS_Z_BIT_SHIFT + POS_Z_BIT_LENGTH;
    /**
     * Bit shift the Y coordinate in a pos long.
     */
    private static final int POS_Y_BIT_SHIFT = POS_X_BIT_SHIFT + POS_X_BIT_LENGTH;
    /**
     * Bit shift for the light value in a pos long.
     */
    private static final int LIGHT_VALUE_BIT_SHIFT = POS_Y_BIT_SHIFT + POS_Y_BIT_LENGTH;
    /**
     * Bit mask of the Z coordinate in a pos long.
     */
    private static final long POS_Z_BIT_MASK = (1L << POS_Z_BIT_LENGTH) - 1;
    /**
     * Bit mask of the X coordinate in a pos long.
     */
    private static final long POS_X_BIT_MASK = (1L << POS_X_BIT_LENGTH) - 1;
    /**
     * Bit mask of the Y coordinate in a pos long.
     */
    private static final long POS_Y_BIT_MASK = (1L << POS_Y_BIT_LENGTH) - 1;
    /**
     * Bit mask of the light value in a pos long.
     */
    private static final long LIGHT_VALUE_BIT_MASK = (1L << LIGHT_VALUE_BIT_LENGTH) - 1;
    /**
     * Composite bit mask for the XYZ coordinates in a pos long.
     */
    private static final long BLOCK_POS_BIT_MASK = (POS_Z_BIT_MASK << POS_Z_BIT_SHIFT) |
                                                   (POS_X_BIT_MASK << POS_X_BIT_SHIFT) |
                                                   (POS_Y_BIT_MASK << POS_Y_BIT_SHIFT);
    /**
     * Bit mask zeroing out the Y coordinate and light value in a pos long, alongside the lower 4 bits in X and Z.
     * <p>
     * Used for comparing if two given pos longs are in the same chunk.
     */
    private static final long BLOCK_POS_CHUNK_BIT_MASK = ((POS_Z_BIT_MASK >> 4) << (4 + POS_Z_BIT_SHIFT)) |
                                                         ((POS_X_BIT_MASK >> 4) << (4 + POS_X_BIT_SHIFT));
    /**
     * Pos long overflow check bit mask.
     * <p>
     * Used for checking if a pos long has overflowed into the light value range,
     * which is expected when dealing with cursor data, but not when dealing with a simple pos long.
     */
    private static final long POS_OVERFLOW_CHECK_BIT_MASK = 1L << (POS_Y_BIT_SHIFT + POS_Y_BIT_LENGTH);

    private static final List<Direction> NEIGHBOUR_DIRECTIONS = Direction.validDirections();
    private static final int NEIGHBOUR_COUNT = NEIGHBOUR_DIRECTIONS.size();

    private static final long[] BLOCK_SIDE_BIT_OFFSET;

    static {
        BLOCK_SIDE_BIT_OFFSET = new long[NEIGHBOUR_COUNT];
        for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
            val direction = NEIGHBOUR_DIRECTIONS.get(i);
            BLOCK_SIDE_BIT_OFFSET[i] = ((long) direction.xOffset() << POS_X_BIT_SHIFT) |
                                       ((long) direction.yOffset() << POS_Y_BIT_SHIFT) |
                                       ((long) direction.zOffset() << POS_Z_BIT_SHIFT);
        }
    }

    private final Thread updateThread = Thread.currentThread();
    private final ReentrantLock lock = new ReentrantLock();

    private final LumiWorld world;
    private final LumiWorldRoot worldRoot;
    private final Profiler profiler;

    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] updateQueues;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] brighteningQueues;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue[] darkeningQueues;
    /**
     * Layout of longs: [newLight(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue initialBrighteningQueue;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final PooledLongQueue initialDarkeningQueue;

    private @Nullable PooledLongQueue.LongQueueIterator queueIterator;

    private final BlockPos.MutableBlockPos cursorBlockPos;
    private @Nullable LumiChunk cursorChunk;
    private long cursorChunkPosLong;
    private long cursorData;

    private final NeighborBlock[] neighborBlocks;
    private boolean areNeighboursBlocksValid;

    private boolean isUpdating;

    PhosphorLightingEngine(LumiWorld world, Profiler profiler) {
        this.world = world;
        this.worldRoot = world.lumi$root();
        this.profiler = profiler;

        val queuePool = PooledLongQueue.createPool();
        this.updateQueues = new PooledLongQueue[LIGHT_VALUE_TYPES_COUNT];
        for (var i = 0; i < LIGHT_VALUE_TYPES_COUNT; i++)
            this.updateQueues[i] = queuePool.createQueue();
        this.darkeningQueues = new PooledLongQueue[LIGHT_VALUE_RANGE];
        this.brighteningQueues = new PooledLongQueue[LIGHT_VALUE_RANGE];
        for (var i = 0; i < LIGHT_VALUE_RANGE; i++)
            this.brighteningQueues[i] = queuePool.createQueue();
        for (var i = 0; i < LIGHT_VALUE_RANGE; i++)
            this.darkeningQueues[i] = queuePool.createQueue();
        this.initialBrighteningQueue = queuePool.createQueue();
        this.initialDarkeningQueue = queuePool.createQueue();
        this.queueIterator = null;

        this.neighborBlocks = new NeighborBlock[NEIGHBOUR_COUNT];
        for (var i = 0; i < NEIGHBOUR_COUNT; i++)
            neighborBlocks[i] = new NeighborBlock();
        this.areNeighboursBlocksValid = false;

        this.cursorBlockPos = new BlockPos.MutableBlockPos();
        this.cursorChunk = null;
        this.cursorChunkPosLong = 0L;
        this.cursorData = 0L;

        this.isUpdating = false;
    }

    @Override
    public @NotNull String lightingEngineID() {
        return "phosphor";
    }

    @Override
    public void lumi$writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output) {
        PhosporUtil.writeNeighborLightChecksToNBT(chunk, output);
    }

    @Override
    public void lumi$readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input) {
        PhosporUtil.readNeighborLightChecksFromNBT(chunk, input);
    }

    @Override
    public void lumi$writeSubChunkToNBT(@NotNull LumiChunk chunk,
                                        @NotNull LumiSubChunk subChunk,
                                        @NotNull NBTTagCompound output) {
    }

    @Override
    public void lumi$readSubChunkFromNBT(@NotNull LumiChunk chunk,
                                         @NotNull LumiSubChunk subChunk,
                                         @NotNull NBTTagCompound input) {
    }

    @Override
    public void lumi$writeChunkToPacket(@NotNull LumiChunk chunk,
                                        @NotNull ByteBuffer output) {
    }

    @Override
    public void lumi$readChunkFromPacket(@NotNull LumiChunk chunk,
                                         @NotNull ByteBuffer input) {
    }

    @Override
    public void lumi$writeSubChunkToPacket(@NotNull LumiChunk chunk,
                                           @NotNull LumiSubChunk subChunk,
                                           @NotNull ByteBuffer input) {

    }

    @Override
    public void lumi$readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                            @NotNull LumiSubChunk subChunk,
                                            @NotNull ByteBuffer output) {
    }

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos) {
        return getCurrentLightValue(lightType, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        processLightingUpdatesForType(lightType);
        return world.lumi$getLightValue(lightType, posX, posY, posZ);
    }

    @Override
    public int getBrightnessAndLightValueMax(@NotNull LightType lightType, @NotNull BlockPos blockPos) {
        return getBrightnessAndLightValueMax(lightType, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public int getBrightnessAndLightValueMax(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return world.lumi$getBrightnessAndLightValueMax(lightType, posX, posY, posZ);
    }

    @Override
    public boolean isChunkFullyLit(@NotNull LumiChunk chunk) {
        return PhosporUtil.isChunkFullyLit(world, chunk);
    }

    @Override
    public void handleChunkInit(@NotNull LumiChunk chunk) {
        val worldRoot = chunk.lumi$world().lumi$root();
        val hasSky = worldRoot.lumi$hasSky();

        val chunkRoot = chunk.lumi$root();

        val basePosX = chunk.lumi$chunkPosX() << 4;
        val basePosY = chunkRoot.lumi$topPreparedSubChunkBasePosY();
        val basePosZ = chunk.lumi$chunkPosZ() << 4;

        val maxPosY = basePosY + 15;

        var minSkyLightHeight = Integer.MAX_VALUE;
        for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
            int subChunkPosZ = 0;
            while (subChunkPosZ < 16) {
                var skyLightHeight = maxPosY;

                while (true) {
                    if (skyLightHeight > 0) {
                        val posY = skyLightHeight - 1;
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                        if (blockOpacity == 0) {
                            skyLightHeight--;
                            continue;
                        }

                        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, skyLightHeight);
                        minSkyLightHeight = Math.min(minSkyLightHeight, skyLightHeight);
                    }

                    if (hasSky) {
                        var lightLevel = 15;
                        skyLightHeight = (basePosY + 16) - 1;

                        do {
                            var blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, skyLightHeight, subChunkPosZ);
                            if (blockOpacity == 0 && lightLevel != 15)
                                blockOpacity = 1;

                            lightLevel -= blockOpacity;
                            if (lightLevel > 0) {
                                val chunkPosY = skyLightHeight / 16;
                                val subChunkPosY = skyLightHeight & 15;

                                val subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
                                if (subChunk != null) {
                                    val posX = basePosX + subChunkPosX;
                                    val posZ = basePosZ + subChunkPosZ;

                                    subChunk.lumi$setSkyLightValue(subChunkPosX,
                                                                   subChunkPosY,
                                                                   subChunkPosZ,
                                                                   lightLevel);
                                    worldRoot.lumi$markBlockForRenderUpdate(posX, skyLightHeight, posZ);
                                }
                            }

                            skyLightHeight--;
                        }
                        while (skyLightHeight > 0 && lightLevel > 0);
                    }

                    subChunkPosZ++;
                    break;
                }
            }
        }

        chunk.lumi$minSkyLightHeight(minSkyLightHeight);
        chunkRoot.lumi$markDirty();
    }

    @Override
    @SideOnly(CLIENT)
    public void handleClientChunkInit(@NotNull LumiChunk chunk) {
        val chunkRoot = chunk.lumi$root();

        val basePosY = chunkRoot.lumi$topPreparedSubChunkBasePosY();
        val maxPosY = basePosY + 15;

        var minSkyLightHeight = Integer.MAX_VALUE;
        for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
            var subChunkPosZ = 0;

            while (subChunkPosZ < 16) {
                var skyLightHeight = maxPosY;
                while (true) {
                    if (skyLightHeight > 0) {
                        val posY = skyLightHeight - 1;
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                        if (blockOpacity == 0) {
                            skyLightHeight--;
                            continue;
                        }

                        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, skyLightHeight);
                        minSkyLightHeight = Math.min(minSkyLightHeight, skyLightHeight);
                    }

                    subChunkPosZ++;
                    break;
                }
            }
        }

        chunk.lumi$minSkyLightHeight(minSkyLightHeight);
        chunk.lumi$isLightingInitialized(true);
    }

    @Override
    public void handleSubChunkInit(@NotNull LumiChunk chunk, @NotNull LumiSubChunk subChunk) {
        if (!worldRoot.lumi$hasSky())
            return;

        acquireLock();
        try {
            val maxPosY = subChunk.lumi$root().lumi$posY() + 15;
            val lightValue = SKY_LIGHT_TYPE.defaultLightValue();
            for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                    if (chunk.lumi$canBlockSeeSky(subChunkPosX, maxPosY, subChunkPosZ)) {
                        for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                            subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
            }
        } finally {
            releaseLock();
        }
        chunk.lumi$root().lumi$markDirty();
    }

    @Override
    public void handleChunkLoad(@NotNull LumiChunk chunk) {
        PhosporUtil.scheduleRelightChecksForChunkBoundaries(world, chunk);
    }

    @Override
    public void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk) {
        val chunkRoot = chunk.lumi$root();

        var queuedRandomLightUpdates = chunk.lumi$queuedRandomLightUpdates();
        if (queuedRandomLightUpdates >= MAX_QUEUED_RANDOM_LIGHT_UPDATES)
            return;

        val isUpdating = chunkRoot.lumi$isUpdating();
        val isClientSide = worldRoot.lumi$isClientSide();

        final int maxUpdateIterations;
        if (isClientSide && isUpdating) {
            maxUpdateIterations = 256;
        } else if (isClientSide) {
            maxUpdateIterations = 64;
        } else {
            maxUpdateIterations = 32;
        }

        val chunkPosX = chunk.lumi$chunkPosX();
        val chunkPosZ = chunk.lumi$chunkPosZ();
        val minPosX = chunkPosX << 4;
        val minPosZ = chunkPosZ << 4;

        var remainingIterations = maxUpdateIterations;
        while (remainingIterations > 0) {
            if (queuedRandomLightUpdates >= MAX_QUEUED_RANDOM_LIGHT_UPDATES)
                return;
            remainingIterations--;

            val chunkPosY = queuedRandomLightUpdates % 16;
            val subChunkPosX = (queuedRandomLightUpdates / 16) % 16;
            val subChunkPosZ = queuedRandomLightUpdates / (16 * 16);
            queuedRandomLightUpdates++;

            val minPosY = chunkPosY << 4;

            val posX = minPosX + subChunkPosX;
            val posZ = minPosZ + subChunkPosZ;


            if (!chunkRoot.lumi$isSubChunkPrepared(chunkPosY))
                continue;

            for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                val posY = minPosY + subChunkPosY;

                notCornerCheck:
                {
                    if (subChunkPosX != 0 && subChunkPosX != 15)
                        break notCornerCheck;
                    if (subChunkPosY != 0 && subChunkPosY != 15)
                        break notCornerCheck;
                    if (subChunkPosZ != 0 && subChunkPosZ != 15)
                        break notCornerCheck;

                    worldRoot.lumi$scheduleLightingUpdate(posX, posY, posZ);
                    continue;
                }

                renderUpdateCheck:
                {
                    val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                    if (blockOpacity < 15)
                        break renderUpdateCheck;

                    val blockBrightness = chunk.lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
                    if (blockBrightness > 0)
                        break renderUpdateCheck;

                    val lightValue = chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
                    if (lightValue == 0)
                        continue;

                    chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, 0);
                    worldRoot.lumi$markBlockForRenderUpdate(posX, posY, posZ);
                    break;
                }

                worldRoot.lumi$scheduleLightingUpdate(posX, posY, posZ);
                break;
            }

        }
    }

    @Override
    public void updateLightingForBlock(@NotNull BlockPos blockPos) {
        updateLightingForBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public void updateLightingForBlock(int posX, int posY, int posZ) {
        val chunk = world.lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk == null)
            return;

        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        var maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ) & 255;
        var minPosY = Math.max((posY + 1) & 255, maxPosY);

        if (!chunk.lumi$canBlockSeeSky(subChunkPosX, minPosY, subChunkPosZ))
            return;

        while (minPosY > 0 && chunk.lumi$getBlockOpacity(subChunkPosX, minPosY - 1, subChunkPosZ) == 0)
            --minPosY;
        if (minPosY == maxPosY)
            return;

        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, minPosY);

        if (worldRoot.lumi$hasSky())
            PhosporUtil.relightSkyLightColumn(this,
                                              world,
                                              chunk,
                                              subChunkPosX,
                                              subChunkPosZ,
                                              maxPosY,
                                              minPosY);

        maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);
        if (maxPosY < chunk.lumi$minSkyLightHeight())
            chunk.lumi$minSkyLightHeight(maxPosY);

        chunk.lumi$root().lumi$markDirty();
    }

    @Override
    public void scheduleLightingUpdateForRange(@NotNull LightType lightType, @NotNull BlockPos minBlockPos, @NotNull BlockPos maxBlockPos) {
        val minPosX = minBlockPos.getX();
        val maxPosX = maxBlockPos.getX();
        if (maxPosX < minPosX)
            return;
        val minPosY = minBlockPos.getY();
        val maxPosY = maxBlockPos.getY();
        if (maxPosY < minPosY)
            return;
        val minPosZ = minBlockPos.getZ();
        val maxPosZ = maxBlockPos.getZ();
        if (maxPosZ < minPosZ)
            return;

        acquireLock();
        try {
            for (var posY = minPosY; posY < maxPosY; posY++) {
                for (var posZ = minPosZ; posZ < maxPosZ; posZ++) {
                    for (var posX = minPosX; posX < maxPosX; posX++) {
                        scheduleLightingUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
                    }
                }
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                               int minPosX,
                                               int minPosY,
                                               int minPosZ,
                                               int maxPosX,
                                               int maxPosY,
                                               int maxPosZ) {
        if (maxPosX < minPosX)
            return;
        if (maxPosY < minPosY)
            return;
        if (maxPosZ < minPosZ)
            return;

        acquireLock();
        try {
            for (var posY = minPosY; posY < maxPosY; posY++) {
                for (var posZ = minPosZ; posZ < maxPosZ; posZ++) {
                    for (var posX = minPosX; posX < maxPosX; posX++) {
                        scheduleLightingUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
                    }
                }
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ) {
        acquireLock();
        try {
            for (var posY = 0; posY < 255; posY++)
                scheduleLightingUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int minPosY, int maxPosY) {
        if (maxPosY < minPosY)
            return;
        acquireLock();
        try {
            for (var posY = minPosY; posY < maxPosY; posY++)
                scheduleLightingUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos) {
        acquireLock();
        try {
            scheduleLightingUpdate(lightType, posLongFromBlockPos(blockPos));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ) {
        acquireLock();
        try {
            scheduleLightingUpdate(lightType, posLongFromPosXYZ(posX, posY, posZ));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void processLightingUpdatesForType(@NotNull LightType lightType) {
        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (worldRoot.lumi$isClientSide() && !isCallingFromClientThread())
            return;

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

    @Override
    public void processLightingUpdatesForAllTypes() {
        for (val lightType : values())
            processLightingUpdatesForType(lightType);

        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (worldRoot.lumi$isClientSide() && !isCallingFromClientThread())
            return;

        // Quickly check if the queue is empty before we acquire a more expensive lock.
        hasQueuedUpdatesCheck:
        {
            for (val lightType : values()) {
                val queue = updateQueues[lightType.ordinal()];
                if (!queue.isEmpty())
                    break hasQueuedUpdatesCheck;
            }
            return;
        }

        acquireLock();
        try {
            for (val lightType : values()) {
                val queue = updateQueues[lightType.ordinal()];
                processLightUpdateQueue(lightType, queue);
            }
        } finally {
            releaseLock();
        }
    }

    private void scheduleLightingUpdate(LightType lightType, long posLong) {
        val queue = updateQueues[lightType.ordinal()];
        if (queue.size() >= MAX_SCHEDULED_UPDATES)
            processLightingUpdatesForType(lightType);

        queue.add(posLong);
    }

    @SideOnly(Side.CLIENT)
    private boolean isCallingFromClientThread() {
        return Minecraft.getMinecraft().func_152345_ab();
    }

    private void acquireLock() {
        if (lock.tryLock())
            return;

        // If we cannot lock, something has gone wrong... Only one thread should ever acquire the lock.
        // Validate that we're on the right thread immediately so we can gather information.
        // It is NEVER valid to call World methods from a thread other than the owning thread of the world instance.
        // Users can safely disable this warning, however it will not resolve the issue.
        if (ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS) {
            val currentThread = Thread.currentThread();

            if (currentThread != updateThread) {
                val e = new IllegalAccessException(String.format("World is owned by '%s' (ID: %s)," +
                                                                 " but was accessed from thread '%s' (ID: %s)",
                                                                 updateThread.getName(),
                                                                 updateThread.getId(),
                                                                 currentThread.getName(),
                                                                 currentThread.getId()));

                LOG.error("Something (likely another mod) has attempted to modify the world's state from the wrong thread!\n" +
                          "This is *bad practice* and can cause severe issues in your game.\n" +
                          "Phosphor has done as best as it can to mitigate this violation, but it may negatively impact performance or introduce stalls.\n" +
                          "You should report this issue to our issue tracker with the following stacktrace information.\n" +
                          "(If you are aware you have misbehaving mods and cannot resolve this issue, you can safely disable this warning by setting" +
                          " `enable_illegal_thread_access_warnings` to `false` in LUMINAS's configuration file for the time being.)", e);

            }

        }

        // Wait for the lock to be released. This will likely introduce unwanted stalls, but will mitigate the issue.
        lock.lock();
    }

    private void releaseLock() {
        lock.unlock();
    }

    private void processLightUpdateQueue(LightType lightType, PooledLongQueue queue) {
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
                val posLong = cursorData & BLOCK_POS_BIT_MASK;
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
        // Iterate through enqueued updates (brightening and darkening in parallel)
        // from brightest to darkest so that we only need to iterate once
        for (var queueIndex = MAX_LIGHT_VALUE; queueIndex >= 0; queueIndex--) {
            profiler.startSection("darkening");

            queueIterator = darkeningQueues[queueIndex].iterator();
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
            queueIterator = brighteningQueues[queueIndex].iterator();
            while (nextItem()) {
                // Only process this if nothing else has happened at this position since scheduling
                if (getCursorCurrentLightValue(lightType) == queueIndex) {
                    val posX = cursorBlockPos.getX();
                    val posY = cursorBlockPos.getY();
                    val posZ = cursorBlockPos.getZ();
                    worldRoot.lumi$markBlockForRenderUpdate(posX, posY, posZ);
                    if (queueIndex > 1)
                        spreadLightFromCursor(queueIndex, lightType);
                }
            }
            profiler.endSection();
        }

        profiler.endSection();
        isUpdating = false;
    }

    private void updateNeighborBlocks(LightType lightType) {
        if (areNeighboursBlocksValid)
            return;
        areNeighboursBlocksValid = true;

        for (var i = 0; i < NEIGHBOUR_COUNT; ++i) {
            val neighbor = neighborBlocks[i];

            neighbor.posLong = cursorData + BLOCK_SIDE_BIT_OFFSET[i];
            if ((neighbor.posLong & POS_OVERFLOW_CHECK_BIT_MASK) != 0) {
                neighbor.chunk = null;
                neighbor.subChunk = null;
                continue;
            }

            blockPosFromPosLong(neighbor.blockPos, neighbor.posLong);
            if ((neighbor.posLong & BLOCK_POS_CHUNK_BIT_MASK) == cursorChunkPosLong) {
                neighbor.chunk = cursorChunk;
            } else {
                neighbor.chunk = getChunk(neighbor.blockPos);
            }

            if (neighbor.chunk == null)
                continue;

            val chunkPosY = neighbor.blockPos.getY() / 16;
            neighbor.subChunk = neighbor.chunk.lumi$getSubChunkIfPrepared(chunkPosY);
            neighbor.lightValue = getCachedLightFor(neighbor.chunk, neighbor.subChunk, lightType, neighbor.blockPos);
        }
    }

    private static int getCachedLightFor(LumiChunk chunk,
                                         @Nullable LumiSubChunk subChunk,
                                         LightType lightType,
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

    private int getCursorUpdatedLightValue(LightType lightType) {
        if (cursorChunk == null)
            return lightType.defaultLightValue();

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

    private int getCursorUpdatedLightValue(int cursorBlockLightValue, int cursorBlockOpacity, LightType lightType) {
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

    private void spreadLightFromCursor(int cursorLightValue, LightType lightType) {
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

    private void enqueueBrighteningFromCursor(int lightValue, LightType lightType) {
        if (cursorChunk != null)
            enqueueBrightening(cursorBlockPos, cursorData, lightValue, cursorChunk, lightType);
    }

    private void enqueueBrightening(BlockPos blockPos,
                                    long posLong,
                                    int lightValue,
                                    @Nullable LumiChunk chunk,
                                    LightType lightType) {
        if (chunk == null)
            return;

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        brighteningQueues[lightValue].add(posLong);
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, lightValue);
        chunk.lumi$root().lumi$markDirty();
    }

    private void enqueueDarkening(BlockPos blockPos,
                                  long posLong,
                                  int oldLightValue,
                                  @Nullable LumiChunk chunk,
                                  LightType lightType) {
        if (chunk == null)
            return;

        val posY = blockPos.getY();
        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        darkeningQueues[oldLightValue].add(posLong);
        chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, MIN_LIGHT_VALUE);
        chunk.lumi$root().lumi$markDirty();
    }

    private static long posLongFromBlockPos(BlockPos blockPos) {
        return posLongFromPosXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private static long posLongFromPosXYZ(int posX, int posY, int posZ) {
        // The additional logic is needed as the X and Z may be negative, and this preserves the sign value.
        return ((long) posX + (1L << POS_X_BIT_LENGTH - 1L) << POS_X_BIT_SHIFT) |
               ((long) posY << POS_Y_BIT_SHIFT) |
               ((long) posZ + (1L << POS_Z_BIT_LENGTH - 1L) << POS_Z_BIT_SHIFT);
    }

    private static void blockPosFromPosLong(BlockPos.MutableBlockPos blockPos, long longPos) {
        // The additional logic is needed as the X and Z may be negative, and this preserves the sign value.
        val posX = (int) (longPos >> POS_X_BIT_SHIFT & POS_X_BIT_MASK) - (1L << POS_X_BIT_LENGTH - 1L);
        val posY = (int) (longPos >> POS_Y_BIT_SHIFT & POS_Y_BIT_MASK);
        val posZ = (int) (longPos >> POS_Z_BIT_SHIFT & POS_Z_BIT_MASK) - (1L << POS_Z_BIT_LENGTH - 1L);
        blockPos.setPos(posX, posY, posZ);
    }

    private boolean nextItem() {
        if (queueIterator == null)
            return false;

        if (!queueIterator.hasNext()) {
            queueIterator = null;
            return false;
        }

        cursorData = queueIterator.next();
        areNeighboursBlocksValid = false;

        blockPosFromPosLong(cursorBlockPos, cursorData);

        val chunkPosLong = cursorData & BLOCK_POS_CHUNK_BIT_MASK;
        if (cursorChunkPosLong != chunkPosLong) {
            cursorChunk = getChunk(cursorBlockPos);
            cursorChunkPosLong = chunkPosLong;
        }

        return true;
    }

    private int getCursorCurrentLightValue(LightType lightType) {
        if (cursorChunk == null)
            return lightType.defaultLightValue();

        val posY = cursorBlockPos.getY();
        val subChunkPosX = cursorBlockPos.getX() & 15;
        val subChunkPosZ = cursorBlockPos.getZ() & 15;
        return cursorChunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    private int getCursorBlockLightValue(Block cursorBlock, int cursorBlockMeta, LightType lightType) {
        val posX = cursorBlockPos.getX();
        val posY = cursorBlockPos.getY();
        val posZ = cursorBlockPos.getZ();

        if (lightType == SKY_LIGHT_TYPE) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            if (cursorChunk != null && cursorChunk.lumi$canBlockSeeSky(subChunkPosX, posY, subChunkPosZ)) {
                return SKY_LIGHT_TYPE.defaultLightValue();
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

    private @Nullable LumiChunk getChunk(BlockPos blockPos) {
        val chunkPosX = blockPos.getX() >> 4;
        val chunkPosZ = blockPos.getZ() >> 4;
        return getLoadedChunk(world, chunkPosX, chunkPosZ);
    }

    public static Block getBlockFromChunk(@Nullable LumiChunk chunk, BlockPos blockPos) {
        if (chunk == null)
            return Blocks.air;

        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
        return getBlockFromSubChunk(subChunk, blockPos);
    }

    public static Block getBlockFromSubChunk(@Nullable LumiSubChunk subChunk, BlockPos blockPos) {
        if (subChunk == null)
            return Blocks.air;

        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosY = blockPos.getY() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        return subChunk.lumi$root().lumi$getBlock(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    public static int getBlockMetaFromChunk(@Nullable LumiChunk chunk, BlockPos blockPos) {
        if (chunk == null)
            return 0;

        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
        return getBlockMetaFromSubChunk(subChunk, blockPos);
    }

    public static int getBlockMetaFromSubChunk(@Nullable LumiSubChunk subChunk, BlockPos blockPos) {
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

        private long posLong = 0L;

        private @Nullable LumiChunk chunk = null;
        private @Nullable LumiSubChunk subChunk = null;

        private int lightValue = 0;
    }
}
