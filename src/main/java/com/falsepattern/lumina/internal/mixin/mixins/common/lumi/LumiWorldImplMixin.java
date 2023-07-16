/*
 * Copyright (c) 2023 FalsePattern, Ven
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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.LumiAPI;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;

@Unique
@Mixin(World.class)
public abstract class LumiWorldImplMixin implements IBlockAccess, LumiWorld {
    @Mutable
    @Final
    @Shadow
    public Profiler theProfiler;

    @Shadow
    @SuppressWarnings("rawtypes")
    public Set activeChunkSet;

    @Shadow
    public abstract Chunk getChunkFromBlockCoords(int posX, int posZ);

    @Shadow
    public abstract Chunk getChunkFromChunkCoords(int chunkPosX, int chunkPosZ);

    private LumiWorldRoot lumi$root = null;
    private LumiLightingEngine lumi$lightingEngine = null;

    @Redirect(method = "<init>(" +
                       "Lnet/minecraft/world/storage/ISaveHandler;" +
                       "Ljava/lang/String;" +
                       "Lnet/minecraft/world/WorldSettings;" +
                       "Lnet/minecraft/world/WorldProvider;" +
                       "Lnet/minecraft/profiler/Profiler;" +
                       ")V",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/world/World;" +
                                "theProfiler:Lnet/minecraft/profiler/Profiler;"),
              require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    private void lumiWorldInit(World thiz, Profiler profiler) {
        this.theProfiler = profiler;

        this.lumi$root = (LumiWorldRoot) this;
        this.lumi$lightingEngine = LumiAPI.provideLightingEngine(this, profiler);
    }

    @Override
    public @NotNull LumiWorldRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public @NotNull String lumi$worldID() {
        return Tags.MOD_ID;
    }

    @Override
    @SuppressWarnings("CastToIncompatibleInterface")
    public @NotNull LumiChunk lumi$wrap(@NotNull Chunk baseChunk) {
        return (LumiChunk) baseChunk;
    }

    @Override
    @SuppressWarnings("CastToIncompatibleInterface")
    public @NotNull LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage baseSubChunk) {
        return (LumiSubChunk) baseSubChunk;
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ) {
        val baseChunk = getChunkFromBlockCoords(posX, posZ);
        if (baseChunk instanceof LumiChunk)
            return (LumiChunk) baseChunk;
        return null;
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ) {
        val baseChunk = getChunkFromChunkCoords(chunkPosX, chunkPosZ);
        if (baseChunk instanceof LumiChunk)
            return (LumiChunk) baseChunk;
        return null;
    }

    @Override
    public @NotNull LumiLightingEngine lumi$lightingEngine() {
        return lumi$lightingEngine;
    }


    @Override
    public int lumi$getBrightnessAndLightValueMax(@NotNull LightType lightType, int posX, int posY, int posZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightnessAndBlockLightValueMax(posX, posY, posZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(posX, posY, posZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightnessAndBlockLightValueMax(int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBrightnessAndBlockLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        val blockBrightness = lumi$getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, BLOCK_LIGHT_TYPE.defaultLightValue());
    }

    @Override
    public int lumi$getLightValueMax(int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        return LightType.maxBaseLightValue();
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
    public void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue) {
        val chunk = lumi$getChunkFromBlockPosIfExists(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
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
}
