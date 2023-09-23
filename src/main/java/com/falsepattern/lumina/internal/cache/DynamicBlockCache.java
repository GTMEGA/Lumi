package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.storage.LumiBlockCache;
import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;

/**
 * TODO: Stores Light Values/Opacity values
 */
public class DynamicBlockCache implements LumiBlockCache {
    /**
     * Center of the current focus, we work on a 3x3 set of chunks
     */
    int centerChunkPosX;
    int centerChunkPosY;

    /**
     * On construction, this is set to false, as the center pos is not defined
     */
    boolean isReady;

    /**
     * TODO: Unsure about the binding relationship atm, should be decided based on implementation
     */
    DynamicBlockCacheRoot root;
    LumiWorld world;

    // Z/X/Y 48/48/256
    int[] lightValues;
    // Z/X/Y 48/48/256
    int[] opacityValues;

    // Z/X/Y 48/48/256
    BitSet checkedBlocks = new BitSet(48 * 48 * 256); //TODO: Check if a block -has- one before hand

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
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightness(posX, posY, posZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(posX, posY, posZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightness(int posX, int posY, int posZ) {
//        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
//        if (chunk != null) {
//            val subChunkPosX = posX & 15;
//            val subChunkPosZ = posZ & 15;
//            return chunk.lumi$getBrightness(subChunkPosX, posY, subChunkPosZ);
//        }
        val blockBrightness = lumi$getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, BLOCK_LIGHT_TYPE.defaultLightValue());
    }

    @Override
    public int lumi$getLightValue(int posX, int posY, int posZ) {
//        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
//        if (chunk != null) {
//            val subChunkPosX = posX & 15;
//            val subChunkPosZ = posZ & 15;
//            return chunk.lumi$getLightValue(subChunkPosX, posY, subChunkPosZ);
//        }
        return LightType.maxBaseLightValue();
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
//        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
//        if (chunk != null) {
//            val subChunkPosX = posX & 15;
//            val subChunkPosZ = posZ & 15;
//            return chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
//        }

        switch (lightType) {
            default:
            case BLOCK_LIGHT_TYPE:
                return BLOCK_LIGHT_TYPE.defaultLightValue();
            case SKY_LIGHT_TYPE: {
                if (lumi$root().lumi$hasSky())
                    return SKY_LIGHT_TYPE.defaultLightValue();
                return 0;
            }
        }
    }

    @Override
    public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
//        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
//        if (chunk != null) {
//            val subChunkPosX = posX & 15;
//            val subChunkPosZ = posZ & 15;
//            return chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
//        }
        return BLOCK_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
//        if (!lumi$root().lumi$hasSky())
//            return 0;
//
//        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
//        if (chunk != null) {
//            val subChunkPosX = posX & 15;
//            val subChunkPosZ = posZ & 15;
//            return chunk.lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
//        }

        return SKY_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
//        val block = lumi$root.lumi$getBlock(posX, posY, posZ);
//        return block.getLightValue(this, posX, posY, posZ);
        return 0;
    }

    @Override
    public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
//        val block = lumi$root.lumi$getBlock(posX, posY, posZ);
//        return block.getLightOpacity(this, posX, posY, posZ);
        return 0;
    }

    @Override
    public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return 0;
        //        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return 0;
//        return block.getLightOpacity(this, posX, posY, posZ);
    }

    /**
     * TODO: Ensures the selected block is ready to be read
     * TODO: could also be merged with a 'get index' method, to automatically reset & shift the focus
     */
    private void prepareBlock(int posX, int posY, int posZ) {
        if (checkedBlocks.get(0))
            return;
    }

    void resetCacheForBlockPos(int posX, int posY, int posZ) {

    }

    /**
     * TODO: Resets this cache, and focuses on the new area
     */
    void resetCache(int centerChunkPosX, int centerChunkPosY) {

    }
}
