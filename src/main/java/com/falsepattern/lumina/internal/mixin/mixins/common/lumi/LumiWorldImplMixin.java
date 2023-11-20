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

import com.falsepattern.lumina.api.LumiAPI;
import com.falsepattern.lumina.api.cache.LumiBlockCache;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.world.DefaultWorldProvider;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumina.api.init.LumiWorldInitHook.LUMI_WORLD_INIT_HOOK_INFO;
import static com.falsepattern.lumina.api.init.LumiWorldInitHook.LUMI_WORLD_INIT_HOOK_METHOD;
import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;

@Unique
@Mixin(World.class)
public abstract class LumiWorldImplMixin implements IBlockAccess, LumiWorld {
    // region Shadow
    @Mutable
    @Final
    @Shadow
    public Profiler theProfiler;
    // endregion

    private LumiWorldRoot lumi$root = null;
    private LumiLightingEngine lumi$lightingEngine = null;
    private LumiBlockCache lumi$blockCache = null;

    @Inject(method = LUMI_WORLD_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    @Dynamic(LUMI_WORLD_INIT_HOOK_INFO)
    private void lumiWorldInit(CallbackInfo ci) {
        this.lumi$root = (LumiWorldRoot) this;

        if (DefaultWorldProvider.isRegistered()) {
            val blockCacheRoot = lumi$root.lumi$blockCacheRoot();
            this.lumi$blockCache = blockCacheRoot.lumi$createBlockCache(this);
            this.lumi$lightingEngine = LumiAPI.provideLightingEngine(this, theProfiler);
        }
    }

    // region World
    @Override
    public @NotNull LumiWorldRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public @NotNull String lumi$worldID() {
        return "lumi_world";
    }

    @Override
    @SuppressWarnings("CastToIncompatibleInterface")
    public @NotNull LumiChunk lumi$wrap(@NotNull Chunk chunkBase) {
        return (LumiChunk) chunkBase;
    }

    @Override
    @SuppressWarnings("CastToIncompatibleInterface")
    public @NotNull LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage subChunkBase) {
        return (LumiSubChunk) subChunkBase;
    }

    @Override
    public @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ) {
        val chunkRoot = lumi$root.lumi$getChunkRootFromBlockPosIfExists(posX, posZ);
        if (chunkRoot instanceof LumiChunk)
            return (LumiChunk) chunkRoot;
        return null;
    }

    @Override
    public @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ) {
        val chunkRoot = lumi$root.lumi$getChunkRootFromChunkPosIfExists(chunkPosX, chunkPosZ);
        if (chunkRoot instanceof LumiChunk)
            return (LumiChunk) chunkRoot;
        return null;
    }

    @Override
    public @NotNull LumiLightingEngine lumi$lightingEngine() {
        return lumi$lightingEngine;
    }

    @Override
    public void lumi$setLightValue(@NotNull LightType lightType, int posX, int posY, int posZ, int lightValue) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue) {
        if (!lumi$root().lumi$hasSky())
            return;

        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public @NotNull LumiBlockCache lumi$blockCache() {
        return lumi$blockCache;
    }
    // endregion

    // region Block Storage
    @Override
    public @NotNull String lumi$blockStorageID() {
        return "lumi_world";
    }

    @Override
    public @NotNull LumiWorld lumi$world() {
        return this;
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
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
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
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return LightType.maxBaseLightValue();
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
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
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return BLOCK_LIGHT_TYPE.defaultLightValue();
    }

    @Override
    public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
        if (!lumi$root().lumi$hasSky())
            return 0;

        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
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
}
