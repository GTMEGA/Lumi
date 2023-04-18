package com.falsepattern.lumina.internal.world.lighting;

import com.falsepattern.lib.internal.Share;
import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.internal.world.WorldChunkSlice;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;

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

        if (chunk.getLumiEBS(yMin >> 4) == null && yMin > 0) {
            world.updateLightByType(EnumSkyBlock.Sky, xBase, yMin - 1, zBase);
        }

        short emptySections = 0;

        for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
            if (chunk.getLumiEBS(sec) == null) {
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
            world.updateLightByType(lightType, x, y, z);
        }
    }


    private static int getBlockLightOpacity(ILumiChunk chunk, int x, int y, int z) {
        return chunk.world().getLightOpacity(chunk.getBlock(x, y, z), x, y, z);
    }

    public static void relightBlock(ILumiChunk chunk, int x, int y, int z) {
        int i = chunk.getHeightValue(x, z) & 255;
        int j = i;

        if (y > i) {
            j = y;
        }

        while (j > 0 && getBlockLightOpacity(chunk, x, j - 1, z) == 0) {
            --j;
        }

        if (j != i) {
            chunk.setHeightValue(x, z, j);

            if (!chunk.world().hasNoSky()) {
                relightSkylightColumn(chunk.world(), chunk, x, z, i, j);
            }

            int l1 = chunk.getHeightValue(x, z);

            if (l1 < chunk.heightMapMinimum()) {
                chunk.heightMapMinimum(l1);
            }
        }
    }

    public static void doRecheckGaps(ILumiChunk chunk, boolean onlyOne) {
        chunk.world().theProfiler().startSection("recheckGaps");

        WorldChunkSlice slice = new WorldChunkSlice(chunk.world(), chunk.x(), chunk.z());
        if (chunk.world().doChunksNearChunkExist(chunk.x() * 16 + 8, 0, chunk.z() * 16 + 8, 16)) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (recheckGapsForColumn(chunk, slice, x, z)) {
                        if (onlyOne) {
                            chunk.world().theProfiler().endSection();

                            return;
                        }
                    }
                }
            }

            chunk.isGapLightingUpdated(false);
        }

        chunk.world().theProfiler().endSection();
    }

    private static boolean recheckGapsForColumn(ILumiChunk chunk, WorldChunkSlice slice, int x, int z) {
        int i = x + z * 16;

        if (chunk.updateSkylightColumns()[i]) {
            chunk.updateSkylightColumns()[i] = false;

            int height = chunk.getHeightValue(x, z);

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

            max = Math.min(max, slice.getChunkFromWorldCoords(j, k).heightMapMinimum());
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
        int i = slice.getChunkFromWorldCoords(x, z).getHeightValue(x & 15, z & 15);

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
                chunk.world().updateLightByType(EnumSkyBlock.Sky, x, i, z);
            }

            chunk.isModified(true);
        }
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
        chunk.getNeighborLightChecks()[getFlagIndex(lightType, dir, axisDirection, boundaryFacing)] |= sectionMask;
        chunk.setChunkModified();
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

            final ILumiChunk nChunk = LightingEngineHelpers.getLoadedChunk(chunk.world(), chunk.x() + xOffset, chunk.z() + zOffset);

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
        if (outChunk.getNeighborLightChecks() == null) {
            return;
        }

        initNeighborLightChecks(inChunk);

        final int inIndex = getFlagIndex(lightType, dir, axisDir, EnumBoundaryFacing.IN);
        final int outIndex = getFlagIndex(lightType, getOpposite(dir), axisDir, EnumBoundaryFacing.OUT);

        inChunk.getNeighborLightChecks()[inIndex] |= outChunk.getNeighborLightChecks()[outIndex];
        //no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    private static void scheduleRelightChecksForBoundary(final ILumiWorld world, final ILumiChunk chunk, ILumiChunk nChunk, ILumiChunk sChunk, final EnumSkyBlock lightType,
                                                         final int xOffset, final int zOffset, final AxisDirection axisDir) {
        if (chunk.getNeighborLightChecks() == null) {
            return;
        }

        final int flagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDir, EnumBoundaryFacing.IN); //OUT checks from neighbor are already merged

        final int flags = chunk.getNeighborLightChecks()[flagIndex];

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

        chunk.getNeighborLightChecks()[flagIndex] = 0;

        if (nChunk.getNeighborLightChecks() != null) {
            nChunk.getNeighborLightChecks()[reverseIndex] = 0; //Clear only now that it's clear that the checks are processed
        }

        chunk.setChunkModified();
        nChunk.setChunkModified();

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
        if (chunk.getNeighborLightChecks() == null) {
            chunk.setNeighborLightChecks(new short[FLAG_COUNT]);
        }
    }

    public static final String neighborLightChecksKey = "NeighborLightChecks";

    public static void writeNeighborLightChecksToNBT(final ILumiChunk chunk, final NBTTagCompound nbt) {
        short[] neighborLightChecks = chunk.getNeighborLightChecks();

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

                short[] neighborLightChecks = chunk.getNeighborLightChecks();

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

        if (world.checkChunksExist(xBase - 16, 0, zBase - 16, xBase + 31, 255, zBase + 31)) {

            for (int j = 0; j < 16; ++j) {
                final ILumiEBS storage = chunk.getLumiEBS(j);

                if (storage == null) {
                    continue;
                }

                int yBase = j * 16;

                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            Block block = storage.getBlockByExtId(x, y, z);
                            if(block != Blocks.air) {
                                pos.setPos(xBase + x, yBase + y, zBase + z);
                                int light = chunk.world().getLightValueForState(block, pos.getX(), pos.getY(), pos.getZ());

                                if (light > 0) {
                                    world.updateLightByType(EnumSkyBlock.Block, pos.getX(), pos.getY(), pos.getZ());
                                }
                            }
                        }
                    }
                }
            }

            if (!world.hasNoSky()) {
                chunk.setSkylightUpdatedPublic();
            }

            chunk.setLightInitialized(true);
        }
    }

    public static void checkChunkLighting(final ILumiChunk chunk, final ILumiWorld world) {
        if (!chunk.isLightInitialized()) {
            initChunkLighting(chunk, world);
        }

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (x != 0 || z != 0) {
                    ILumiChunk nChunk = LightingEngineHelpers.getLoadedChunk(world, chunk.x() + x, chunk.z() + z);

                    if (nChunk == null || !nChunk.isLightInitialized()) {
                        return;
                    }
                }
            }
        }

        chunk.isLightPopulated(true);
    }

    public static void initSkylightForSection(final ILumiWorld world, final ILumiChunk chunk, final ILumiEBS section) {
        if (!world.hasNoSky()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (chunk.getHeightValue(x, z) <= section.getYLocation()) {
                        for (int y = 0; y < 16; ++y) {
                            section.setExtSkylightValue(x, y, z, EnumSkyBlock.Sky.defaultLightValue);
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
    public static int getIntrinsicOrSavedBlockLightValue(ILumiChunk chunk, int x, int y, int z) {
        int savedLightValue = chunk.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
        int bx = x + (chunk.x() * 16);
        int bz = z + (chunk.z() * 16);
        Block block = chunk.getBlock(x, y, z);
        int lightValue = chunk.world().getLightValueForState(block, bx, y, bz);
        return Math.max(savedLightValue, lightValue);
    }
}
