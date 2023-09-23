package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;


// TODO: On first call, this should become ready if it is not already ready
public class DynamicBlockCacheRoot implements LumiBlockCacheRoot {
    static final int CHUNK_XZ_SIZE = 16;
    static final int CHUNK_XZ_BITMASK = 15;
    static final int CHUNK_Y_SIZE = 256;
    static final int CHUNK_Y_BITMASK = 255;
    static final int CACHE_CHUNK_XZ_SIZE = 3;
    static final int TOTAL_CACHED_CHUNK_COUNT = CACHE_CHUNK_XZ_SIZE * CACHE_CHUNK_XZ_SIZE;
    static final int ELEMENT_COUNT_PER_CHUNK = CHUNK_XZ_SIZE * CHUNK_XZ_SIZE * CHUNK_Y_SIZE;
    static final int ELEMENT_COUNT_PER_CACHED_THING = TOTAL_CACHED_CHUNK_COUNT * ELEMENT_COUNT_PER_CHUNK;

    static final int BITSIZE_CHUNK_XZ = 4;
    static final int BITSIZE_CHUNK_Y = 8;
    static final int BITSHIFT_CHUNK_Z = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK_X = BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;

    private final DynamicBlockCache cache;

    private final LumiWorldRoot worldRoot;

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

    private boolean isReady;

    public DynamicBlockCacheRoot(LumiWorldRoot worldRoot) {
        this.worldRoot = worldRoot;
        this.cache = new DynamicBlockCache(this);
    }

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_dynamic_block_cache_root";
    }

    @Override
    public void lumi$clearCache() {
        isReady = false;
        checkedBlocks.clear();
        Arrays.fill(tileEntities, null);
        Arrays.fill(rootChunks, null);
        cache.resetCache();
        // We don't need to clear the "blocks" array because blocks are singletons
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
        val index = cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return blocks[index];
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        val index = cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return blockMetas[index];
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        val index = cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return airChecks.get(index);
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        val index = cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return tileEntities[index];
    }

    private void prepareBlock(int cacheIndex, int posX, int posY, int posZ) {
        if (checkedBlocks.get(cacheIndex))
            return;

        val theChunk = chunkFromBlockPos(posX, posZ);

        val subChunkX = posX & CHUNK_XZ_BITMASK;
        val subChunkZ = posZ & CHUNK_XZ_BITMASK;

        val block = blocks[cacheIndex] = theChunk.lumi$getBlock(subChunkX, posY, subChunkZ);
        val meta = blockMetas[cacheIndex] = theChunk.lumi$getBlockMeta(subChunkX, posY, subChunkZ);

        tileEntities[cacheIndex] = ((Chunk)theChunk).getTileEntityUnsafe(subChunkX, posY, subChunkZ);

        airChecks.set(cacheIndex, block.isAir(helperCache, posX, posY, posZ));

        checkedBlocks.set(cacheIndex);
    }

    private void setupCache(int centerChunkPosX, int centerChunkPosZ) {
        val minChunkPosX = centerChunkPosX - 1;
        val minChunkPosZ = centerChunkPosZ - 1;

        val maxChunkPosX = centerChunkPosX + 1;
        val maxChunkPosZ = centerChunkPosZ + 1;

        for (var chunkPosZ = minChunkPosZ; chunkPosZ < maxChunkPosZ; chunkPosZ++) {
            for (var chunkPosX = minChunkPosX; chunkPosX < maxChunkPosX; chunkPosX++) {
                val rootChunkIndex = (chunkPosZ * CACHE_CHUNK_XZ_SIZE) + chunkPosX;

                val chunkProvider = worldRoot.lumi$chunkProvider();
                chunkExistsCheck:
                {
                    if (chunkProvider.chunkExists(chunkPosX, chunkPosZ))
                        break chunkExistsCheck;
                    val chunkBase = chunkProvider.provideChunk(chunkPosX, chunkPosZ);
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
        checkedBlocks.clear();
        cache.resetCache();
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
        return chunkBase | subChunkOffset;
    }

    private LumiChunkRoot chunkFromBlockPos(int posX, int posZ) {
        val chunkPosX = (posX >> BITSIZE_CHUNK_XZ) - minChunkPosX;
        val chunkPosZ = (posZ >> BITSIZE_CHUNK_XZ) - minChunkPosZ;

        if (chunkPosX < 0 || chunkPosX >= CACHE_CHUNK_XZ_SIZE || chunkPosZ < 0 || chunkPosZ >= CACHE_CHUNK_XZ_SIZE) {
            // TODO smarter shifting logic here
            setupCache(chunkPosX + minChunkPosZ, chunkPosZ + minChunkPosZ);
        }

        return rootChunks[chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX];
    }
}
