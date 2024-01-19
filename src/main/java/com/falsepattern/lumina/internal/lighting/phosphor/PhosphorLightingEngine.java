/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.config.LumiConfig;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.falsepattern.lumina.api.chunk.LumiChunk.MAX_QUEUED_RANDOM_LIGHT_UPDATES;
import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;
import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.lighting.phosphor.Direction.VALID_DIRECTIONS;
import static com.falsepattern.lumina.internal.lighting.phosphor.Direction.VALID_DIRECTIONS_SIZE;
import static com.falsepattern.lumina.internal.lighting.phosphor.DummyLock.getDummyLock;
import static com.falsepattern.lumina.internal.lighting.phosphor.PhosphorUtil.*;
import static cpw.mods.fml.relauncher.Side.CLIENT;


public final class PhosphorLightingEngine implements LumiLightingEngine {
    private static final Logger LOG = createLogger("Phosphor");

    /**
     * Maximum scheduled lighting updates before processing the updates is forced.
     */
    private static final int MAX_SCHEDULED_BLOCK_LIGHT_UPDATES_SERVER = 1 << 18;
    private static final int MAX_SCHEDULED_SKY_LIGHT_UPDATES_SERVER = 1 << 18;
    private static final int MAX_SCHEDULED_BLOCK_LIGHT_UPDATES_CLIENT = 1 << 10;
    private static final int MAX_SCHEDULED_SKY_LIGHT_UPDATES_CLIENT = 1 << 10;

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

    private static final int NEIGHBOUR_COUNT = VALID_DIRECTIONS_SIZE;

    private static final long[] BLOCK_SIDE_BIT_OFFSET;

    static {
        BLOCK_SIDE_BIT_OFFSET = new long[NEIGHBOUR_COUNT];
        for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
            val direction = VALID_DIRECTIONS[i];
            BLOCK_SIDE_BIT_OFFSET[i] = ((long) direction.xOffset << POS_X_BIT_SHIFT) |
                                       ((long) direction.yOffset << POS_Y_BIT_SHIFT) |
                                       ((long) direction.zOffset << POS_Z_BIT_SHIFT);
        }
    }

    private final Thread updateThread = Thread.currentThread();
    private final Lock lock;

    private final LumiWorld world;
    private final LumiWorldRoot worldRoot;
    private final boolean isClientSide;
    private final Profiler profiler;

    private final int maxBlockLightUpdates;
    private final int maxSkyLightUpdates;

    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final LongList blockLightUpdateQueue;
    private final LongList skyLightUpdateQueue;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final LongList[] brighteningQueues;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final LongList[] darkeningQueues;
    /**
     * Layout of longs: [newLight(4)] [y(8)] [x(26)] [z(26)]
     */
    private final LongList initialBrighteningQueue;
    /**
     * Layout of longs: [padding(4)] [y(8)] [x(26)] [z(26)]
     */
    private final LongList initialDarkeningQueue;

    private @Nullable LightType currentLightType;
    private @Nullable LongList currentQueue;
    private int currentQueueSize;
    private int currentQueueIndex;

    private final BlockReference cursor;

    private final BlockReference[] neighbors;
    private boolean areNeighboursBlocksValid;

    PhosphorLightingEngine(LumiWorld world, Profiler profiler) {
        this.lock = LumiConfig.ENABLE_LOCKS ? new ReentrantLock() : getDummyLock();

        this.world = world;
        this.worldRoot = world.lumi$root();
        this.isClientSide = worldRoot.lumi$isClientSide();
        this.profiler = profiler;

        this.maxBlockLightUpdates = isClientSide ? MAX_SCHEDULED_BLOCK_LIGHT_UPDATES_CLIENT : MAX_SCHEDULED_BLOCK_LIGHT_UPDATES_SERVER;
        this.maxSkyLightUpdates = isClientSide ? MAX_SCHEDULED_SKY_LIGHT_UPDATES_CLIENT : MAX_SCHEDULED_SKY_LIGHT_UPDATES_SERVER;

        this.blockLightUpdateQueue = new LongArrayList(maxBlockLightUpdates);
        this.skyLightUpdateQueue = new LongArrayList(maxSkyLightUpdates);

        this.brighteningQueues = new LongArrayList[LIGHT_VALUE_RANGE];
        for (var i = 0; i < LIGHT_VALUE_RANGE; i++)
            this.brighteningQueues[i] = new LongArrayList();
        this.darkeningQueues = new LongArrayList[LIGHT_VALUE_RANGE];
        for (var i = 0; i < LIGHT_VALUE_RANGE; i++)
            this.darkeningQueues[i] = new LongArrayList();
        this.initialBrighteningQueue = new LongArrayList();
        this.initialDarkeningQueue = new LongArrayList();

        this.neighbors = new BlockReference[NEIGHBOUR_COUNT];
        for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
            val neighbor = new BlockReference();
            neighbor.neighbourBlockSideBitOffset = BLOCK_SIDE_BIT_OFFSET[i];
            neighbors[i] = neighbor;
        }
        this.areNeighboursBlocksValid = false;

        this.cursor = new BlockReference();

        this.currentLightType = null;
        this.currentQueue = null;
        this.currentQueueSize = 0;
        this.currentQueueIndex = 0;
    }

    @Override
    public @NotNull String lightingEngineID() {
        return "phosphor";
    }

    // region Data
    @Override
    public void writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output) {
        writeNeighborLightChecksToNBT(chunk, output);
    }

    @Override
    public void readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input) {
        readNeighborLightChecksFromNBT(chunk, input);
    }

    @Override
    public void cloneChunk(@NotNull LumiChunk from, @NotNull LumiChunk to) {
        cloneNeighborLightChecks(from, to);
    }

    @Override
    public void writeSubChunkToNBT(@NotNull LumiChunk chunk,
                                   @NotNull LumiSubChunk subChunk,
                                   @NotNull NBTTagCompound output) {
    }

    @Override
    public void readSubChunkFromNBT(@NotNull LumiChunk chunk,
                                    @NotNull LumiSubChunk subChunk,
                                    @NotNull NBTTagCompound input) {
    }

    @Override
    public void cloneSubChunk(@NotNull LumiChunk fromChunk, @NotNull LumiSubChunk from, @NotNull LumiSubChunk to) {
    }

    @Override
    public void writeChunkToPacket(@NotNull LumiChunk chunk,
                                   @NotNull ByteBuffer output) {
    }

    @Override
    public void readChunkFromPacket(@NotNull LumiChunk chunk,
                                    @NotNull ByteBuffer input) {
    }

    @Override
    public void writeSubChunkToPacket(@NotNull LumiChunk chunk,
                                      @NotNull LumiSubChunk subChunk,
                                      @NotNull ByteBuffer input) {

    }

    @Override
    public void readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                       @NotNull LumiSubChunk subChunk,
                                       @NotNull ByteBuffer output) {
    }
    // endregion

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos) {
        return getCurrentLightValue(lightType, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private static final String MARKER = "$LUMI_NO_RELIGHT";

    private static final ThreadLocal<Boolean> THREAD_ALLOWED_TO_RELIGHT = ThreadLocal.withInitial(() -> {
        val t = Thread.currentThread();
        val name = t.getName();
        boolean allow = true;
        if (name.startsWith(MARKER)) {
            allow = false;
            t.setName(name.substring(MARKER.length()));
        }
        return allow;
    });

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        if (THREAD_ALLOWED_TO_RELIGHT.get()) {
            processLightingUpdatesForType(lightType);
        }
        return clampLightValue(world.lumi$getLightValue(lightType, posX, posY, posZ));
    }

    @Override
    public int getCurrentLightValueChunk(@NotNull Chunk chunk, @NotNull LightType lightType, int chunkPosX, int posY, int chunkPosZ) {
        if (THREAD_ALLOWED_TO_RELIGHT.get()) {
            processLightingUpdatesForType(lightType);
        }
        return clampLightValue(world.lumi$getLightValue(world.lumi$wrap(chunk), lightType, chunkPosX, posY, chunkPosZ));
    }

    @Override
    public boolean isChunkFullyLit(@NotNull LumiChunk chunk) {
        return PhosphorUtil.isChunkFullyLit(world, chunk, profiler);
    }

    @Override
    public void handleChunkInit(@NotNull LumiChunk chunk) {
        chunk.lumi$isLightingInitialized(false);

        val hasSky = worldRoot.lumi$hasSky();

        val chunkRoot = chunk.lumi$root();

        val basePosX = chunk.lumi$chunkPosX() << 4;
        val basePosY = chunkRoot.lumi$topPreparedSubChunkBasePosY();
        val basePosZ = chunk.lumi$chunkPosZ() << 4;

        val maxPosY = basePosY + 16;

        var minSkyLightHeight = Integer.MAX_VALUE;
        for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
            int subChunkPosZ = 0;
            while (subChunkPosZ < 16) {
                var skyLightHeight = maxPosY;

                while (true) {
                    if (skyLightHeight > 0) {
                        val posY = skyLightHeight - 1;
                        // Will use Fast-Path in LUMINA & RPLE
                        val blockOpacity = clampSkyLightOpacity(
                                chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ));
                        if (blockOpacity == MIN_SKY_LIGHT_OPACITY) {
                            skyLightHeight--;
                            continue;
                        }

                        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, skyLightHeight);
                        minSkyLightHeight = Math.min(minSkyLightHeight, skyLightHeight);
                    }

                    if (hasSky) {
                        var lightLevel = MAX_LIGHT_VALUE;
                        skyLightHeight = (basePosY + 16) - 1;

                        do {
                            // Will use Fast-Path in LUMINA & RPLE
                            var blockOpacity = clampSkyLightOpacity(
                                    chunk.lumi$getBlockOpacity(subChunkPosX, skyLightHeight, subChunkPosZ));
                            if (blockOpacity == MIN_SKY_LIGHT_OPACITY && lightLevel != MAX_LIGHT_VALUE)
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
                        while (skyLightHeight > 0 && lightLevel > MIN_LIGHT_VALUE);
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
                        val block = chunkRoot.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
                        val blockMeta = chunkRoot.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
                        final int blockOpacity;
                        if (block == Blocks.air) {
                            blockOpacity = MIN_SKY_LIGHT_OPACITY;
                        } else {
                            blockOpacity = clampSkyLightOpacity(chunk.lumi$getBlockOpacity(block, blockMeta,
                                                                                           subChunkPosX,
                                                                                           posY,
                                                                                           subChunkPosZ));
                        }
                        if (blockOpacity == MIN_SKY_LIGHT_OPACITY) {
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
        chunk.lumi$root().lumi$markDirty();
    }

    @Override
    public void handleChunkLoad(@NotNull LumiChunk chunk) {
        if (scheduleRelightChecksForChunkBoundaries(world, chunk))
            processLightingUpdatesForType(SKY_LIGHT_TYPE);
    }

    @Override
    public void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk) {
        val chunkRoot = chunk.lumi$root();

        var queuedRandomLightUpdates = chunk.lumi$queuedRandomLightUpdates();
        if (queuedRandomLightUpdates >= MAX_QUEUED_RANDOM_LIGHT_UPDATES)
            return;

        final int maxUpdateIterations;
        if (isClientSide) {
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

            try {
                acquireLock();
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

                        scheduleLightingUpdatePostLock(BLOCK_LIGHT_TYPE, posX, posY, posZ);
                        if (worldRoot.lumi$hasSky())
                            scheduleLightingUpdatePostLock(SKY_LIGHT_TYPE, posX, posY, posZ);
                        continue;
                    }

                    renderUpdateCheck:
                    {
                        val blockOpacity = clampBlockLightOpacity(world.lumi$getBlockOpacity(posX, posY, posZ));
                        if (blockOpacity < MAX_BLOCK_LIGHT_OPACITY)
                            break renderUpdateCheck;

                        val blockBrightness = clampLightValue(world.lumi$getBlockBrightness(posX, posY, posZ));
                        if (blockBrightness > MIN_LIGHT_VALUE)
                            break renderUpdateCheck;

                        val lightValue = clampLightValue(world.lumi$getBlockLightValue(posX, posY, posZ));
                        if (lightValue == MIN_LIGHT_VALUE)
                            continue;

                        chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, 0);
                        worldRoot.lumi$markBlockForRenderUpdate(posX, posY, posZ);
                        break;
                    }

                    scheduleLightingUpdatePostLock(BLOCK_LIGHT_TYPE, posX, posY, posZ);
                    if (worldRoot.lumi$hasSky())
                        scheduleLightingUpdatePostLock(SKY_LIGHT_TYPE, posX, posY, posZ);
                    break;
                }
            } finally {
                releaseLock();
            }

        }
        chunk.lumi$queuedRandomLightUpdates(queuedRandomLightUpdates);
    }

    @Override
    public void updateLightingForBlock(@NotNull BlockPos blockPos) {
        updateLightingForBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public void updateLightingForBlock(int posX, int posY, int posZ) {
        val chunkPosX = posX >> 4;
        val chunkPosZ = posZ >> 4;
        val chunk = world.lumi$getChunkFromChunkPosIfExists(chunkPosX, chunkPosZ);
        if (chunk == null)
            return;

        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        var maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ) & 255;
        var minPosY = Math.max((posY + 1) & 255, maxPosY);

        if (!chunk.lumi$canBlockSeeSky(subChunkPosX, minPosY, subChunkPosZ))
            return;

        val chunkRoot = chunk.lumi$root();
        while (minPosY > 0) {
            val block = chunkRoot.lumi$getBlock(subChunkPosX, minPosY - 1, subChunkPosZ);
            val blockMeta = chunkRoot.lumi$getBlockMeta(subChunkPosX, minPosY - 1, subChunkPosZ);
            if (world.lumi$getBlockOpacity(block, blockMeta, posX, minPosY - 1, posZ) != MIN_SKY_LIGHT_OPACITY)
                break;
            minPosY--;
        }

        if (minPosY == maxPosY)
            return;

        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, minPosY);

        if (worldRoot.lumi$hasSky())
            relightSkyLightColumn(this,
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
                        scheduleLightingUpdatePostLock(lightType, posX, posY, posZ);
                    }
                }
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ) {
        scheduleLightingUpdateForColumn(lightType, posX, posZ, 0, 255);
    }

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int minPosY, int maxPosY) {
        if (maxPosY < minPosY)
            return;
        acquireLock();
        try {
            for (var posY = minPosY; posY < maxPosY; posY++)
                scheduleLightingUpdatePostLock(lightType, posX, posY, posZ);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos) {
        acquireLock();
        try {
            scheduleLightingUpdatePostLock(lightType, posLongFromBlockPos(blockPos));
        } finally {
            releaseLock();
        }
    }

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ) {
        acquireLock();
        try {
            scheduleLightingUpdatePostLock(lightType, posX, posY, posZ);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void processLightingUpdatesForType(@NotNull LightType lightType) {
        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (isClientSide && !isCallingFromClientThread())
            return;

        // Quickly check if the queue is empty before we acquire a more expensive lock.
        val queue = lightType.isBlock() ? blockLightUpdateQueue : skyLightUpdateQueue;
        if (queue.isEmpty())
            return;

        acquireLock();
        try {
            updateLighting(lightType);
            resetBlockReferences();
        } finally {
            releaseLock();
        }
    }

    @Override
    public void processLightingUpdatesForAllTypes() {
        // We only want to perform updates if we're being called from a tick event on the client
        // There are many locations in the client code which will end up making calls to this method, usually from
        // other threads.
        if (isClientSide && !isCallingFromClientThread())
            return;

        val hasBlockLightUpdates = !blockLightUpdateQueue.isEmpty();
        val hasSkyLightUpdates = !skyLightUpdateQueue.isEmpty();

        // Quickly check if the queue is empty before we acquire a more expensive lock.
        if (!(hasBlockLightUpdates || hasSkyLightUpdates))
            return;

        acquireLock();
        try {
            if (hasBlockLightUpdates)
                updateLighting(BLOCK_LIGHT_TYPE);
            if (hasSkyLightUpdates)
                updateLighting(SKY_LIGHT_TYPE);
            resetBlockReferences();
        } finally {
            releaseLock();
        }
    }

    private void resetBlockReferences() {
        cursor.reset();
        for (var i = 0; i < NEIGHBOUR_COUNT; i++)
            neighbors[i].reset();
    }

    private void scheduleLightingUpdate(LightType lightType, long posLong) {
        acquireLock();
        try {
            scheduleLightingUpdatePostLock(lightType, posLong);
        } finally {
            releaseLock();
        }
    }

    public void scheduleLightingUpdatePostLock(@NotNull LightType lightType, int posX, int posY, int posZ) {
        scheduleLightingUpdatePostLock(lightType, posLongFromPosXYZ(posX, posY, posZ));
    }

    private void scheduleLightingUpdatePostLock(LightType lightType, long posLong) {
        final int maxLightUpdates;
        final LongList queue;
        if (lightType.isBlock()) {
            maxLightUpdates = maxBlockLightUpdates;
            queue = blockLightUpdateQueue;
        } else {
            maxLightUpdates = maxSkyLightUpdates;
            queue = skyLightUpdateQueue;
        }

        if (queue.size() >= maxLightUpdates)
            processLightingUpdatesForType(lightType);

        queue.add(posLong);
    }

    @SideOnly(CLIENT)
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
        if (LumiConfig.ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS) {
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

    private void updateLighting(LightType lightType) {
        currentLightType = lightType;

        profiler.startSection("lighting");

        profiler.startSection("checking");
        processUpdateQueue();
        processInitialDarkening();
        processInitialBrightening();
        profiler.endSection();

        // Iterate through enqueued updates (brightening and darkening in parallel)
        // from brightest to darkest so that we only need to iterate once
        for (var queueIndex = MAX_LIGHT_VALUE; queueIndex >= 0; queueIndex--) {
            profiler.startSection("darkening");
            processDarkeningQueue(queueIndex);
            profiler.endStartSection("brightening");
            processBrighteningQueue(queueIndex);
            profiler.endSection();
        }

        cursor.isValid = false;
        profiler.endSection();
    }

    private void processUpdateQueue() {
        assert currentLightType != null;

        // Process the queued updates and enqueue them for further processing
        val updateQueue = currentLightType.isBlock() ? blockLightUpdateQueue : skyLightUpdateQueue;
        setQueue(updateQueue);
        while (nextItem()) {
            val cursorUpdatedLightValue = getCursorUpdatedLightValue();
            if (cursor.lightValue < cursorUpdatedLightValue) {
                // Don't enqueue directly for brightening in order to avoid duplicate scheduling
                val newData = ((long) cursorUpdatedLightValue << LIGHT_VALUE_BIT_SHIFT) | cursor.data;
                initialBrighteningQueue.add(newData);
            } else if (cursor.lightValue > cursorUpdatedLightValue) {
                // Don't enqueue directly for darkening in order to avoid duplicate scheduling
                initialDarkeningQueue.add(cursor.data);
            }
        }
    }

    private void processInitialDarkening() {
        setQueue(initialBrighteningQueue);
        while (nextItem()) {
            // Sets the light to newLight to only schedule once. Clear leading bits of curData for later
            val cursorDataLightValue = (int) (cursor.data >> LIGHT_VALUE_BIT_SHIFT & LIGHT_VALUE_BIT_MASK);
            if (cursorDataLightValue > cursor.lightValue) {
                val posLong = cursor.data & BLOCK_POS_BIT_MASK;
                enqueueBrightening(cursor.blockPos, posLong, cursorDataLightValue, cursor.chunk);
                cursor.setLightValue(cursorDataLightValue);
            }
        }
    }

    private void processInitialBrightening() {
        setQueue(initialDarkeningQueue);
        while (nextItem()) {
            // Sets the light to 0 to only schedule once
            if (cursor.lightValue != MIN_LIGHT_VALUE) {
                enqueueDarkening(cursor.blockPos, cursor.data, cursor.lightValue, cursor.chunk);
                cursor.setLightValue(MIN_LIGHT_VALUE);
            }
        }
    }

    private void processDarkeningQueue(int queueIndex) {
        setQueue(darkeningQueues[queueIndex]);
        while (nextItem()) {
            // Don't darken if we got brighter due to some other change
            if (cursor.lightValue >= queueIndex)
                continue;

            // If luminosity is high enough, opacity is irrelevant
            final int cursorBlockOpacity;
            if (cursor.brightnessValue >= MAX_LIGHT_VALUE - 1) {
                cursorBlockOpacity = MIN_BLOCK_LIGHT_OPACITY;
            } else {
                cursorBlockOpacity = cursor.opacityValue;
            }

            // Only darken neighbors if we indeed became darker
            // If we didn't become darker, so we need to re-set our initial light value (was set to 0) and notify neighbors
            if (getCursorUpdatedLightValue(cursor.brightnessValue, cursorBlockOpacity) >= queueIndex) {
                // Do not spread to neighbors immediately to avoid scheduling multiple times
                enqueueBrighteningFromCursor(queueIndex);
                continue;
            }

            // Need to calculate new light value from neighbors IGNORING neighbors which are scheduled for darkening
            var newLightValue = cursor.brightnessValue;
            updateNeighborBlocks();
            for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
                val neighbor = neighbors[i];
                if (!neighbor.isValid)
                    continue;
                if (neighbor.lightValue == MIN_LIGHT_VALUE)
                    continue;

                // If we can't darken the neighbor, no one else can (because of processing order) -> safe to let us be illuminated by it
                if (queueIndex - neighbor.opacityValue >= neighbor.lightValue) {
                    // Schedule neighbor for darkening if we possibly light it
                    enqueueDarkening(neighbor.blockPos, neighbor.data, neighbor.lightValue, neighbor.chunk);
                } else {
                    // Only use for new light calculation if not
                    newLightValue = Math.max(newLightValue, neighbor.lightValue - cursorBlockOpacity);
                }
            }

            // Schedule brightening since light level was set to 0
            enqueueBrighteningFromCursor(newLightValue);
        }
    }

    private void processBrighteningQueue(int queueIndex) {
        setQueue(brighteningQueues[queueIndex]);
        while (nextItem()) {
            // Only process this if nothing else has happened at this position since scheduling
            if (cursor.lightValue == queueIndex) {
                worldRoot.lumi$markBlockForRenderUpdate(cursor.posX, cursor.posY, cursor.posZ);
                if (queueIndex > 1)
                    spreadLightFromCursor(queueIndex);
            }
        }
    }

    private void updateNeighborBlocks() {
        if (areNeighboursBlocksValid)
            return;

        for (var i = 0; i < NEIGHBOUR_COUNT; i++)
            neighbors[i].updateNeighbour();
        areNeighboursBlocksValid = true;
    }

    private int getCursorUpdatedLightValue() {
        val cursorBrightnessValue = cursor.brightnessValue;
        if (cursorBrightnessValue >= MAX_LIGHT_VALUE)
            return cursorBrightnessValue;

        final int cursorBlockOpacity;
        if (cursor.brightnessValue >= (MAX_LIGHT_VALUE - MIN_BLOCK_LIGHT_OPACITY)) {
            cursorBlockOpacity = MIN_BLOCK_LIGHT_OPACITY;
        } else {
            cursorBlockOpacity = cursor.opacityValue;
        }
        return getCursorUpdatedLightValue(cursor.brightnessValue, cursorBlockOpacity);
    }

    private int getCursorUpdatedLightValue(int cursorBlockLightValue, int cursorBlockOpacity) {
        if (cursorBlockLightValue >= MAX_LIGHT_VALUE - cursorBlockOpacity)
            return cursorBlockLightValue;

        updateNeighborBlocks();
        var newCursorLightValue = cursorBlockLightValue;
        for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
            val neighbor = neighbors[i];
            if (!neighbor.isValid)
                continue;
            val providedLightValue = neighbor.lightValue - cursorBlockOpacity;
            newCursorLightValue = Math.max(providedLightValue, newCursorLightValue);
        }
        return newCursorLightValue;
    }

    private void spreadLightFromCursor(int cursorLightValue) {
        updateNeighborBlocks();

        for (var i = 0; i < NEIGHBOUR_COUNT; i++) {
            val neighbor = neighbors[i];
            if (!neighbor.isValid)
                continue;

            val newLightValue = cursorLightValue - neighbor.opacityValue;
            if (newLightValue > neighbor.lightValue)
                enqueueBrightening(neighbor.blockPos, neighbor.data, newLightValue, neighbor.chunk);
        }
    }

    private void enqueueBrighteningFromCursor(int lightValue) {
        if (cursor.isValid) {
            enqueueBrightening(cursor.blockPos, cursor.data, lightValue, cursor.chunk);
            cursor.setLightValue(lightValue);
        }
    }

    private void enqueueBrightening(BlockPos blockPos, long posLong, int lightValue, LumiChunk chunk) {
        assert currentLightType != null;

        val posX = blockPos.getX();
        val posY = blockPos.getY();
        val posZ = blockPos.getZ();

        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        brighteningQueues[lightValue].add(posLong);
        chunk.lumi$setLightValue(currentLightType, subChunkPosX, posY, subChunkPosZ, lightValue);
        chunk.lumi$root().lumi$markDirty();
    }

    private void enqueueDarkening(BlockPos blockPos, long posLong, int oldLightValue, LumiChunk chunk) {
        assert currentLightType != null;

        val posX = blockPos.getX();
        val posY = blockPos.getY();
        val posZ = blockPos.getZ();

        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        darkeningQueues[oldLightValue].add(posLong);
        chunk.lumi$setLightValue(currentLightType, subChunkPosX, posY, subChunkPosZ, MIN_LIGHT_VALUE);
        chunk.lumi$root().lumi$markDirty();
    }

    private void setQueue(LongList queue) {
        currentQueue = queue;
        currentQueueSize = currentQueue.size();
        currentQueueIndex = 0;
    }

    private boolean nextItem() {
        areNeighboursBlocksValid = false;

        assert currentQueue != null;
        while (currentQueueIndex < currentQueueSize) {
            val isValid = cursor.updateCursor(currentQueue.getLong(currentQueueIndex));
            currentQueueIndex++;
            if (isValid)
                return true;
        }

        currentQueue.clear();
        return false;
    }

    private static long posLongFromBlockPos(BlockPos blockPos) {
        return posLongFromPosXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    // region BlockReference
    class BlockReference {
        long neighbourBlockSideBitOffset = 0;

        boolean isValid = false;

        long data = 0;
        long chunkLongPos = -1;

        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int posX = 0;
        int posY = 0;
        int posZ = 0;

        int chunkPosY = -1;

        int subChunkPosX = 0;
        int subChunkPosY = 0;
        int subChunkPosZ = 0;

        LumiChunk chunk = null;
        LumiSubChunk subChunk = null;

        LumiSubChunkRoot subChunkRoot = null;

        Block block;
        int blockMeta;

        int brightnessValue = 0;
        int opacityValue = 0;
        int lightValue = 0;

        boolean updateCursor(long data) {
            if (this.isValid && this.data == data)
                return true;

            this.data = data;
            updatePos();
            this.isValid = updateChunk() && updateSubChunk() && updateBlock();
            return isValid;
        }

        void prepareNeighbor() {
            this.chunkLongPos = cursor.chunkLongPos;
            this.chunkPosY = cursor.chunkPosY;
            this.subChunkPosX = cursor.subChunkPosX;
            this.subChunkPosY = cursor.subChunkPosY;
            this.subChunkPosZ = cursor.subChunkPosZ;
            this.chunk = cursor.chunk;
            this.subChunk = cursor.subChunk;
            this.subChunkRoot = cursor.subChunkRoot;
        }

        void updateNeighbour() {
            this.data = cursor.data + neighbourBlockSideBitOffset;
            if ((data & POS_OVERFLOW_CHECK_BIT_MASK) != 0) {
                this.isValid = false;
                return;
            }
            prepareNeighbor();

            updatePos();
            this.isValid = updateChunk() && updateSubChunk() && updateBlock();
        }

        void updatePos() {
            this.posX = (int) ((data >> POS_X_BIT_SHIFT & POS_X_BIT_MASK) - (1L << POS_X_BIT_LENGTH - 1L));
            this.posY = (int) (data >> POS_Y_BIT_SHIFT & POS_Y_BIT_MASK);
            this.posZ = (int) ((data >> POS_Z_BIT_SHIFT & POS_Z_BIT_MASK) - (1L << POS_Z_BIT_LENGTH - 1L));

            this.blockPos.setPos(posX, posY, posZ);
        }

        boolean updateChunk() {
            val chunkPosLong = data & BLOCK_POS_CHUNK_BIT_MASK;
            if (chunk == null || this.chunkLongPos != chunkPosLong) {
                val chunkPosX = posX >> 4;
                val chunkPosZ = posZ >> 4;
                this.chunk = world.lumi$getChunkFromChunkPosIfExists(chunkPosX, chunkPosZ);
                if (chunk == null)
                    return false;
                this.subChunk = null;
                this.subChunkRoot = null;
            }

            this.chunkLongPos = chunkPosLong;
            return true;
        }

        boolean updateSubChunk() {
            // We can only re-use the subchunk, if we are re-using the chunk itself.
            val chunkPosY = posY >> 4;
            if (!(this.chunkPosY == chunkPosY && subChunk != null)) {
                this.subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
                if (subChunk == null)
                    return false;
                this.subChunkRoot = subChunk.lumi$root();
            }
            this.chunkPosY = chunkPosY;

            this.subChunkPosX = posX & 15;
            this.subChunkPosY = posY & 15;
            this.subChunkPosZ = posZ & 15;
            return true;
        }

        boolean updateBlock() {
            this.block = subChunkRoot.lumi$getBlock(subChunkPosX, subChunkPosY, subChunkPosZ);
            this.blockMeta = subChunkRoot.lumi$getBlockMeta(subChunkPosX, subChunkPosY, subChunkPosZ);
            if (currentLightType.isBlock()) {
                this.brightnessValue = clampLightValue(chunk.lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ));
                this.lightValue = subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            } else {
                if (chunk.lumi$canBlockSeeSky(subChunkPosX, posY, subChunkPosZ)) {
                    this.brightnessValue = MAX_LIGHT_VALUE;
                } else {
                    this.brightnessValue = MIN_LIGHT_VALUE;
                }
                this.lightValue = subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            }

            this.opacityValue = clampBlockLightOpacity(chunk.lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ));
            return true;
        }

        void reset() {
            isValid = false;

            data = 0;
            chunkLongPos = -1;

            posX = 0;
            posY = 0;
            posZ = 0;

            chunkPosY = -1;

            subChunkPosX = 0;
            subChunkPosY = 0;
            subChunkPosZ = 0;

            chunk = null;

            subChunk = null;
            subChunkRoot = null;

            block = null;
            blockMeta = 0;

            brightnessValue = 0;
            opacityValue = 0;
            lightValue = 0;
        }

        void setLightValue(int lightValue) {
            this.lightValue = lightValue;
        }
    }
    // endregion

    private static long posLongFromPosXYZ(int posX, int posY, int posZ) {
        // The additional logic is needed as the X and Z may be negative, and this preserves the sign value.
        return ((long) posX + (1L << POS_X_BIT_LENGTH - 1L) << POS_X_BIT_SHIFT) |
               ((long) posY << POS_Y_BIT_SHIFT) |
               ((long) posZ + (1L << POS_Z_BIT_LENGTH - 1L) << POS_Z_BIT_SHIFT);
    }
}

