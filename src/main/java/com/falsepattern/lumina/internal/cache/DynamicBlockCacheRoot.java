package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;


/**
 * TODO: Stores Blocks/Block Meta/Tile Entities/Air Checks
 */
public class DynamicBlockCacheRoot implements LumiBlockCacheRoot {
    /**
     * TODO: Center of the current focus, we work on a 3x3 set of chunks
     */
    int centerChunkPosX;
    int centerChunkPosY;

    /**
     * TODO: On construction, this is set to false, as the center pos is not defined
     */
    boolean isReady;

    LumiWorldRoot worldRoot;

    // Z/X 3/3
    LumiChunkRoot[] rootChunks = new LumiChunkRoot[9];

    // Z/X/Y 48/48/256
    Block[] blocks;
    // Z/X/Y 48/48/256
    int[] blockMetas;
    // Z/X/Y 48/48/256
    TileEntity[] tileEntities; //TODO: Check if a block -has- one before hand
    // Z/X/Y 48/48/256
    BitSet airChecks = new BitSet(48 * 48 * 256);

    // Z/X/Y 48/48/256
    BitSet checkedBlocks = new BitSet(48 * 48 * 256);

    boolean lumi$hasSky;

    boolean isClientSide;

    /**
     * TODO: Unsure about the binding relationship atm, should be decided based on implementation
     */
    DynamicBlockCache cache;

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_dynamic_block_cache_root";
    }

    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_dynamic_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return isClientSide;
    }

    @Override
    public boolean lumi$hasSky() {
        return lumi$hasSky;
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        return null;
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return 0;
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return false;
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return null;
    }

    /**
     * TODO: Ensures the selected block is ready to be read
     * TODO: could also be merged with a 'get index' method, to automatically reset & shift the focus
     */
    private void prepareBlock(int posX, int posY, int posZ) {
        if (checkedBlocks.get(0))
            return;
    }

    /**
     * TODO: Resets the entire cache, called any time the requested info is out of range, or `isReady` is false
     */
    void resetCache(int centerChunkPosX, int centerChunkPosZ) {
        val minChunkPosX = centerChunkPosX - 1;
        val minChunkPosZ = centerChunkPosZ - 1;

        val maxChunkPosX = centerChunkPosX + 1;
        val maxChunkPosZ = centerChunkPosZ + 1;

        for (var chunkPosZ = minChunkPosZ; chunkPosZ < maxChunkPosZ; chunkPosZ++) {
            for (var chunkPosX = minChunkPosX; chunkPosX < maxChunkPosX; chunkPosX++) {
                val rootChunkIndex = chunkPosX + (chunkPosZ * 3);

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
    }

    /**
     * This is probably broken af
     */
    int cacheIndexFromBlockPos(int posX, int posY, int posZ) {
        val chunkPosX = (posX >> 4) - centerChunkPosX;
        val chunkPosZ = (posZ >> 4) - centerChunkPosY;

        val subChunkPosX = (chunkPosX * 3) + (posX & 15);
        val subChunkPosY = posY & 255;
        val subChunkPosZ = (chunkPosZ * 3) + (posZ & 15);

        return (subChunkPosZ * 16 * 16) + (subChunkPosX * 16) + subChunkPosY;
    }


    /**
     * TODO: Zero's out the bitset of the valid blocks, to be called once at the start or end of server tick
     */
    void resetCache() {
    }
}
