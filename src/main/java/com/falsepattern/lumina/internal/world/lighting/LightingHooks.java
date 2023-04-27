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

import com.falsepattern.lib.internal.Share;
import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.internal.world.WorldChunkSlice;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Arrays;

@SuppressWarnings("unused")
public class LightingHooks {
    private static final EnumSkyBlock[] ENUM_SKY_BLOCK_VALUES = EnumSkyBlock.values();

    private static final AxisDirection[] ENUM_AXIS_DIRECTION_VALUES = AxisDirection.values();

    public static final EnumFacing[] HORIZONTAL_FACINGS = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST };
    private static final EnumFacing[] HORIZONTAL = HORIZONTAL_FACINGS;

    private static final int FLAG_COUNT = 32; //2 light types * 4 directions * 2 halves * (inwards + outwards)

    public static void relightSkylightColumn(final ILumiWorld world, final ILumiChunk chunk, final int x, final int z, final int height1, final int height2) {
        final int yMin = Math.min(height1, height2);
        final int yMax = Math.max(height1, height2) - 1;

        final int xBase = (chunk.x() << 4) + x;
        final int zBase = (chunk.z() << 4) + z;

        scheduleRelightChecksForColumn(world, EnumSkyBlock.Sky, xBase, zBase, yMin, yMax);

        if (chunk.lumiEBS(yMin >> 4) == null && yMin > 0) {
            chunk.getLightingEngine().scheduleLightUpdate(EnumSkyBlock.Sky, xBase, yMin - 1, zBase);
        }

        short emptySections = 0;

        for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
            if (chunk.lumiEBS(sec) == null) {
                emptySections |= 1 << sec;
            }
        }

        if (emptySections != 0) {
            for (final EnumFacing dir : HORIZONTAL_FACINGS) {
                final int xOffset = dir.getFrontOffsetX();
                final int zOffset = dir.getFrontOffsetZ();

                final boolean neighborColumnExists =
                        (((x + xOffset) | (z + zOffset)) & 16) == 0
                                //Checks whether the position is at the specified border (the 16 bit is set for both 15+1 and 0-1)
                                || LightingEngineHelpers.getLoadedChunk(world, chunk.x() + xOffset, chunk.z() + zOffset) != null;

                if (neighborColumnExists) {
                    for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
                        if ((emptySections & (1 << sec)) != 0) {
                            scheduleRelightChecksForColumn(world, EnumSkyBlock.Sky, xBase + xOffset, zBase + zOffset, sec << 4, (sec << 4) + 15);
                        }
                    }
                } else {
                    flagChunkBoundaryForUpdate(chunk, emptySections, EnumSkyBlock.Sky, dir, getAxisDirection(dir, x, z), EnumBoundaryFacing.OUT);
                }
            }
        }
    }

    public static void scheduleRelightChecksForArea(final ILumiWorld world, final EnumSkyBlock lightType, final int xMin, final int yMin, final int zMin,
                                                    final int xMax, final int yMax, final int zMax) {
        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                scheduleRelightChecksForColumn(world, lightType, x, z, yMin, yMax);
            }
        }
    }

    private static void scheduleRelightChecksForColumn(final ILumiWorld world, final EnumSkyBlock lightType, final int x, final int z, final int yMin, final int yMax) {
        for (int y = yMin; y <= yMax; ++y) {
            world.getLightingEngine().scheduleLightUpdate(lightType, x, y, z);
        }
    }


    private static int getBlockLightOpacity(ILumiChunk chunk, int x, int y, int z) {
        return chunk.lumiWorld().lumiGetLightOpacity(chunk.root().rootGetBlock(x, y, z), chunk.root().rootGetBlockMetadata(x, y, z), x, y, z);
    }

    public static void relightBlock(ILumiChunk chunk, int x, int y, int z) {
        int i = lumiGetHeightValue(chunk, x, z) & 255;
        int j = i;

        if (y > i) {
            j = y;
        }

        while (j > 0 && getBlockLightOpacity(chunk, x, j - 1, z) == 0) {
            --j;
        }

        if (j != i) {
            lumiSetHeightValue(chunk, x, z, j);

            if (!chunk.lumiWorld().root().rootHasNoSky()) {
                relightSkylightColumn(chunk.lumiWorld(), chunk, x, z, i, j);
            }

            int l1 = lumiGetHeightValue(chunk, x, z);

            if (l1 < chunk.lumiHeightMapMinimum()) {
                chunk.lumiHeightMapMinimum(l1);
            }
        }
    }

    public static void doRecheckGaps(ILumiChunk chunk, boolean onlyOne) {
        val worldRoot = chunk.lumiWorld().root();
        val prof = worldRoot.rootTheProfiler();
        prof.startSection("recheckGaps");

        WorldChunkSlice slice = new WorldChunkSlice(chunk.lumiWorld(), chunk.x(), chunk.z());
        if (worldRoot.rootDoChunksNearChunkExist(chunk.x() * 16 + 8, 0, chunk.z() * 16 + 8, 16)) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (recheckGapsForColumn(chunk, slice, x, z)) {
                        if (onlyOne) {
                            prof.endSection();

                            return;
                        }
                    }
                }
            }

            chunk.root().rootIsGapLightingUpdated(false);
        }

        prof.endSection();
    }

    private static boolean recheckGapsForColumn(ILumiChunk chunk, WorldChunkSlice slice, int x, int z) {
        int i = x + z * 16;

        if (chunk.lumiUpdateSkylightColumns()[i]) {
            chunk.lumiUpdateSkylightColumns()[i] = false;

            int height = lumiGetHeightValue(chunk, x, z);

            int x1 = chunk.x() * 16 + x;
            int z1 = chunk.z() * 16 + z;

            int max = recheckGapsGetLowestHeight(slice, x1, z1);

            recheckGapsSkylightNeighborHeight(chunk, slice, x1, z1, height, max);

            return true;
        }

        return false;
    }

    private static int recheckGapsGetLowestHeight(WorldChunkSlice slice, int x, int z) {
        int max = Integer.MAX_VALUE;

        for (EnumFacing facing : HORIZONTAL) {
            int j = x + facing.getFrontOffsetX();
            int k = z + facing.getFrontOffsetZ();

            max = Math.min(max, slice.getChunkFromWorldCoords(j, k).lumiHeightMapMinimum());
        }

        return max;
    }

    private static void recheckGapsSkylightNeighborHeight(ILumiChunk chunk, WorldChunkSlice slice, int x, int z, int height, int max) {
        checkSkylightNeighborHeight(chunk, slice, x, z, max);

        for (EnumFacing facing : HORIZONTAL) {
            int j = x + facing.getFrontOffsetX();
            int k = z + facing.getFrontOffsetZ();

            checkSkylightNeighborHeight(chunk, slice, j, k, height);
        }
    }

    private static void checkSkylightNeighborHeight(ILumiChunk chunk, WorldChunkSlice slice, int x, int z, int maxValue) {
        int i = lumiGetHeightValue(slice.getChunkFromWorldCoords(x, z), x & 15, z & 15);

        if (i > maxValue) {
            updateSkylightNeighborHeight(chunk, slice, x, z, maxValue, i + 1);
        } else if (i < maxValue) {
            updateSkylightNeighborHeight(chunk, slice, x, z, i, maxValue + 1);
        }
    }

    private static void updateSkylightNeighborHeight(ILumiChunk chunk, WorldChunkSlice slice, int x, int z, int startY, int endY) {
        if (endY > startY) {
            if (!slice.isLoaded(x, z, 16)) {
                return;
            }

            for (int i = startY; i < endY; ++i) {
                chunk.getLightingEngine().scheduleLightUpdate(EnumSkyBlock.Sky, x, i, z);
            }

            chunk.root().rootSetChunkModified();
        }
    }

    public static boolean lumiCanBlockSeeTheSky(ILumiChunk iLumiChunk, int x, int y, int z) {
        return y >= iLumiChunk.lumiHeightMap()[z << 4 | x];
    }

    public static void lumiSetSkylightUpdatedPublic(ILumiChunk iLumiChunk) {
        Arrays.fill(iLumiChunk.lumiUpdateSkylightColumns(), true);
    }

    public static void lumiSetHeightValue(ILumiChunk iLumiChunk, int x, int z, int val) {
        iLumiChunk.lumiHeightMap()[z << 4 | x] = val;
    }

    public static int lumiGetHeightValue(ILumiChunk iLumiChunk, int x, int z) {
        return iLumiChunk.lumiHeightMap()[z << 4 | x];
    }

    public static int getCachedLightFor(ILumiChunk iLumiChunk, EnumSkyBlock type, int xIn, int yIn, int zIn) {
        int i = xIn & 15;
        int j = yIn;
        int k = zIn & 15;

        ILumiEBS extendedblockstorage = iLumiChunk.lumiEBS(j >> 4);

        if (extendedblockstorage == null) {
            if (lumiCanBlockSeeTheSky(iLumiChunk, i, j, k)) {
                return type.defaultLightValue;
            } else {
                return 0;
            }
        } else if (type == EnumSkyBlock.Sky) {
            if (iLumiChunk.lumiWorld().root().rootHasNoSky()) {
                return 0;
            } else {
                return lumiGetSkylight(extendedblockstorage, i, j & 15, k);
            }
        } else {
            if (type == EnumSkyBlock.Block) {
                return lumiGetBlocklight(extendedblockstorage, i, j & 15, k);
            } else {
                return type.defaultLightValue;
            }
        }
    }

    public static void lumiSetLightValue(ILumiChunk iLumiChunk, EnumSkyBlock enumSkyBlock, int x, int y, int z, int lightValue) {
        iLumiChunk.root().rootEnsureEBSPresent(y);
        val ebs = iLumiChunk.lumiEBS(y >>> 4);

        if (enumSkyBlock == EnumSkyBlock.Sky) {
            if (!iLumiChunk.lumiWorld().root().rootHasNoSky()) {
                lumiSetSkylight(ebs, x, y & 15, z, lightValue);
            }
        } else if (enumSkyBlock == EnumSkyBlock.Block) {
            lumiSetBlocklight(ebs, x, y & 15, z, lightValue);
        }
    }

    /**
     * The custom skylight data
     */
    public static int lumiGetSkylight(ILumiEBS iLumiEBS, int x, int y, int z) {
        return iLumiEBS.lumiSkylightArray().get(x, y, z);
    }

    public static void lumiSetSkylight(ILumiEBS iLumiEBS, int x, int y, int z, int defaultLightValue) {
        iLumiEBS.lumiSkylightArray().set(x, y, z, defaultLightValue);
    }

    /**
     * The custom blocklight data
     */
    public static int lumiGetBlocklight(ILumiEBS iLumiEBS, int x, int y, int z) {
        return iLumiEBS.lumiBlocklightArray().get(x, y, z);
    }

    public static void lumiSetBlocklight(ILumiEBS iLumiEBS, int x, int y, int z, int defaultLightValue) {
        iLumiEBS.lumiBlocklightArray().set(x, y, z, defaultLightValue);
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public static void generateSkylightMap(ILumiChunk chunk) {
        val root = chunk.root();
        int topSegment = root.rootGetTopFilledSegment();
        chunk.lumiHeightMapMinimum(Integer.MAX_VALUE);
        int heightMapMinimum = Integer.MAX_VALUE;
        val heightMap = chunk.lumiHeightMap();
        for (int x = 0; x < 16; ++x) {
            int z = 0;
            while (z < 16) {
                chunk.root().rootPrecipitationHeightMap()[x + (z << 4)] = -999;
                int y = topSegment + 16 - 1;

                while (true) {
                    if (y > 0) {
                        if (getLightOpacity(chunk, x, y - 1, z) == 0) {
                            --y;
                            continue;
                        }

                        heightMap[z << 4 | x] = y;

                        if (y < heightMapMinimum) {
                            heightMapMinimum = y;
                        }
                    }

                    if (!chunk.lumiWorld().root().rootHasNoSky()) {
                        int lightLevel = 15;
                        y = topSegment + 16 - 1;

                        do {
                            int opacity = getLightOpacity(chunk, x, y, z);

                            if (opacity == 0 && lightLevel != 15) {
                                opacity = 1;
                            }

                            lightLevel -= opacity;

                            if (lightLevel > 0) {
                                val ebs = chunk.lumiEBS(y >> 4);

                                if (ebs != null) {
                                    ebs.lumiSkylightArray().set(x, y & 15, z, lightLevel);
                                    chunk.lumiWorld().root().rootMarkBlockForRenderUpdate((chunk.x() << 4) + x, y, (chunk.z() << 4) + z);
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

        chunk.root().rootSetChunkModified();
        chunk.lumiHeightMapMinimum(heightMapMinimum);
    }

    /**
     * Generates the height map for a chunk from scratch
     * @param chunk
     */
    @SideOnly(Side.CLIENT)
    public static void generateHeightMap(ILumiChunk chunk) {
        int i = chunk.root().rootGetTopFilledSegment();
        int heightMapMinimum = Integer.MAX_VALUE;
        val heightMap = chunk.lumiHeightMap();

        for (int j = 0; j < 16; ++j) {
            int k = 0;

            while (k < 16) {
                chunk.root().rootPrecipitationHeightMap()[j + (k << 4)] = -999;
                int l = i + 16 - 1;

                while (true) {
                    if (l > 0) {
                        if (getLightOpacity(chunk, j, l - 1, k) == 0) {
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

        chunk.lumiHeightMapMinimum(heightMapMinimum);
        chunk.root().rootSetChunkModified();
    }

    public enum EnumBoundaryFacing {
        IN, OUT;

        public EnumBoundaryFacing getOpposite() {
            return this == IN ? OUT : IN;
        }
    }

    public static void flagSecBoundaryForUpdate(final ILumiChunk chunk, final BlockPos pos, final EnumSkyBlock lightType, final EnumFacing dir,
                                                final EnumBoundaryFacing boundaryFacing) {
        flagChunkBoundaryForUpdate(chunk, (short) (1 << (pos.getY() >> 4)), lightType, dir, getAxisDirection(dir, pos.getX(), pos.getZ()), boundaryFacing);
    }

    public static void flagChunkBoundaryForUpdate(final ILumiChunk chunk, final short sectionMask, final EnumSkyBlock lightType, final EnumFacing dir,
                                                  final AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        initNeighborLightChecks(chunk);
        chunk.lumiGetNeighborLightChecks()[getFlagIndex(lightType, dir, axisDirection, boundaryFacing)] |= sectionMask;
        chunk.root().rootSetChunkModified();
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

    private static EnumFacing getOpposite(EnumFacing in) {
        switch(in) {
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

    public static void scheduleRelightChecksForChunkBoundaries(final ILumiWorld world, final ILumiChunk chunk) {
        for (final EnumFacing dir : HORIZONTAL_FACINGS) {
            final int xOffset = dir.getFrontOffsetX();
            final int zOffset = dir.getFrontOffsetZ();

            final ILumiChunk nChunk = LightingEngineHelpers.getLoadedChunk(chunk.lumiWorld(), chunk.x() + xOffset, chunk.z() + zOffset);

            if(nChunk == null)
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

    private static void mergeFlags(final EnumSkyBlock lightType, final ILumiChunk inChunk, final ILumiChunk outChunk, final EnumFacing dir,
                                   final AxisDirection axisDir) {
        if (outChunk.lumiGetNeighborLightChecks() == null) {
            return;
        }

        initNeighborLightChecks(inChunk);

        final int inIndex = getFlagIndex(lightType, dir, axisDir, EnumBoundaryFacing.IN);
        final int outIndex = getFlagIndex(lightType, getOpposite(dir), axisDir, EnumBoundaryFacing.OUT);

        inChunk.lumiGetNeighborLightChecks()[inIndex] |= outChunk.lumiGetNeighborLightChecks()[outIndex];
        //no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    private static void scheduleRelightChecksForBoundary(final ILumiWorld world, final ILumiChunk chunk, ILumiChunk nChunk, ILumiChunk sChunk, final EnumSkyBlock lightType,
                                                         final int xOffset, final int zOffset, final AxisDirection axisDir) {
        if (chunk.lumiGetNeighborLightChecks() == null) {
            return;
        }

        final int flagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDir, EnumBoundaryFacing.IN); //OUT checks from neighbor are already merged

        final int flags = chunk.lumiGetNeighborLightChecks()[flagIndex];

        if (flags == 0) {
            return;
        }

        if (nChunk == null) {
            nChunk = LightingEngineHelpers.getLoadedChunk(world,chunk.x() + xOffset, chunk.z() + zOffset);
            if(nChunk == null)
                return;
        }

        if (sChunk == null) {
            int theX = chunk.x() + (zOffset != 0 ? axisDir.getOffset() : 0);
            int theZ = chunk.z() + (xOffset != 0 ? axisDir.getOffset() : 0);

            sChunk = LightingEngineHelpers.getLoadedChunk(world, theX, theZ);
            if(sChunk == null)
                return;
        }

        final int reverseIndex = getFlagIndex(lightType, -xOffset, -zOffset, axisDir, EnumBoundaryFacing.OUT);

        chunk.lumiGetNeighborLightChecks()[flagIndex] = 0;

        if (nChunk.lumiGetNeighborLightChecks() != null) {
            nChunk.lumiGetNeighborLightChecks()[reverseIndex] = 0; //Clear only now that it's clear that the checks are processed
        }

        chunk.root().rootSetChunkModified();
        nChunk.root().rootSetChunkModified();

        //Get the area to check
        //Start in the corner...
        int xMin = chunk.x() << 4;
        int zMin = chunk.z() << 4;

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

    public static void initNeighborLightChecks(final ILumiChunk chunk) {
        if (chunk.lumiGetNeighborLightChecks() == null) {
            chunk.lumiGetNeighborLightChecks(new short[FLAG_COUNT]);
        }
    }

    public static final String neighborLightChecksKey = "NeighborLightChecks";

    public static void writeNeighborLightChecksToNBT(final ILumiChunk chunk, final NBTTagCompound nbt) {
        short[] neighborLightChecks = chunk.lumiGetNeighborLightChecks();

        if (neighborLightChecks == null) {
            return;
        }

        boolean empty = true;

        final NBTTagList list = new NBTTagList();

        for (final short flags : neighborLightChecks) {
            list.appendTag(new NBTTagShort(flags));

            if (flags != 0) {
                empty = false;
            }
        }

        if (!empty) {
            nbt.setTag(neighborLightChecksKey, list);
        }
    }

    public static void readNeighborLightChecksFromNBT(final ILumiChunk chunk, final NBTTagCompound nbt) {
        if (nbt.hasKey(neighborLightChecksKey, 9)) {
            final NBTTagList list = nbt.getTagList(neighborLightChecksKey, 2);

            if (list.tagCount() == FLAG_COUNT) {
                initNeighborLightChecks(chunk);

                short[] neighborLightChecks = chunk.lumiGetNeighborLightChecks();

                for (int i = 0; i < FLAG_COUNT; ++i) {
                    neighborLightChecks[i] = ((NBTTagShort) list.tagList.get(i)).func_150289_e();
                }
            } else {
                Share.LOG.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})", neighborLightChecksKey, chunk.x(), chunk.z());
            }
        }
    }

    public static void initChunkLighting(final ILumiChunk chunk, final ILumiWorld world) {
        final int xBase = chunk.x() << 4;
        final int zBase = chunk.z() << 4;

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(xBase, 0, zBase);

        if (world.root().rootCheckChunksExist(xBase - 16, 0, zBase - 16, xBase + 31, 255, zBase + 31)) {

            for (int j = 0; j < 16; ++j) {
                final ILumiEBS storage = chunk.lumiEBS(j);

                if (storage == null) {
                    continue;
                }

                int yBase = j * 16;

                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            Block block = storage.root().rootGetBlockByExtId(x, y, z);
                            int meta = storage.root().rootGetExtBlockMetadata(x, y, z);
                            if(block != Blocks.air) {
                                pos.setPos(xBase + x, yBase + y, zBase + z);
                                int light = chunk.lumiWorld().lumiGetLightValue(block, meta, pos.getX(), pos.getY(), pos.getZ());

                                if (light > 0) {
                                    world.getLightingEngine().scheduleLightUpdate(EnumSkyBlock.Block, pos.getX(), pos.getY(), pos.getZ());
                                }
                            }
                        }
                    }
                }
            }

            if (!world.root().rootHasNoSky()) {
                lumiSetSkylightUpdatedPublic(chunk);
                doRecheckGaps(chunk, false);
            }

            chunk.lumiIsLightInitialized(true);
        }
    }

    public static boolean checkChunkLighting(final ILumiChunk chunk, final ILumiWorld world) {
        if (!chunk.lumiIsLightInitialized()) {
            initChunkLighting(chunk, world);
        }

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (x != 0 || z != 0) {
                    ILumiChunk nChunk = LightingEngineHelpers.getLoadedChunk(world, chunk.x() + x, chunk.z() + z);

                    if (nChunk == null || !nChunk.lumiIsLightInitialized()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void initSkylightForSection(final ILumiWorld world, final ILumiChunk chunk, final ILumiEBS section) {
        if (!world.root().rootHasNoSky()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (lumiGetHeightValue(chunk, x, z) <= section.root().rootGetYLocation()) {
                        for (int y = 0; y < 16; ++y) {
                            lumiSetSkylight(section, x, y, z, EnumSkyBlock.Sky.defaultLightValue);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the intrinsic or saved block light value in a chunk.
     * @param chunk the chunk
     * @param x X coordinate (0-15)
     * @param y Y coordinate (0-255)
     * @param z Z coordinate (0-15)
     * @return light level
     */
    public static int getIntrinsicOrSavedBlockLightValue(Chunk chunk, int x, int y, int z) {
        int savedLightValue = chunk.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
        int bx = x + (chunk.xPosition * 16);
        int bz = z + (chunk.zPosition * 16);
        Block block = chunk.getBlock(x, y, z);
        int lightValue = block.getLightValue(chunk.worldObj, bx, y, bz);
        return Math.max(savedLightValue, lightValue);
    }

    public static int getLightOpacity(ILumiChunk chunk, int cx, int y, int cz) {
        int x = (chunk.x() << 4) + cx;
        int z = (chunk.z() << 4) + cz;
        return chunk.lumiWorld().lumiGetLightOpacity(chunk.root().rootGetBlock(cx, y, cz), chunk.root().rootGetBlockMetadata(cx, y, cz), x, y, z);
    }
}
