package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.Setter;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;


public final class DynamicBlockCacheRoot implements LumiBlockCacheRoot {
    static final int CHUNK_XZ_SIZE = 16;
    static final int CHUNK_XZ_BITMASK = 15;
    static final int CHUNK_Y_SIZE = 256;
    static final int CHUNK_Y_BITMASK = 255;
    static final int CACHE_CHUNK_XZ_SIZE = 3;
    static final int CENTER_TO_MIN_DISTANCE = CACHE_CHUNK_XZ_SIZE / 2;
    static final int TOTAL_CACHED_CHUNK_COUNT = CACHE_CHUNK_XZ_SIZE * CACHE_CHUNK_XZ_SIZE;
    static final int ELEMENT_COUNT_PER_CHUNK = CHUNK_XZ_SIZE * CHUNK_XZ_SIZE * CHUNK_Y_SIZE;
    static final int ELEMENT_COUNT_PER_CACHED_THING = TOTAL_CACHED_CHUNK_COUNT * ELEMENT_COUNT_PER_CHUNK;

    static final int BITSIZE_CHUNK_XZ = 4;
    static final int BITSIZE_CHUNK_Y = 8;
    static final int BITSHIFT_CHUNK_Z = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK_X = BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;

    private final LumiWorldRoot worldRoot;

    @Setter
    private DynamicBlockCache worldCache;

    // Z/X 3/3
    private final LumiChunkRoot[] rootChunks = new LumiChunkRoot[TOTAL_CACHED_CHUNK_COUNT];
    // Used for populating
    private final ChunkCacheCompact helperCache = new ChunkCacheCompact();

    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final Block[] blocks = new Block[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final int[] blockMetas = new int[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final TileEntity[] tileEntities = new TileEntity[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final BitSet airChecks = new BitSet(ELEMENT_COUNT_PER_CACHED_THING);

    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final BitSet checkedBlocks = new BitSet(ELEMENT_COUNT_PER_CACHED_THING);

    private int minChunkPosX;
    private int minChunkPosZ;
    private int maxChunkPosX;
    private int maxChunkPosZ;

    private boolean isReady;

    public DynamicBlockCacheRoot(@NotNull LumiWorldRoot worldRoot) {
        this.worldRoot = worldRoot;
        // Initialized in [com.falsepattern.lumina.internal.mixin.mixins.common.lumi.LumiWorldImplMixin]
        //noinspection DataFlowIssue
        this.worldCache = null;
    }

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_dynamic_block_cache_root";
    }

    @Override
    public void lumi$clearCache() {
        if (!isReady)
            return;

        worldCache.lumi$clearCache();
        // We don't need to clear the "blocks" array because blocks are singletons
        Arrays.fill(tileEntities, null);
        Arrays.fill(rootChunks, null);
        checkedBlocks.clear();

        isReady = false;
    }

    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_dynamic_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return worldRoot.lumi$isClientSide();
    }

    @Override
    public boolean lumi$hasSky() {
        return worldRoot.lumi$hasSky();
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        return blocks[getIndex(posX, posY, posZ)];
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return blockMetas[getIndex(posX, posY, posZ)];
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return airChecks.get(getIndex(posX, posY, posZ));
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return tileEntities[getIndex(posX, posY, posZ)];
    }

    private int getIndex(int posX, int posY, int posZ) {
        val theChunk = chunkFromBlockPos(posX, posZ);
        val cacheIndex = cacheIndexFromBlockPos(posX, posY, posZ);
        if (checkedBlocks.get(cacheIndex))
            return cacheIndex;

        if (theChunk == null) {
            blocks[cacheIndex] = Blocks.air;
            blockMetas[cacheIndex] = 0;
            tileEntities[cacheIndex] = null;
            airChecks.clear(cacheIndex);
            checkedBlocks.clear(cacheIndex);
        } else {
            val subChunkX = posX & CHUNK_XZ_BITMASK;
            val subChunkZ = posZ & CHUNK_XZ_BITMASK;

            val block = blocks[cacheIndex] = theChunk.lumi$getBlock(subChunkX, posY, subChunkZ);
            val meta = blockMetas[cacheIndex] = theChunk.lumi$getBlockMeta(subChunkX, posY, subChunkZ);

            tileEntities[cacheIndex] = ((Chunk) theChunk).getTileEntityUnsafe(subChunkX, posY, subChunkZ);

            airChecks.set(cacheIndex, block.isAir(helperCache, posX, posY, posZ));

            checkedBlocks.set(cacheIndex);
        }
        return cacheIndex;
    }

    private void setupCache(int centerChunkPosX, int centerChunkPosZ) {
        val minChunkPosX = centerChunkPosX - CENTER_TO_MIN_DISTANCE;
        val minChunkPosZ = centerChunkPosZ - CENTER_TO_MIN_DISTANCE;

        val maxChunkPosX = minChunkPosX + CACHE_CHUNK_XZ_SIZE;
        val maxChunkPosZ = minChunkPosZ + CACHE_CHUNK_XZ_SIZE;

        for (var chunkPosZ = 0; chunkPosZ < CACHE_CHUNK_XZ_SIZE; chunkPosZ++) {
            val realChunkPosZ = chunkPosZ + minChunkPosZ;
            for (var chunkPosX = 0; chunkPosX < CACHE_CHUNK_XZ_SIZE; chunkPosX++) {
                val rootChunkIndex = (chunkPosZ * CACHE_CHUNK_XZ_SIZE) + chunkPosX;
                val realChunkPosX = chunkPosX + minChunkPosX;

                val chunkProvider = worldRoot.lumi$chunkProvider();
                chunkExistsCheck:
                {
                    if (!chunkProvider.chunkExists(realChunkPosX, realChunkPosZ))
                        break chunkExistsCheck;
                    val chunkBase = chunkProvider.provideChunk(realChunkPosX, realChunkPosZ);
                    if (!(chunkBase instanceof LumiChunkRoot))
                        break chunkExistsCheck;
                    val chunkRoot = (LumiChunkRoot) chunkBase;
                    rootChunks[rootChunkIndex] = chunkRoot;
                }
                rootChunks[rootChunkIndex] = null;
            }
        }
        helperCache.init(worldRoot, rootChunks, CACHE_CHUNK_XZ_SIZE, minChunkPosX, minChunkPosZ);
        this.minChunkPosX = minChunkPosX;
        this.minChunkPosZ = minChunkPosZ;
        this.maxChunkPosX = maxChunkPosX;
        this.maxChunkPosZ = maxChunkPosZ;
        checkedBlocks.clear();
        worldCache.lumi$clearCache();
        Arrays.fill(tileEntities, null);
        isReady = true;
    }

    int cacheIndexFromBlockPos(int posX, int posY, int posZ) {
        val chunkPosZ = (posZ >> BITSIZE_CHUNK_XZ) - minChunkPosZ;
        val chunkPosX = (posX >> BITSIZE_CHUNK_XZ) - minChunkPosX;

        // val chunkBase = (chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX) * ELEMENT_COUNT_PER_CHUNK;
        // chunk element count is always 16*16*256, so we optimize away the multiply
        val chunkBase = (chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX) << BITSHIFT_CHUNK;

        val subChunkZ = posZ & CHUNK_XZ_BITMASK;
        val subChunkX = posX & CHUNK_XZ_BITMASK;
        val subChunkY = posY & CHUNK_Y_BITMASK;

        //val subChunkOffset = (subChunkZ * CHUNK_XZ_SIZE + subChunkX) * CHUNK_Y_SIZE + subChunkY;
        //All these are constants so we can reduce it to bit shuffling
        val subChunkOffset = (subChunkZ << BITSHIFT_CHUNK_Z) | (subChunkX << BITSHIFT_CHUNK_X) | subChunkY;
        int index = chunkBase | subChunkOffset;
        if (index < 0 || index >= blocks.length) {
            chunkFromBlockPos(posX, posZ);
            return cacheIndexFromBlockPos(posX, posY, posZ);
        } else {
            return index;
        }
    }

    private @Nullable LumiChunkRoot chunkFromBlockPos(int posX, int posZ) {
        val baseChunkPosX = posX >> BITSIZE_CHUNK_XZ;
        val baseChunkPosZ = posZ >> BITSIZE_CHUNK_XZ;
        if (!isReady) {
            setupCache(baseChunkPosX, baseChunkPosZ);
        }

        if (baseChunkPosX < minChunkPosX || baseChunkPosX >= maxChunkPosX ||
            baseChunkPosZ < minChunkPosZ || baseChunkPosZ >= maxChunkPosZ) {
            // TODO smarter shifting logic here
            setupCache(baseChunkPosX, baseChunkPosZ);
        }
        val chunkPosX = baseChunkPosX - minChunkPosX;
        val chunkPosZ = baseChunkPosZ - minChunkPosZ;

        val rootChunk = rootChunks[chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX];
        if (rootChunk != null)
            return rootChunk;

        val chunkProvider = worldRoot.lumi$chunkProvider();
        if (!chunkProvider.chunkExists(baseChunkPosX, baseChunkPosZ))
            return null;
        val chunkBase = chunkProvider.provideChunk(baseChunkPosX, baseChunkPosZ);
        if (!(chunkBase instanceof LumiChunkRoot))
            return null;

        return rootChunks[chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX] = (LumiChunkRoot) chunkBase;
    }
}
