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

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.storage.LumiBlockStorage;
import com.falsepattern.lumina.api.storage.LumiBlockStorageRoot;
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
public abstract class LumiBlockCacheImplMixin implements IBlockAccess, LumiBlockStorage, LumiBlockStorageRoot {
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

    private LumiWorld lumi$world = null;

    @Inject(method = LUMI_CHUNK_CACHE_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    @Dynamic(LUMI_CHUNK_CACHE_INIT_HOOK_INFO)
    private void lumiBlockChunkCacheInit(CallbackInfo ci) {
        this.lumi$world = (LumiWorld) worldObj;
    }

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
    public @NotNull LumiBlockStorageRoot lumi$root() {
        return this;
    }

    @Override
    public int lumi$getBrightness(@Nullable LumiChunk chunk, @NotNull LightType lightType, int posX, int posY, int posZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightness(chunk, posX, posY, posZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(chunk, posX, posY, posZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightness(@Nullable LumiChunk chunk, int posX, int posY, int posZ) {
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBrightness(subChunkPosX, posY, subChunkPosZ);
        }
        val blockBrightness = lumi$getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, BLOCK_LIGHT_TYPE.defaultLightValue());
    }

    @Override
    public int lumi$getLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ) {
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return LightType.maxBaseLightValue();
    }

    @Override
    public int lumi$getLightValue(@Nullable LumiChunk chunk, @NotNull LightType lightType, int posX, int posY, int posZ) {
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
    public int lumi$getBlockLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ) {
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return BLOCK_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getSkyLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ) {
        if (!lumi$hasSky())
            return 0;

        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        }

        return SKY_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
        val block = lumi$getBlock(posX, posY, posZ);
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
        val block = lumi$getBlock(posX, posY, posZ);
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


    @Override
    public @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ) {
        val chunk = lumi$getChunkRootFromBlockPosIfExists(posX, posZ);
        if (chunk instanceof LumiChunk)
            return (LumiChunk) chunk;
        return null;
    }

    @Override
    public @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ) {
        val chunk = lumi$getChunkRootFromChunkPosIfExists(chunkPosX, chunkPosZ);
        if (chunk instanceof LumiChunk)
            return (LumiChunk) chunk;
        return null;
    }
}
