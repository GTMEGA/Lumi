/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.internal.lighting.phosphor;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.api.world.LumiWorld;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lumi.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumi.api.lighting.LightType.SKY_LIGHT_TYPE;
import static com.falsepattern.lumi.internal.Share.LOG;
import static com.falsepattern.lumi.internal.lighting.phosphor.Direction.HORIZONTAL_DIRECTIONS;
import static com.falsepattern.lumi.internal.lighting.phosphor.Direction.HORIZONTAL_DIRECTIONS_SIZE;
import static com.falsepattern.lumi.internal.lighting.phosphor.PhosphorChunk.LIGHT_CHECK_FLAGS_LENGTH;

@UtilityClass
final class PhosphorUtil {
    static final int MIN_LIGHT_VALUE = 0;
    static final int MAX_LIGHT_VALUE = 15;

    static final int MIN_BLOCK_LIGHT_OPACITY = 1;
    static final int MAX_BLOCK_LIGHT_OPACITY = 15;

    static final int MIN_SKY_LIGHT_OPACITY = 0;
    static final int MAX_SKY_LIGHT_OPACITY = 15;

    static final int LIGHT_VALUE_RANGE = (MAX_LIGHT_VALUE - MIN_LIGHT_VALUE) + 1;

    private static final String NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME = "neighbor_light_checks";

    static int clampLightValue(int lightValue) {
        return MathUtil.clamp(lightValue, MIN_LIGHT_VALUE, MAX_LIGHT_VALUE);
    }

    static int clampBlockLightOpacity(int opacityValue) {
        return MathUtil.clamp(opacityValue, MIN_BLOCK_LIGHT_OPACITY, MAX_BLOCK_LIGHT_OPACITY);
    }

    static int clampSkyLightOpacity(int opacityValue) {
        return MathUtil.clamp(opacityValue, MIN_SKY_LIGHT_OPACITY, MAX_SKY_LIGHT_OPACITY);
    }

    static boolean scheduleRelightChecksForChunkBoundaries(LumiWorld world, LumiChunk chunk) {
        val chunkBasePosX = chunk.lumi$chunkPosX();
        val chunkBasePosZ = chunk.lumi$chunkPosZ();

        var scheduledSkyLightUpdates = false;
        for (int i = 0; i < HORIZONTAL_DIRECTIONS_SIZE; i++) {
            val direction = HORIZONTAL_DIRECTIONS[i];
            val xOffset = direction.xOffset; // -1, 0, +1
            val zOffset = direction.zOffset; // -1, 0, +1

            val neighbourChunkPosX = chunkBasePosX + xOffset;
            val neighbourChunkPosZ = chunkBasePosZ + zOffset;

            val neighbourChunk = getLoadedChunk(world, neighbourChunkPosX, neighbourChunkPosZ);
            if (neighbourChunk == null)
                continue;

            for (val lightType : LightType.values()) {
                for (val directionSign : DirectionSign.values()) { // -1, +1
                    // Merge flags upon loading of a chunk. This ensures that all flags are always already on the IN boundary below
                    mergeFlags(lightType, chunk, neighbourChunk, direction, directionSign);
                    mergeFlags(lightType, neighbourChunk, chunk, direction.opposite(), directionSign);

                    // Check everything that might have been canceled due to this chunk not being loaded.
                    // Also, pass in chunks if already known
                    // The boundary to the neighbor chunk (both ways)
                    scheduledSkyLightUpdates =
                            scheduleRelightChecksForBoundary(world, chunk, neighbourChunk, null, lightType, xOffset, zOffset, directionSign) ||
                            scheduleRelightChecksForBoundary(world, neighbourChunk, chunk, null, lightType, -xOffset, -zOffset, directionSign) ||
                            // The boundary to the diagonal neighbor (since the checks in that chunk were aborted if this chunk wasn't loaded, see scheduleRelightChecksForBoundary)
                            scheduleRelightChecksForBoundary(world,
                                                             neighbourChunk,
                                                             null,
                                                             chunk,
                                                             lightType,
                                                             zOffset != 0 ? directionSign.sign() : 0,
                                                             xOffset != 0 ? directionSign.sign() : 0,
                                                             DirectionSign.of(direction.opposite()));
                }
            }
        }
        return scheduledSkyLightUpdates;
    }

    static boolean isChunkFullyLit(LumiWorld world, LumiChunk chunk, Profiler profiler) {
        if (!chunk.lumi$isLightingInitialized())
            if (!initChunkLighting(world, chunk, profiler))
                return false;

        for (var zOffset = -1; zOffset <= 1; ++zOffset) {
            for (var xOffset = -1; xOffset <= 1; ++xOffset) {
                if (xOffset == 0 && zOffset == 0)
                    continue;

                val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
                val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;
                val chunkNeighbour = getLoadedChunk(world, chunkPosX, chunkPosZ);

                if (chunkNeighbour == null || !chunkNeighbour.lumi$isLightingInitialized())
                    return false;
            }
        }

        return true;
    }

    static void writeNeighborLightChecksToNBT(LumiChunk lumiChunk, NBTTagCompound output) {
        if (!(lumiChunk instanceof PhosphorChunk))
            return;
        val chunk = (PhosphorChunk) lumiChunk;
        val flags = chunk.phosphor$lightCheckFlags();

        val flagList = new NBTTagList();
        var flagsSet = false;
        for (var i = 0; i < LIGHT_CHECK_FLAGS_LENGTH; i++) {
            val flag = flags[i];
            val flagTag = new NBTTagShort(flag);
            flagList.appendTag(flagTag);
            if (flag != 0)
                flagsSet = true;
        }

        if (flagsSet)
            output.setTag(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, flagList);
    }

    static void readNeighborLightChecksFromNBT(LumiChunk lumiChunk, NBTTagCompound input) {
        if (!(lumiChunk instanceof PhosphorChunk))
            return;
        val chunk = (PhosphorChunk) lumiChunk;

        if (!input.hasKey(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, 9))
            return;

        val list = input.getTagList(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, 2);
        if (list.tagCount() != LIGHT_CHECK_FLAGS_LENGTH) {
            LOG.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})",
                     NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME,
                     lumiChunk.lumi$chunkPosX(),
                     lumiChunk.lumi$chunkPosZ());
            return;
        }

        val flags = chunk.phosphor$lightCheckFlags();
        for (var flagIndex = 0; flagIndex < LIGHT_CHECK_FLAGS_LENGTH; ++flagIndex) {
            val flagTag = (NBTTagShort) list.tagList.get(flagIndex);
            val flag = flagTag.func_150289_e();
            flags[flagIndex] = flag;
        }
    }

    static void cloneNeighborLightChecks(LumiChunk lumiFrom, LumiChunk lumiTo) {
        if (!(lumiFrom instanceof PhosphorChunk) || !(lumiTo instanceof PhosphorChunk))
            return;
        val from = (PhosphorChunk) lumiFrom;
        val to = (PhosphorChunk) lumiTo;

        ArrayUtil.copyArray(from.phosphor$lightCheckFlags(), to.phosphor$lightCheckFlags());
    }

    static @Nullable LumiChunk getLoadedChunk(LumiWorld world, int chunkPosX, int chunkPosZ) {
        return world.lumi$getChunkFromChunkPosIfExists(chunkPosX, chunkPosZ);
    }

    static void relightSkyLightColumn(LumiLightingEngine lightingEngine,
                                      LumiWorld world,
                                      LumiChunk chunk,
                                      int subChunkPosX,
                                      int subChunkPosZ,
                                      int startPosY,
                                      int endPosY) {
        {
            val minPosY = Math.min(startPosY, endPosY);
            val maxPosY = Math.max(startPosY, endPosY) - 1;
            startPosY = minPosY;
            endPosY = maxPosY;
        }

        val basePosX = (chunk.lumi$chunkPosX() << 4) + subChunkPosX;
        val basePosZ = (chunk.lumi$chunkPosZ() << 4) + subChunkPosZ;

        val minChunkPosY = startPosY / 16;
        val maxChunkPosY = endPosY / 16;

        lightingEngine.scheduleLightingUpdateForColumn(SKY_LIGHT_TYPE, basePosX, basePosZ, startPosY, endPosY);

        val bottomSubChunk = chunk.lumi$getSubChunkIfPrepared(minChunkPosY);
        if (bottomSubChunk == null && startPosY > 0) {
            val posY = startPosY - 1;
            lightingEngine.scheduleLightingUpdate(SKY_LIGHT_TYPE, basePosX, posY, basePosZ);
        }

        short flags = 0;
        for (var chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
            val subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
            if (subChunk != null)
                continue;

            val subChunkFlag = 1 << chunkPosY;
            flags |= subChunkFlag;
        }

        if (flags == 0)
            return;

        for (int i = 0; i < HORIZONTAL_DIRECTIONS_SIZE; i++) {
            val direction = HORIZONTAL_DIRECTIONS[i];
            val xOffset = direction.xOffset;
            val zOffset = direction.zOffset;
            val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
            val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;

            // Checks whether the position is at the specified border (the 16 bit is set for both 15+1 and 0-1)
            val someInterestingExpression = ((subChunkPosX + xOffset) | (subChunkPosZ + zOffset)) & 16;
            val neighborChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (someInterestingExpression != 0 && neighborChunk == null) {
                val axisDirection = DirectionSign.of(direction, subChunkPosX, subChunkPosZ);
                flagChunkBoundaryForUpdate(chunk,
                                           flags,
                                           direction,
                                           axisDirection
                );
                continue;
            }

            for (var chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
                val subChunkFlag = 1 << chunkPosY;
                val subChunkExists = (flags & subChunkFlag) != 0;
                if (!subChunkExists)
                    continue;

                val posX = basePosX + xOffset;
                val posZ = basePosZ + zOffset;
                val minPosY = chunkPosY * 16;
                val maxPosY = minPosY + 15;
                lightingEngine.scheduleLightingUpdateForColumn(SKY_LIGHT_TYPE, posX, posZ, minPosY, maxPosY);
            }
        }
    }

    private static void doRecheckGaps(LumiChunk chunk, WorldChunkSlice slice, Profiler profiler) {
        profiler.startSection("recheckGaps");
        for (int subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++)
            for (int subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++)
                recheckGapsForColumn(chunk, slice, subChunkPosX, subChunkPosZ);
        profiler.endSection();
    }

    private static void recheckGapsForColumn(LumiChunk chunk,
                                             WorldChunkSlice slice,
                                             int subChunkPosX,
                                             int subChunkPosZ) {
        if (!chunk.lumi$isHeightOutdated(subChunkPosX, subChunkPosZ))
            return;

        val posX = (chunk.lumi$chunkPosX() * 16) + subChunkPosX;
        val posZ = (chunk.lumi$chunkPosZ() * 16) + subChunkPosZ;
        val minPosY = recheckGapsGetLowestHeight(slice, posX, posZ);
        val maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);

        recheckGapsSkylightNeighborHeight(chunk, slice, posX, posZ, maxPosY, minPosY);
        chunk.lumi$isHeightOutdated(subChunkPosX, subChunkPosZ, false);
    }

    private static int recheckGapsGetLowestHeight(WorldChunkSlice slice, int posX, int posZ) {
        var minPosY = Integer.MAX_VALUE;
        for (var i = 0; i < HORIZONTAL_DIRECTIONS_SIZE; i++) {
            val direction = HORIZONTAL_DIRECTIONS[i];
            val neighbourPosX = posX + direction.xOffset;
            val neighbourPosZ = posZ + direction.zOffset;
            val chunk = slice.getChunkFromWorldCoords(neighbourPosX, neighbourPosZ);

            minPosY = Math.min(minPosY, chunk.lumi$minSkyLightHeight());
        }
        return minPosY;
    }

    private static void recheckGapsSkylightNeighborHeight(LumiChunk chunk,
                                                          WorldChunkSlice slice,
                                                          int posX,
                                                          int posZ,
                                                          int height,
                                                          int max) {
        checkSkylightNeighborHeight(chunk, slice, posX, posZ, max);
        for (var i = 0; i < HORIZONTAL_DIRECTIONS_SIZE; i++) {
            val direction = HORIZONTAL_DIRECTIONS[i];
            val neighbourPosX = posX + direction.xOffset;
            val neighbourPosZ = posZ + direction.zOffset;
            checkSkylightNeighborHeight(chunk, slice, neighbourPosX, neighbourPosZ, height);
        }
    }

    private static void checkSkylightNeighborHeight(LumiChunk chunk,
                                                    WorldChunkSlice slice,
                                                    int posX,
                                                    int posZ,
                                                    int maxValue) {
        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        val neighbourChunk = slice.getChunkFromWorldCoords(posX, posZ);
        val neighbourSkyLightHeight = neighbourChunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);
        if (neighbourSkyLightHeight > maxValue) {
            val maxPosY = neighbourSkyLightHeight + 1;
            updateSkylightNeighborHeight(chunk, slice, posX, posZ, maxValue, maxPosY);
        } else if (neighbourSkyLightHeight < maxValue) {
            val maxPosY = maxValue + 1;
            updateSkylightNeighborHeight(chunk, slice, posX, posZ, neighbourSkyLightHeight, maxPosY);
        }
    }

    private static void updateSkylightNeighborHeight(LumiChunk chunk,
                                                     WorldChunkSlice slice,
                                                     int posX,
                                                     int posZ,
                                                     int minPosY,
                                                     int maxPosY) {
        if (!slice.isLoaded(posX, posZ, 16))
            return;

        val lightingEngine = chunk.lumi$world().lumi$lightingEngine();
        lightingEngine.scheduleLightingUpdateForColumn(SKY_LIGHT_TYPE, posX, posZ, minPosY, maxPosY);
        chunk.lumi$root().lumi$markDirty();
    }

    private static void flagChunkBoundaryForUpdate(LumiChunk lumiChunk,
                                                   short subChunkMask,
                                                   Direction direction,
                                                   DirectionSign directionSign) {
        if (!(lumiChunk instanceof PhosphorChunk))
            return;
        val chunk = (PhosphorChunk) lumiChunk;
        val flags = chunk.phosphor$lightCheckFlags();

        val flagIndex = getFlagIndex(SKY_LIGHT_TYPE, direction, directionSign, FacingDirection.OUTPUT);
        flags[flagIndex] |= subChunkMask;
        lumiChunk.lumi$root().lumi$markDirty();
    }

    private static void mergeFlags(LightType lightType,
                                   LumiChunk lumiDestinationChunk,
                                   LumiChunk lumiSourceChunk,
                                   Direction direction,
                                   DirectionSign directionSign) {
        if (!(lumiDestinationChunk instanceof PhosphorChunk) || !(lumiSourceChunk instanceof PhosphorChunk))
            return;
        val destinationChunk = (PhosphorChunk) lumiDestinationChunk;
        val sourceChunk = (PhosphorChunk) lumiSourceChunk;

        val destinationFlags = destinationChunk.phosphor$lightCheckFlags();
        val sourceFlags = sourceChunk.phosphor$lightCheckFlags();

        val destinationFlagIndex = getFlagIndex(lightType, direction, directionSign, FacingDirection.INPUT);
        val sourceFlagIndex = getFlagIndex(lightType, direction.opposite(), directionSign, FacingDirection.OUTPUT);

        destinationFlags[destinationFlagIndex] |= sourceFlags[sourceFlagIndex];
        // no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    private static boolean scheduleRelightChecksForBoundary(LumiWorld world,
                                                            LumiChunk lumiChunk,
                                                            LumiChunk lumiNChunk,
                                                            LumiChunk lumiSChunk,
                                                            LightType lightType,
                                                            int xOffset,
                                                            int zOffset,
                                                            DirectionSign directionSign) {
        if (!(lumiChunk instanceof PhosphorChunk))
            return false;
        val chunk = (PhosphorChunk) lumiChunk;
        val flags = chunk.phosphor$lightCheckFlags();

        // OUT checks from neighbor are already merged
        val inFlagIndex = getFlagIndex(lightType, xOffset, zOffset, directionSign, FacingDirection.INPUT);
        val flag = flags[inFlagIndex];

        if (flag == 0)
            return false;

        val chunkBasePosX = lumiChunk.lumi$chunkPosX();
        val chunkBasePosZ = lumiChunk.lumi$chunkPosZ();

        if (lumiNChunk == null) {
            val chunkPosX = chunkBasePosX + xOffset;
            val chunkPosZ = chunkBasePosZ + zOffset;
            lumiNChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (lumiNChunk == null)
                return false;
        }

        if (lumiSChunk == null) {
            val chunkPosX = chunkBasePosX + (zOffset != 0 ? directionSign.sign() : 0);
            val chunkPosZ = chunkBasePosZ + (xOffset != 0 ? directionSign.sign() : 0);

            lumiSChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (lumiSChunk == null)
                return false;
        }

        if (!(lumiNChunk instanceof PhosphorChunk))
            return false;
        val nChunk = (PhosphorChunk) lumiNChunk;
        val nFlags = nChunk.phosphor$lightCheckFlags();

        val outFlagIndex = getFlagIndex(lightType, -xOffset, -zOffset, directionSign, FacingDirection.OUTPUT);
        // Clear only now that it's clear that the checks are processed
        flags[inFlagIndex] = 0;
        nFlags[outFlagIndex] = 0;

        lumiChunk.lumi$root().lumi$markDirty();
        lumiNChunk.lumi$root().lumi$markDirty();

        val lightingEngine = world.lumi$lightingEngine();

        // Get the area to check
        // Start in the corner...
        var minPosX = lumiChunk.lumi$chunkPosX() << 4;
        var minPosZ = lumiChunk.lumi$chunkPosZ() << 4;

        // move to other side of chunk if the direction is positive
        if ((xOffset | zOffset) > 0) {
            minPosX += xOffset * 15;
            minPosZ += zOffset * 15;
        }

        // shift to other half if necessary (shift perpendicular to dir)
        if (directionSign == DirectionSign.POSITIVE) {
            // x & 1 is same as abs(x) for x = -1, 0, 1
            minPosX += (zOffset & 1) * 8;
            minPosZ += (xOffset & 1) * 8;
        }

        // get maximal values (shift perpendicular to dir)
        val maxPosX = (7 * (zOffset & 1)) + minPosX;
        val maxPosZ = (7 * (xOffset & 1)) + minPosZ;

        var scheduledSkyLightUpdates = false;
        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            if ((flag & (1 << chunkPosY)) != 0) {
                val minPosY = chunkPosY * 16;
                val maxPosY = minPosY + 15;
                lightingEngine.scheduleLightingUpdateForRange(lightType,
                                                              minPosX,
                                                              minPosY,
                                                              minPosZ,
                                                              maxPosX,
                                                              maxPosY,
                                                              maxPosZ);
                scheduledSkyLightUpdates = true;
            }
        }
        return scheduledSkyLightUpdates;
    }

    private static int getFlagIndex(LightType lightType,
                                    Direction direction,
                                    DirectionSign directionSign,
                                    FacingDirection facingDirection) {
        val xOffset = direction.xOffset;
        val zOffset = direction.zOffset;
        return getFlagIndex(lightType, xOffset, zOffset, directionSign, facingDirection);
    }

    private static int getFlagIndex(LightType lightType,
                                    int facingOffsetX,
                                    int facingOffsetZ,
                                    DirectionSign directionSign,
                                    FacingDirection facingDirection) {
        final int lightTypeBits;
        switch (lightType) {
            default:
            case BLOCK_LIGHT_TYPE:
                lightTypeBits = 0x00;
                break;
            case SKY_LIGHT_TYPE:
                lightTypeBits = 0x10;
                break;
        }

        val facingOffsetXBits = (facingOffsetX + 1) << 2;
        val facingOffsetZBits = (facingOffsetZ + 1) << 1;
        val axisDirectionOffsetBits = directionSign.sign() + 1;
        val boundaryFacingBits = facingDirection.ordinal();

        return lightTypeBits |
               facingOffsetXBits |
               facingOffsetZBits |
               axisDirectionOffsetBits |
               boundaryFacingBits;
    }

    private static boolean initChunkLighting(LumiWorld world, LumiChunk chunk, Profiler profiler) {
        val chunkPosX = chunk.lumi$chunkPosX();
        val chunkPosZ = chunk.lumi$chunkPosZ();

        val basePosX = chunkPosX << 4;
        val basePosZ = chunkPosZ << 4;

        val centerPosX = basePosX + 8;
        val centerPosZ = basePosZ + 8;

        val slice = new WorldChunkSlice(world, chunkPosX, chunkPosZ);
        if (!slice.isLoaded(centerPosX, centerPosZ, 16))
            return false;

        val lightingEngine = world.lumi$lightingEngine();
        var scheduledBlockUpdate = false;
        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            val subChunk = chunk.lumi$getSubChunkIfPrepared(chunkPosY);
            if (subChunk == null)
                continue;

            val basePosY = chunkPosY * 16;
            for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                        val posY = basePosY + subChunkPosY;

                        val brightness = chunk.lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
                        if (brightness > MIN_LIGHT_VALUE) {
                            val posX = basePosX + subChunkPosX;
                            val posZ = basePosZ + subChunkPosZ;
                            lightingEngine.scheduleLightingUpdate(BLOCK_LIGHT_TYPE, posX, posY, posZ);
                            scheduledBlockUpdate = true;
                        }
                    }
                }
            }
        }
        if (scheduledBlockUpdate)
            lightingEngine.processLightingUpdatesForType(BLOCK_LIGHT_TYPE);

        if (world.lumi$root().lumi$hasSky()) {
            chunk.lumi$resetOutdatedHeightFlags();
            doRecheckGaps(chunk, slice, profiler);
        }

        chunk.lumi$isLightingInitialized(true);
        return true;
    }
}
