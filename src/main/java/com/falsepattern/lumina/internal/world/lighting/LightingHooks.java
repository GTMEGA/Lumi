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
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.world.WorldChunkSlice;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;

import java.util.Arrays;

import static com.falsepattern.lumina.internal.world.lighting.LightingEngineHelpers.getLoadedChunk;

@SuppressWarnings("unused")
public class LightingHooks {
    private static final EnumSkyBlock[] ENUM_SKY_BLOCK_VALUES = EnumSkyBlock.values();
    private static final AxisDirection[] ENUM_AXIS_DIRECTION_VALUES = AxisDirection.values();
    public static final EnumFacing[] HORIZONTAL_FACINGS = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST};

    public static final int FLAG_COUNT = 32; //2 light types * 4 directions * 2 halves * (inwards + outwards)
    public static final String NEIGHBOR_LIGHT_CHECKS_KEY = "NeighborLightChecks";

    public static void relightSkylightColumn(LumiWorld world, LumiChunk chunk, int subChunkPosX, int subChunkPosZ, int aPosY, int bPosY) {
        val minPosY = Math.min(aPosY, bPosY);
        val maxPosY = Math.max(aPosY, bPosY) - 1;

        val posX = (chunk.lumi$chunkPosX() << 4) + subChunkPosX;
        val posZ = (chunk.lumi$chunkPosZ() << 4) + subChunkPosZ;

        val minChunkPosY = minPosY / 16;
        val maxChunkPosY = maxPosY / 16;

        scheduleRelightChecksForColumn(world, EnumSkyBlock.Sky, posX, posZ, minPosY, maxPosY);

        val bottomSubChunk = chunk.lumi$subChunk(minChunkPosY);
        if (bottomSubChunk == null && minPosY > 0)
            chunk.lumi$lightingEngine().scheduleLightUpdate(EnumSkyBlock.Sky, posX, minPosY - 1, posZ);

        short flags = 0;
        for (int chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            if (subChunk != null)
                continue;

            val subChunkFlag = 1 << chunkPosY;
            flags |= subChunkFlag;
        }

        if (flags == 0)
            return;

        for (val direction : HORIZONTAL_FACINGS) {
            val xOffset = direction.getFrontOffsetX();
            val zOffset = direction.getFrontOffsetZ();

            val neighborColumnExists =
                    (((subChunkPosX + xOffset) | (subChunkPosZ + zOffset)) & 16) == 0
                    //Checks whether the position is at the specified border (the 16 bit is set for both 15+1 and 0-1)
                    || getLoadedChunk(world, chunk.lumi$chunkPosX() + xOffset, chunk.lumi$chunkPosZ() + zOffset) != null;

            if (!neighborColumnExists) {
                flagChunkBoundaryForUpdate(chunk, flags, EnumSkyBlock.Sky, direction, getAxisDirection(direction, subChunkPosX, subChunkPosZ), EnumBoundaryFacing.OUT);
                continue;
            }

            for (int chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
                val subChunkFlag = 1 << chunkPosY;
                val subChunkExists = (flags & subChunkFlag) != 0;
                if (!subChunkExists)
                    continue;

                scheduleRelightChecksForColumn(world,
                                               EnumSkyBlock.Sky,
                                               posX + xOffset,
                                               posZ + zOffset,
                                               chunkPosY << 4,
                                               (chunkPosY << 4) + 15);
            }
        }
    }

    public static void scheduleRelightChecksForArea(LumiWorld world,
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

    private static void scheduleRelightChecksForColumn(LumiWorld world,
                                                       EnumSkyBlock lightType,
                                                       int posX,
                                                       int posZ,
                                                       int minPosY,
                                                       int maxPosY) {
        val lightingEngine = world.lumi$lightingEngine();
        for (var posY = minPosY; posY <= maxPosY; posY++)
            lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
    }

    public static void relightBlock(LumiChunk chunk, int subChunkPosX, int posY, int subChunkPosZ) {
        var maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ) & 255;
        var minPosY = Math.max(posY, maxPosY);

        while (minPosY > 0 && chunk.lumi$getBlockOpacity(subChunkPosX, minPosY - 1, subChunkPosZ) == 0)
            --minPosY;
        if (minPosY == maxPosY)
            return;

        lumiSetHeightValue(chunk, subChunkPosX, subChunkPosZ, minPosY);

        if (chunk.lumi$world().lumi$root().lumi$hasSky())
            relightSkylightColumn(chunk.lumi$world(), chunk, subChunkPosX, subChunkPosZ, maxPosY, minPosY);

        maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ);
        if (maxPosY < chunk.lumi$minSkyLightHeight())
            chunk.lumi$minSkyLightHeight(maxPosY);
    }

    public static void doRecheckGaps(LumiChunk chunk, boolean onlyOne) {
        val world = chunk.lumi$world();
        val worldRoot = world.lumi$root();
        val profiler = worldRoot.lumi$profiler();
        profiler.startSection("recheckGaps");

        val chunkPosX = chunk.lumi$chunkPosX();
        val chunkPosZ = chunk.lumi$chunkPosZ();

        val centerPosX = (chunkPosX * 16) + 8;
        val centerPosY = 0;
        val centerPosZ = (chunkPosZ * 16) + 8;
        val blockRange = 16;

        val slice = new WorldChunkSlice(world, chunkPosX, chunkPosZ);
        if (!worldRoot.lumi$doChunksExist(centerPosX, centerPosY, centerPosZ, blockRange)) {
            profiler.endSection();
            return;
        }

        for (int subChunkPosZ = 0; subChunkPosZ < 16; ++subChunkPosZ) {
            for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
                if (!recheckGapsForColumn(chunk, slice, subChunkPosX, subChunkPosZ))
                    continue;

                if (onlyOne) {
                    profiler.endSection();
                    return;
                }
            }
        }

        chunk.lumi$root().lumi$shouldRecheckLightingGaps(false);
        profiler.endSection();
    }

    private static boolean recheckGapsForColumn(LumiChunk chunk, WorldChunkSlice slice, int subChunkPosX, int subChunkPosZ) {
        val index = subChunkPosX + subChunkPosZ * 16;

        val isOutdated = chunk.lumi$outdatedSkyLightColumns()[index];
        if (isOutdated) {
            val maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ);

            val posX = chunk.lumi$chunkPosX() * 16 + subChunkPosX;
            val posZ = chunk.lumi$chunkPosZ() * 16 + subChunkPosZ;

            val minPosY = recheckGapsGetLowestHeight(slice, posX, posZ);

            recheckGapsSkylightNeighborHeight(chunk, slice, posX, posZ, maxPosY, minPosY);

            chunk.lumi$outdatedSkyLightColumns()[index] = false;
            return true;
        }

        return false;
    }

    private static int recheckGapsGetLowestHeight(WorldChunkSlice slice, int centerPosX, int centerPosZ) {
        int minPosY = Integer.MAX_VALUE;

        for (val facing : HORIZONTAL_FACINGS) {
            val posX = centerPosX + facing.getFrontOffsetX();
            val posY = centerPosZ + facing.getFrontOffsetZ();
            val chunk = slice.getChunkFromWorldCoords(posX, posY);

            minPosY = Math.min(minPosY, chunk.lumi$minSkyLightHeight());
        }

        return minPosY;
    }

    private static void recheckGapsSkylightNeighborHeight(LumiChunk chunk, WorldChunkSlice slice, int x, int z, int height, int max) {
        checkSkylightNeighborHeight(chunk, slice, x, z, max);

        for (val facing : HORIZONTAL_FACINGS) {
            int j = x + facing.getFrontOffsetX();
            int k = z + facing.getFrontOffsetZ();

            checkSkylightNeighborHeight(chunk, slice, j, k, height);
        }
    }

    private static void checkSkylightNeighborHeight(LumiChunk chunk, WorldChunkSlice slice, int x, int z, int maxValue) {
        int i = lumiGetHeightValue(slice.getChunkFromWorldCoords(x, z), x & 15, z & 15);

        if (i > maxValue) {
            updateSkylightNeighborHeight(chunk, slice, x, z, maxValue, i + 1);
        } else if (i < maxValue) {
            updateSkylightNeighborHeight(chunk, slice, x, z, i, maxValue + 1);
        }
    }

    private static void updateSkylightNeighborHeight(LumiChunk chunk, WorldChunkSlice slice, int x, int z, int startY, int endY) {
        if (endY > startY) {
            if (!slice.isLoaded(x, z, 16)) {
                return;
            }

            for (int i = startY; i < endY; ++i) {
                chunk.lumi$lightingEngine().scheduleLightUpdate(EnumSkyBlock.Sky, x, i, z);
            }

            chunk.lumi$root().lumi$markDirty();
        }
    }

    public static boolean lumiCanBlockSeeTheSky(LumiChunk iLumiChunk, int x, int y, int z) {
        return y >= iLumiChunk.lumi$skyLightHeights()[z << 4 | x];
    }

    public static void lumiSetSkylightUpdatedPublic(LumiChunk iLumiChunk) {
        Arrays.fill(iLumiChunk.lumi$outdatedSkyLightColumns(), true);
    }

    public static void lumiSetHeightValue(LumiChunk iLumiChunk, int x, int z, int val) {
        iLumiChunk.lumi$skyLightHeights()[z << 4 | x] = val;
    }

    public static int lumiGetHeightValue(LumiChunk iLumiChunk, int x, int z) {
        return iLumiChunk.lumi$skyLightHeights()[z << 4 | x];
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public static void generateSkylightMap(LumiChunk chunk) {
        val root = chunk.lumi$root();
        int topSegment = root.lumi$topPreparedSubChunkPosY();
        chunk.lumi$minSkyLightHeight(Integer.MAX_VALUE);
        int heightMapMinimum = Integer.MAX_VALUE;
        val heightMap = chunk.lumi$skyLightHeights();
        for (int x = 0; x < 16; ++x) {
            int z = 0;
            while (z < 16) {
                val vanillaChunk = chunk.lumi$root().lumi$base();
                vanillaChunk.precipitationHeightMap[x + (z << 4)] = -999;
                int y = topSegment + 16 - 1;

                while (true) {
                    if (y > 0) {
                        val blockOpacity = chunk.lumi$getBlockOpacity(x, y - 1, z);
                        if (blockOpacity == 0) {
                            --y;
                            continue;
                        }

                        heightMap[z << 4 | x] = y;

                        if (y < heightMapMinimum) {
                            heightMapMinimum = y;
                        }
                    }

                    if (chunk.lumi$world().lumi$root().lumi$hasSky()) {
                        int lightLevel = 15;
                        y = topSegment + 16 - 1;

                        do {
                            int blockOpacity = chunk.lumi$getBlockOpacity(x, y, z);

                            if (blockOpacity == 0 && lightLevel != 15) {
                                blockOpacity = 1;
                            }

                            lightLevel -= blockOpacity;

                            if (lightLevel > 0) {
                                val ebs = chunk.lumi$subChunk(y >> 4);

                                if (ebs != null) {
                                    ebs.lumi$setSkyLightValue(x, y & 15, z, lightLevel);
                                    chunk.lumi$world().lumi$root().lumi$markBlockForRenderUpdate((chunk.lumi$chunkPosX() << 4) + x, y, (chunk.lumi$chunkPosZ() << 4) + z);
                                }
                            }

                            --y;
                        }
                        while (y > 0 && lightLevel > 0);
                    }

                    ++z;
                    break;
                }
            }
        }

        chunk.lumi$root().lumi$markDirty();
        chunk.lumi$minSkyLightHeight(heightMapMinimum);
    }

    /**
     * Generates the height map for a chunk from scratch
     *
     * @param chunk
     */
    @SideOnly(Side.CLIENT)
    public static void generateHeightMap(LumiChunk chunk) {
        int i = chunk.lumi$root().lumi$topPreparedSubChunkPosY();
        int heightMapMinimum = Integer.MAX_VALUE;
        val heightMap = chunk.lumi$skyLightHeights();

        for (int j = 0; j < 16; ++j) {
            int k = 0;

            while (k < 16) {
                val vanillaChunk = chunk.lumi$root().lumi$base();
                vanillaChunk.precipitationHeightMap[j + (k << 4)] = -999;
                int l = i + 16 - 1;

                while (true) {
                    if (l > 0) {
                        val blockOpacity = chunk.lumi$getBlockOpacity(j, l - 1, k);
                        if (blockOpacity == 0) {
                            --l;
                            continue;
                        }

                        heightMap[k << 4 | j] = l;

                        if (l < heightMapMinimum) {
                            heightMapMinimum = l;
                        }
                    }

                    ++k;
                    break;
                }
            }
        }

        chunk.lumi$minSkyLightHeight(heightMapMinimum);
        chunk.lumi$root().lumi$markDirty();
    }


    public static void flagSecBoundaryForUpdate(final LumiChunk chunk, final BlockPos pos, final EnumSkyBlock lightType, final EnumFacing dir,
                                                final EnumBoundaryFacing boundaryFacing) {
        flagChunkBoundaryForUpdate(chunk, (short) (1 << (pos.getY() >> 4)), lightType, dir, getAxisDirection(dir, pos.getX(), pos.getZ()), boundaryFacing);
    }

    public static void flagChunkBoundaryForUpdate(final LumiChunk chunk, final short sectionMask, final EnumSkyBlock lightType, final EnumFacing dir,
                                                  final AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        chunk.lumi$neighborLightCheckFlags()[getFlagIndex(lightType, dir, axisDirection, boundaryFacing)] |= sectionMask;
        chunk.lumi$root().lumi$markDirty();
    }

    public static int getFlagIndex(final EnumSkyBlock lightType, final int xOffset, final int zOffset, final AxisDirection axisDirection,
                                   final EnumBoundaryFacing boundaryFacing) {
        return (lightType == EnumSkyBlock.Block ? 0 : 16) | ((xOffset + 1) << 2) | ((zOffset + 1) << 1) | (axisDirection.getOffset() + 1) | boundaryFacing
                .ordinal();
    }

    public static int getFlagIndex(final EnumSkyBlock lightType, final EnumFacing dir, final AxisDirection axisDirection,
                                   final EnumBoundaryFacing boundaryFacing) {
        return getFlagIndex(lightType, dir.getFrontOffsetX(), dir.getFrontOffsetZ(), axisDirection, boundaryFacing);
    }

    private static AxisDirection getAxisDirection(final EnumFacing dir, final int x, final int z) {
        return (((dir == EnumFacing.EAST || dir == EnumFacing.WEST) ? z : x) & 15) < 8 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE;
    }

    public static void scheduleRelightChecksForChunkBoundaries(final LumiWorld world, final LumiChunk chunk) {
        for (final EnumFacing dir : HORIZONTAL_FACINGS) {
            final int xOffset = dir.getFrontOffsetX();
            final int zOffset = dir.getFrontOffsetZ();

            final LumiChunk nChunk = getLoadedChunk(chunk.lumi$world(), chunk.lumi$chunkPosX() + xOffset, chunk.lumi$chunkPosZ() + zOffset);

            if (nChunk == null)
                continue;

            for (final EnumSkyBlock lightType : ENUM_SKY_BLOCK_VALUES) {
                for (final AxisDirection axisDir : ENUM_AXIS_DIRECTION_VALUES) {
                    //Merge flags upon loading of a chunk. This ensures that all flags are always already on the IN boundary below
                    mergeFlags(lightType, chunk, nChunk, dir, axisDir);
                    mergeFlags(lightType, nChunk, chunk, getOpposite(dir), axisDir);

                    //Check everything that might have been canceled due to this chunk not being loaded.
                    //Also, pass in chunks if already known
                    //The boundary to the neighbor chunk (both ways)
                    scheduleRelightChecksForBoundary(world, chunk, nChunk, null, lightType, xOffset, zOffset, axisDir);
                    scheduleRelightChecksForBoundary(world, nChunk, chunk, null, lightType, -xOffset, -zOffset, axisDir);
                    //The boundary to the diagonal neighbor (since the checks in that chunk were aborted if this chunk wasn't loaded, see scheduleRelightChecksForBoundary)
                    scheduleRelightChecksForBoundary(world, nChunk, null, chunk, lightType, (zOffset != 0 ? axisDir.getOffset() : 0),
                                                     (xOffset != 0 ? axisDir.getOffset() : 0), getAxisDirection(dir) == AxisDirection.POSITIVE ?
                                                             AxisDirection.NEGATIVE :
                                                             AxisDirection.POSITIVE);
                }
            }
        }
    }

    private static void mergeFlags(final EnumSkyBlock lightType, final LumiChunk inChunk, final LumiChunk outChunk, final EnumFacing dir,
                                   final AxisDirection axisDir) {
        if (outChunk.lumi$neighborLightCheckFlags() == null) {
            return;
        }

        final int inIndex = getFlagIndex(lightType, dir, axisDir, EnumBoundaryFacing.IN);
        final int outIndex = getFlagIndex(lightType, getOpposite(dir), axisDir, EnumBoundaryFacing.OUT);

        inChunk.lumi$neighborLightCheckFlags()[inIndex] |= outChunk.lumi$neighborLightCheckFlags()[outIndex];
        //no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    private static void scheduleRelightChecksForBoundary(final LumiWorld world, final LumiChunk chunk, LumiChunk nChunk, LumiChunk sChunk, final EnumSkyBlock lightType,
                                                         final int xOffset, final int zOffset, final AxisDirection axisDir) {
        if (chunk.lumi$neighborLightCheckFlags() == null) {
            return;
        }

        final int flagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDir, EnumBoundaryFacing.IN); //OUT checks from neighbor are already merged

        final int flags = chunk.lumi$neighborLightCheckFlags()[flagIndex];

        if (flags == 0) {
            return;
        }

        if (nChunk == null) {
            nChunk = getLoadedChunk(world, chunk.lumi$chunkPosX() + xOffset, chunk.lumi$chunkPosZ() + zOffset);
            if (nChunk == null)
                return;
        }

        if (sChunk == null) {
            int theX = chunk.lumi$chunkPosX() + (zOffset != 0 ? axisDir.getOffset() : 0);
            int theZ = chunk.lumi$chunkPosZ() + (xOffset != 0 ? axisDir.getOffset() : 0);

            sChunk = getLoadedChunk(world, theX, theZ);
            if (sChunk == null)
                return;
        }

        final int reverseIndex = getFlagIndex(lightType, -xOffset, -zOffset, axisDir, EnumBoundaryFacing.OUT);

        chunk.lumi$neighborLightCheckFlags()[flagIndex] = 0;

        if (nChunk.lumi$neighborLightCheckFlags() != null) {
            nChunk.lumi$neighborLightCheckFlags()[reverseIndex] = 0; //Clear only now that it's clear that the checks are processed
        }

        chunk.lumi$root().lumi$markDirty();
        nChunk.lumi$root().lumi$markDirty();

        //Get the area to check
        //Start in the corner...
        int xMin = chunk.lumi$chunkPosX() << 4;
        int zMin = chunk.lumi$chunkPosZ() << 4;

        //move to other side of chunk if the direction is positive
        if ((xOffset | zOffset) > 0) {
            xMin += 15 * xOffset;
            zMin += 15 * zOffset;
        }

        //shift to other half if necessary (shift perpendicular to dir)
        if (axisDir == AxisDirection.POSITIVE) {
            xMin += 8 * (zOffset & 1); //x & 1 is same as abs(x) for x=-1,0,1
            zMin += 8 * (xOffset & 1);
        }

        //get maximal values (shift perpendicular to dir)
        final int xMax = xMin + 7 * (zOffset & 1);
        final int zMax = zMin + 7 * (xOffset & 1);

        for (int y = 0; y < 16; ++y) {
            if ((flags & (1 << y)) != 0) {
                scheduleRelightChecksForArea(world, lightType, xMin, y << 4, zMin, xMax, (y << 4) + 15, zMax);
            }
        }
    }

    public static void writeNeighborLightChecksToNBT(LumiChunk chunk, NBTTagCompound output) {
        val neighborLightCheckFlags = chunk.lumi$neighborLightCheckFlags();

        var empty = true;

        val flagList = new NBTTagList();
        for (val flag : neighborLightCheckFlags) {
            val flagTag = new NBTTagShort(flag);
            flagList.appendTag(new NBTTagShort(flag));
            if (flag != 0)
                empty = false;
        }

        if (!empty)
            output.setTag(NEIGHBOR_LIGHT_CHECKS_KEY, flagList);
    }

    public static void readNeighborLightChecksFromNBT(LumiChunk chunk, NBTTagCompound input) {
        if (!input.hasKey(NEIGHBOR_LIGHT_CHECKS_KEY, 9))
            return;

        val list = input.getTagList(NEIGHBOR_LIGHT_CHECKS_KEY, 2);
        if (list.tagCount() != FLAG_COUNT) {
            Share.LOG.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})",
                           NEIGHBOR_LIGHT_CHECKS_KEY,
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

    public static void initChunkLighting(LumiWorld world, LumiChunk chunk) {
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
            for (int subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                for (int subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (int subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
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
            lumiSetSkylightUpdatedPublic(chunk);
            doRecheckGaps(chunk, false);
        }

        chunk.lumi$hasLightInitialized(true);
    }

    public static boolean checkChunkLighting(LumiWorld world, LumiChunk chunk) {
        if (!chunk.lumi$hasLightInitialized())
            initChunkLighting(world, chunk);

        for (int xOffset = -1; xOffset <= 1; ++xOffset) {
            for (int zOffset = -1; zOffset <= 1; ++zOffset) {
                if (xOffset == 0 && zOffset == 0)
                    continue;

                val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
                val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;
                val chunkNeighbour = getLoadedChunk(world, chunkPosX, chunkPosZ);

                if (chunkNeighbour == null || !chunkNeighbour.lumi$hasLightInitialized())
                    return false;
            }
        }

        return true;
    }

    public static void initSkyLightForSubChunk(LumiWorld world, LumiChunk chunk, LumiSubChunk subChunk) {
        if (!world.lumi$root().lumi$hasSky())
            return;

        val maxPosY = subChunk.lumi$root().lumi$posY();
        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
            for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                if (lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ) <= maxPosY) {
                    for (var posY = 0; posY < 16; posY++)
                        subChunk.lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, EnumSkyBlock.Sky.defaultLightValue);
                }
            }
        }
    }

    private static AxisDirection getAxisDirection(EnumFacing in) {
        switch (in) {
            case DOWN:
            case NORTH:
            case WEST:
                return AxisDirection.NEGATIVE;
            default:
                return AxisDirection.POSITIVE;
        }
    }

    private static EnumFacing getOpposite(EnumFacing in) {
        switch (in) {
            case NORTH:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.NORTH;
            case EAST:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.EAST;
            case DOWN:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            default:
                throw new IllegalArgumentException();
        }
    }

    public enum EnumBoundaryFacing {
        IN, OUT;

        public EnumBoundaryFacing getOpposite() {
            return this == IN ? OUT : IN;
        }
    }
}
