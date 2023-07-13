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

package com.falsepattern.lumina.internal.lighting;

import com.falsepattern.lib.internal.Share;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.coordinate.Direction;
import com.falsepattern.lumina.api.coordinate.DirectionSign;
import com.falsepattern.lumina.api.coordinate.FacingDirection;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.world.WorldChunkSlice;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.EnumSkyBlock;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public final class LightingHooksOld {
    /**
     * 2 light types * 4 directions * 2 halves * (inwards + outwards)
     */
    public static final int FLAG_COUNT = 32;
    public static final String NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME = "neighbor_light_checks";

    public static void relightBlock(LumiChunk chunk, int subChunkPosX, int posY, int subChunkPosZ) {
        var maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ) & 255;
        var minPosY = Math.max(posY, maxPosY);

        while (minPosY > 0 && chunk.lumi$getBlockOpacity(subChunkPosX, minPosY - 1, subChunkPosZ) == 0)
            --minPosY;
        if (minPosY == maxPosY)
            return;

        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, minPosY);

        if (chunk.lumi$world().lumi$root().lumi$hasSky())
            relightSkylightColumn(chunk.lumi$world(), chunk, subChunkPosX, subChunkPosZ, maxPosY, minPosY);

        maxPosY = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);
        if (maxPosY < chunk.lumi$minSkyLightHeight())
            chunk.lumi$minSkyLightHeight(maxPosY);

        chunk.lumi$root().lumi$markDirty();
    }

    public static void initChunkSkyLight(LumiChunk chunk) {
        chunk.lumi$resetSkyLightHeightMap();

        val rootWorld = chunk.lumi$world().lumi$root();
        val hasSky = rootWorld.lumi$hasSky();

        val rootChunk = chunk.lumi$root();

        val basePosX = chunk.lumi$chunkPosX() * 16;
        val basePosY = rootChunk.lumi$topPreparedSubChunkBasePosY();
        val basePosZ = chunk.lumi$chunkPosZ() * 16;

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

                                val subChunk = chunk.lumi$subChunk(chunkPosY);
                                if (subChunk != null) {
                                    val posX = basePosX + subChunkPosX;
                                    val posZ = basePosZ + subChunkPosZ;

                                    subChunk.lumi$setSkyLightValue(subChunkPosX,
                                                                   subChunkPosY,
                                                                   subChunkPosZ,
                                                                   lightLevel);
                                    rootWorld.lumi$markBlockForRenderUpdate(posX, skyLightHeight, posZ);
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
        rootChunk.lumi$markDirty();
    }

    @SideOnly(Side.CLIENT)
    public static void initClientChunkSkyLight(LumiChunk chunk) {
        chunk.lumi$resetSkyLightHeightMap();

        val rootChunk = chunk.lumi$root();

        val basePosY = rootChunk.lumi$topPreparedSubChunkBasePosY();
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

                    ++subChunkPosZ;
                    break;
                }
            }
        }

        chunk.lumi$minSkyLightHeight(minSkyLightHeight);
        rootChunk.lumi$markDirty();
    }

    public static void scheduleRelightChecksForChunkBoundaries(LumiWorld world, LumiChunk chunk) {
        val baseChunkPosX = chunk.lumi$chunkPosX();
        val baseChunkPosZ = chunk.lumi$chunkPosZ();

        for (val direction : Direction.horizontalDirections()) {
            val xOffset = direction.xOffset();
            val zOffset = direction.zOffset();

            val neighbourChunkPosX = baseChunkPosX + xOffset;
            val neighbourChunkPosZ = baseChunkPosZ + zOffset;

            val neighbourChunk = getLoadedChunk(world, neighbourChunkPosX, neighbourChunkPosZ);
            if (neighbourChunk == null)
                continue;

            for (val lightType : EnumSkyBlock.values()) {
                for (val directionSign : DirectionSign.values()) {
                    // Merge flags upon loading of a chunk. This ensures that all flags are always already on the IN boundary below
                    mergeFlags(lightType, chunk, neighbourChunk, direction, directionSign);
                    mergeFlags(lightType, neighbourChunk, chunk, direction.opposite(), directionSign);

                    // Check everything that might have been canceled due to this chunk not being loaded.
                    // Also, pass in chunks if already known
                    // The boundary to the neighbor chunk (both ways)
                    scheduleRelightChecksForBoundary(world, chunk, neighbourChunk, null, lightType, xOffset, zOffset, directionSign);
                    scheduleRelightChecksForBoundary(world, neighbourChunk, chunk, null, lightType, -xOffset, -zOffset, directionSign);
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
    }


    public static boolean checkChunkLighting(LumiWorld world, LumiChunk chunk) {
        if (!chunk.lumi$lightingInitialized())
            initChunkLighting(world, chunk);

        for (int zOffset = -1; zOffset <= 1; ++zOffset) {
            for (int xOffset = -1; xOffset <= 1; ++xOffset) {
                if (xOffset == 0 && zOffset == 0)
                    continue;

                val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
                val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;
                val chunkNeighbour = getLoadedChunk(world, chunkPosX, chunkPosZ);

                if (chunkNeighbour == null || !chunkNeighbour.lumi$lightingInitialized())
                    return false;
            }
        }

        return true;
    }

    public static void initSkyLightForSubChunk(LumiWorld world, LumiChunk chunk, LumiSubChunk subChunk) {
        if (!world.lumi$root().lumi$hasSky())
            return;

        val maxPosY = subChunk.lumi$root().lumi$posY();
        val lightValue = EnumSkyBlock.Sky.defaultLightValue;
        for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
            for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                if (chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ) <= maxPosY) {
                    for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                        subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                    }
                }
            }
        }
    }

    public static void writeNeighborLightChecksToNBT(LumiChunk chunk, NBTTagCompound output) {
        val neighborLightCheckFlags = chunk.lumi$neighborLightCheckFlags();
        val flagList = new NBTTagList();
        var flagsSet = false;
        for (val flag : neighborLightCheckFlags) {
            val flagTag = new NBTTagShort(flag);
            flagList.appendTag(flagTag);
            if (flag != 0)
                flagsSet = true;
        }

        if (flagsSet)
            output.setTag(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, flagList);
    }

    public static void readNeighborLightChecksFromNBT(LumiChunk chunk, NBTTagCompound input) {
        if (!input.hasKey(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, 9))
            return;

        val list = input.getTagList(NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME, 2);
        if (list.tagCount() != FLAG_COUNT) {
            Share.LOG.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})",
                           NEIGHBOR_LIGHT_CHECKS_NBT_TAG_NAME,
                           chunk.lumi$chunkPosX(),
                           chunk.lumi$chunkPosZ());
            return;
        }

        val neighborLightCheckFlags = chunk.lumi$neighborLightCheckFlags();
        for (var flagIndex = 0; flagIndex < FLAG_COUNT; ++flagIndex) {
            val flagTag = (NBTTagShort) list.tagList.get(flagIndex);
            val flag = flagTag.func_150289_e();
            neighborLightCheckFlags[flagIndex] = flag;
        }
    }

    public static @Nullable LumiChunk getLoadedChunk(LumiWorld world, int chunkPosX, int chunkPosZ) {
        val provider = world.lumi$root().lumi$chunkProvider();
        if (!provider.chunkExists(chunkPosX, chunkPosZ))
            return null;

        val baseChunk = provider.provideChunk(chunkPosX, chunkPosZ);
        return world.lumi$wrap(baseChunk);
    }

    private static void relightSkylightColumn(LumiWorld world,
                                              LumiChunk chunk,
                                              int subChunkPosX,
                                              int subChunkPosZ,
                                              int startPosY,
                                              int endPosY) {
        val lightingEngine = world.lumi$lightingEngine();

        {
            val minPosY = Math.min(startPosY, endPosY);
            val maxPosY = Math.max(startPosY, endPosY) - 1;
            startPosY = minPosY;
            endPosY = maxPosY;
        }

        val basePosX = (chunk.lumi$chunkPosX() * 16) + subChunkPosX;
        val basePosZ = (chunk.lumi$chunkPosZ() * 16) + subChunkPosZ;

        val minChunkPosY = startPosY / 16;
        val maxChunkPosY = endPosY / 16;

        scheduleSkyLightUpdateForColumn(world, basePosX, basePosZ, startPosY, endPosY);

        val bottomSubChunk = chunk.lumi$subChunk(minChunkPosY);
        if (bottomSubChunk == null && startPosY > 0) {
            val posY = startPosY - 1;
            lightingEngine.scheduleLightUpdate(EnumSkyBlock.Sky, basePosX, posY, basePosZ);
        }

        short flags = 0;
        for (var chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            if (subChunk != null)
                continue;

            val subChunkFlag = 1 << chunkPosY;
            flags |= subChunkFlag;
        }

        if (flags == 0)
            return;

        for (val direction : Direction.horizontalDirections()) {
            val xOffset = direction.xOffset();
            val zOffset = direction.zOffset();
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
                scheduleSkyLightUpdateForColumn(world, posX, posZ, minPosY, maxPosY);
            }
        }
    }

    private static void scheduleRelightChecksForArea(LumiWorld world,
                                                     EnumSkyBlock lightType,
                                                     int minPosX,
                                                     int minPosY,
                                                     int minPosZ,
                                                     int maxPosX,
                                                     int maxPosY,
                                                     int maxPosZ) {
        val lightingEngine = world.lumi$lightingEngine();
        for (var posY = minPosY; posY <= maxPosY; posY++)
            for (var posZ = minPosZ; posZ <= maxPosZ; posZ++)
                for (var posX = minPosX; posX <= maxPosX; posX++)
                    lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
    }

    private static void scheduleSkyLightUpdateForColumn(LumiWorld world,
                                                        int posX,
                                                        int posZ,
                                                        int minPosY,
                                                        int maxPosY) {
        val lightingEngine = world.lumi$lightingEngine();
        for (var posY = minPosY; posY <= maxPosY; posY++)
            lightingEngine.scheduleLightUpdate(EnumSkyBlock.Sky, posX, posY, posZ);
    }

    private static void doRecheckGaps(LumiChunk chunk) {
        val world = chunk.lumi$world();
        val worldRoot = world.lumi$root();
        val profiler = worldRoot.lumi$profiler();

        profiler.startSection("recheckGaps");
        profilerSection:
        {
            val chunkPosX = chunk.lumi$chunkPosX();
            val chunkPosZ = chunk.lumi$chunkPosZ();

            val centerPosX = (chunkPosX * 16) + 8;
            val centerPosY = 0;
            val centerPosZ = (chunkPosZ * 16) + 8;
            val blockRange = 16;

            val slice = new WorldChunkSlice(world, chunkPosX, chunkPosZ);
            if (!worldRoot.lumi$doChunksExist(centerPosX, centerPosY, centerPosZ, blockRange))
                break profilerSection;

            for (int subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++)
                for (int subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++)
                    recheckGapsForColumn(chunk, slice, subChunkPosX, subChunkPosZ);
        }
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
        for (val direction : Direction.horizontalDirections()) {
            val neighbourPosX = posX + direction.xOffset();
            val neighbourPosZ = posZ + direction.zOffset();
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
        for (val direction : Direction.horizontalDirections()) {
            val neighbourPosX = posX + direction.xOffset();
            val neighbourPosZ = posZ + direction.zOffset();
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
        if (maxPosY <= minPosY)
            return;
        if (!slice.isLoaded(posX, posZ, 16))
            return;

        val lightingEngine = chunk.lumi$world().lumi$lightingEngine();
        for (var posY = minPosY; posY < maxPosY; posY++)
            lightingEngine.scheduleLightUpdate(EnumSkyBlock.Sky, posX, posY, posZ);
        chunk.lumi$root().lumi$markDirty();
    }

    private static void flagChunkBoundaryForUpdate(LumiChunk chunk,
                                                   short subChunkMask,
                                                   Direction direction,
                                                   DirectionSign directionSign) {
        val flagIndex = getFlagIndex(EnumSkyBlock.Sky, direction, directionSign, FacingDirection.OUTPUT);
        chunk.lumi$neighborLightCheckFlags()[flagIndex] |= subChunkMask;
        chunk.lumi$root().lumi$markDirty();
    }

    private static void mergeFlags(EnumSkyBlock lightType,
                                   LumiChunk destinationChunk,
                                   LumiChunk sourceChunk,
                                   Direction direction,
                                   DirectionSign directionSign) {
        if (sourceChunk.lumi$neighborLightCheckFlags() == null)
            return;

        val destinationFlagIndex = getFlagIndex(lightType, direction, directionSign, FacingDirection.INPUT);
        val sourceFlagIndex = getFlagIndex(lightType, direction.opposite(), directionSign, FacingDirection.OUTPUT);

        destinationChunk.lumi$neighborLightCheckFlags()[destinationFlagIndex] |= sourceChunk.lumi$neighborLightCheckFlags()[sourceFlagIndex];
        // no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    private static void scheduleRelightChecksForBoundary(LumiWorld world,
                                                         LumiChunk chunk,
                                                         LumiChunk nChunk,
                                                         LumiChunk sChunk,
                                                         EnumSkyBlock lightType,
                                                         int xOffset,
                                                         int zOffset,
                                                         DirectionSign directionSign) {
        // OUT checks from neighbor are already merged
        val inFlagIndex = getFlagIndex(lightType, xOffset, zOffset, directionSign, FacingDirection.INPUT);
        val flags = chunk.lumi$neighborLightCheckFlags()[inFlagIndex];

        if (flags == 0)
            return;

        val baseChunkPosX = chunk.lumi$chunkPosX();
        val baseChunkPosZ = chunk.lumi$chunkPosZ();

        if (nChunk == null) {
            val chunkPosX = baseChunkPosX + xOffset;
            val chunkPosZ = baseChunkPosZ + zOffset;
            nChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (nChunk == null)
                return;
        }

        if (sChunk == null) {
            val chunkPosX = baseChunkPosX + (zOffset != 0 ? directionSign.sign() : 0);
            val chunkPosZ = baseChunkPosZ + (xOffset != 0 ? directionSign.sign() : 0);

            sChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (sChunk == null)
                return;
        }

        val outFlagIndex = getFlagIndex(lightType, -xOffset, -zOffset, directionSign, FacingDirection.OUTPUT);
        chunk.lumi$neighborLightCheckFlags()[inFlagIndex] = 0;
        nChunk.lumi$neighborLightCheckFlags()[outFlagIndex] = 0; //Clear only now that it's clear that the checks are processed

        chunk.lumi$root().lumi$markDirty();
        nChunk.lumi$root().lumi$markDirty();

        // Get the area to check
        // Start in the corner...
        var minPosX = chunk.lumi$chunkPosX() << 4;
        var minPosZ = chunk.lumi$chunkPosZ() << 4;

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

        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            if ((flags & (1 << chunkPosY)) != 0) {
                val minPosY = chunkPosY * 16;
                val maxPosY = minPosY + 15;
                scheduleRelightChecksForArea(world, lightType, minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
            }
        }
    }

    private static int getFlagIndex(EnumSkyBlock lightType,
                                    Direction direction,
                                    DirectionSign directionSign,
                                    FacingDirection facingDirection) {
        val xOffset = direction.xOffset();
        val zOffset = direction.zOffset();
        return getFlagIndex(lightType, xOffset, zOffset, directionSign, facingDirection);
    }

    private static int getFlagIndex(EnumSkyBlock lightType,
                                    int facingOffsetX,
                                    int facingOffsetZ,
                                    DirectionSign directionSign,
                                    FacingDirection facingDirection) {
        final int lightTypeBits;
        switch (lightType) {
            default:
            case Sky:
                lightTypeBits = 0x10;
                break;
            case Block:
                lightTypeBits = 0x00;
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

    private static void initChunkLighting(LumiWorld world, LumiChunk chunk) {
        val basePosX = chunk.lumi$chunkPosX() * 16;
        val basePosZ = chunk.lumi$chunkPosZ() * 16;

        val minPosX = basePosX - 16;
        val minPosY = 0;
        val minPosZ = basePosZ - 16;

        val maxPosX = basePosX + 31;
        val maxPosY = 255;
        val maxPosZ = basePosZ + 31;

        if (!world.lumi$root().lumi$doChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ))
            return;

        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            if (subChunk == null)
                continue;

            val basePosY = chunkPosY * 16;
            for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                        val brightness = chunk.lumi$getBlockBrightness(subChunkPosX, subChunkPosY, subChunkPosZ);
                        if (brightness > 0) {
                            val posX = basePosX + subChunkPosX;
                            val posY = basePosY + subChunkPosY;
                            val posZ = basePosZ + subChunkPosZ;
                            world.lumi$lightingEngine().scheduleLightUpdate(EnumSkyBlock.Block, posX, posY, posZ);
                        }
                    }
                }
            }
        }

        if (world.lumi$root().lumi$hasSky()) {
            chunk.lumi$resetOutdatedHeightFlags();
            doRecheckGaps(chunk);
        }

        chunk.lumi$lightingInitialized(true);
    }
}
