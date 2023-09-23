package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.storage.LumiBlockCache;
import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

import static com.falsepattern.lumina.internal.cache.DynamicBlockCacheRoot.CHUNK_XZ_BITMASK;
import static com.falsepattern.lumina.internal.cache.DynamicBlockCacheRoot.ELEMENT_COUNT_PER_CACHED_THING;

/**
 * TODO: Stores Light Values/Opacity values
 */
public class DynamicBlockCache implements LumiBlockCache {
    /**
     * TODO: Unsure about the binding relationship atm, should be decided based on implementation
     */
    final DynamicBlockCacheRoot root;
    LumiWorld world;

    // CZ/CX/Z/X/Y 3/3/16/16/256
    int[] blockBrightnessValues = new int[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    int[] blockOpacityValues = new int[ELEMENT_COUNT_PER_CACHED_THING];

    // CZ/CX/Z/X/Y 3/3/16/16/256
    BitSet checkedBlocks = new BitSet(ELEMENT_COUNT_PER_CACHED_THING); //TODO: Check if a block -has- one before hand

    public DynamicBlockCache(DynamicBlockCacheRoot root) {
        this.root = root;
        world = null;
    }

    @Override
    public @NotNull LumiBlockCacheRoot lumi$root() {
        return root;
    }

    @Override
    public @NotNull String lumi$BlockCacheID() {
        return "lumi_dynamic_block_cache";
    }

    @Override
    public @NotNull String lumi$blockStorageID() {
        return "lumi_dynamic_block_cache";
    }

    @Override
    public @NotNull LumiWorld lumi$world() {
        return world;
    }

    @Override
    public int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return world.lumi$getBrightness(lightType, posX, posY, posZ);
    }

    @Override
    public int lumi$getBrightness(int posX, int posY, int posZ) {
        return world.lumi$getBrightness(posX, posY, posZ);
    }

    @Override
    public int lumi$getLightValue(int posX, int posY, int posZ) {
        return world.lumi$getLightValue(posX, posY, posZ);
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return world.lumi$getLightValue(lightType, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
        return world.lumi$getBlockLightValue(posX, posY, posZ);
    }

    @Override
    public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
        return world.lumi$getSkyLightValue(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
        val index = root.cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return blockBrightnessValues[index];
    }

    @Override
    public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
        val index = root.cacheIndexFromBlockPos(posX, posY, posZ);
        prepareBlock(index, posX, posY, posZ);
        return blockOpacityValues[index];
    }

    @Override
    public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return lumi$getBlockBrightness(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return lumi$getBlockOpacity(posX, posY, posZ);
    }

    /**
     * TODO: could also be merged with a 'get index' method, to automatically reset & shift the focus
     */
    private void prepareBlock(int cacheIndex, int posX, int posY, int posZ) {
        if (checkedBlocks.get(cacheIndex))
            return;

        val subChunkX = posX & CHUNK_XZ_BITMASK;
        val subChunkZ = posZ & CHUNK_XZ_BITMASK;

        val theBlock = root.lumi$getBlock(subChunkX, posY, subChunkZ);
        val theMeta = root.lumi$getBlockMeta(subChunkX, posY, subChunkZ);

        blockBrightnessValues[cacheIndex] = world.lumi$getBlockBrightness(theBlock, theMeta, posX, posY, posZ);
        blockOpacityValues[cacheIndex] = world.lumi$getBlockOpacity(theBlock, theMeta, posX, posY, posZ);

    }

    /**
     * TODO: Resets this cache, and focuses on the new area
     */
    void resetCache() {
        checkedBlocks.clear();
    }
}
