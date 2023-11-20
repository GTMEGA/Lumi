/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.cache.LumiBlockCache;
import com.falsepattern.lumina.api.cache.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumina.api.init.LumiChunkCacheInitHook.LUMI_CHUNK_CACHE_INIT_HOOK_INFO;
import static com.falsepattern.lumina.api.init.LumiChunkCacheInitHook.LUMI_CHUNK_CACHE_INIT_HOOK_METHOD;
import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;

@Unique
@Mixin(ChunkCache.class)
public abstract class LumiBlockCacheImplMixin implements IBlockAccess, LumiBlockCache {
    // region Shadow
    @Shadow
    private int chunkX;
    @Shadow
    private int chunkZ;
    @Shadow
    private Chunk[][] chunkArray;
    @Shadow
    private boolean isEmpty;
    @Shadow
    private World worldObj;
    // endregion

    private LumiBlockCacheRoot lumi$root = null;
    private LumiWorld lumi$world = null;

    @Inject(method = LUMI_CHUNK_CACHE_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    @Dynamic(LUMI_CHUNK_CACHE_INIT_HOOK_INFO)
    private void lumiBlockChunkCacheInit(CallbackInfo ci) {
        this.lumi$root = (LumiBlockCacheRoot) this;
        this.lumi$world = (LumiWorld) worldObj;
    }

    // region Block Cache
    @Override
    public @NotNull LumiBlockCacheRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public @NotNull String lumi$BlockCacheID() {
        return "lumi_block_cache";
    }
    // endregion

    // region Block Storage
    @Override
    public @NotNull String lumi$blockStorageID() {
        return "lumi_block_cache";
    }

    @Override
    public @NotNull LumiWorld lumi$world() {
        return lumi$world;
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
        val chunk = getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBrightness(subChunkPosX, posY, subChunkPosZ);
        }
        val blockBrightness = lumi$getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, BLOCK_LIGHT_TYPE.defaultLightValue());
    }

    @Override
    public int lumi$getLightValue(int posX, int posY, int posZ) {
        val chunk = getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return LightType.maxBaseLightValue();
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        val chunk = getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
        }

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
        val chunk = getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return BLOCK_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
        if (!lumi$root.lumi$hasSky())
            return 0;

        val chunk = getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        }

        return SKY_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
        val block = lumi$root.lumi$getBlock(posX, posY, posZ);
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
        val block = lumi$root.lumi$getBlock(posX, posY, posZ);
        return block.getLightOpacity(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightOpacity(this, posX, posY, posZ);
    }
    // endregion

    @SuppressWarnings("InstanceofIncompatibleInterface")
    private @Nullable LumiChunk getChunkFromBlockPosIfExists(int posX, int posZ) {
        checks:
        {
            if (isEmpty)
                break checks;

            val chunkPosX = posX >> 4;
            val chunkPosZ = posZ >> 4;
            val xChunkIndex = chunkPosX - chunkX;
            val zChunkIndex = chunkPosZ - chunkZ;

            if (xChunkIndex < 0 || xChunkIndex >= chunkArray.length)
                break checks;

            val zChunkArray = chunkArray[xChunkIndex];
            if (zChunkArray == null)
                break checks;
            if (zChunkIndex < 0 || zChunkIndex >= zChunkArray.length)
                break checks;

            val chunkBase = zChunkArray[zChunkIndex];
            if (chunkBase instanceof LumiChunk)
                return (LumiChunk) chunkBase;
        }
        return null;
    }
}
