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

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.ISaveHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumina.internal.world.LumiWorldManager.createLightingEngine;

@Unique
@Mixin(World.class)
public abstract class LumiWorldImplMixin implements IBlockAccess, LumiWorld {
    @Shadow
    public abstract Chunk getChunkFromBlockCoords(int p_72938_1_, int p_72938_2_);

    @Shadow
    public abstract Chunk getChunkFromChunkCoords(int p_72964_1_, int p_72964_2_);

    private LumiWorldRoot lumi$root = null;
    private LumiLightingEngine lumi$lightingEngine = null;

    @Inject(method = "<init>(" +
                     "Lnet/minecraft/world/storage/ISaveHandler;" +
                     "Ljava/lang/String;" +
                     "Lnet/minecraft/world/WorldSettings;" +
                     "Lnet/minecraft/world/WorldProvider;" +
                     "Lnet/minecraft/profiler/Profiler;" +
                     ")V",
            at = @At("TAIL"),
            require = 1)
    private void lumiWorldInit(ISaveHandler saveHandler,
                               String worldName,
                               WorldSettings worldSettings,
                               WorldProvider worldProvider,
                               Profiler profiler,
                               CallbackInfo ci) {
        this.lumi$root = (LumiWorldRoot) this;
        this.lumi$lightingEngine = createLightingEngine(this, profiler);
    }

    @Override
    public LumiWorldRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public String lumi$worldID() {
        return Tags.MODID;
    }

    @Override
    public LumiChunk lumi$wrap(Chunk baseChunk) {
        return (LumiChunk) baseChunk;
    }

    @Override
    public LumiSubChunk lumi$wrap(ExtendedBlockStorage baseSubChunk) {
        return (LumiSubChunk) baseSubChunk;
    }

    @Override
    public @Nullable LumiChunk lumi$getChunkFromBlockPos(int posX, int posZ) {
        val baseChunk = getChunkFromBlockCoords(posX, posZ);
        if (baseChunk instanceof LumiChunk)
            return (LumiChunk) baseChunk;
        return null;
    }

    @Override
    public @Nullable LumiChunk lumi$getChunkFromChunkPos(int chunkPosX, int chunkPosZ) {
        val baseChunk = getChunkFromChunkCoords(chunkPosX, chunkPosZ);
        if (baseChunk instanceof LumiChunk)
            return (LumiChunk) baseChunk;
        return null;
    }

    @Override
    public LumiLightingEngine lumi$lightingEngine() {
        return lumi$lightingEngine;
    }

    @Override
    public int lumi$getBrightnessAndLightValueMax(EnumSkyBlock lightType, int posX, int posY, int posZ) {
        switch (lightType) {
            case Block:
                return lumi$getBrightnessAndBlockLightValueMax(posX, posY, posZ);
            case Sky:
                return lumi$getSkyLightValue(posX, posY, posZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightnessAndBlockLightValueMax(int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBrightnessAndBlockLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        val blockBrightness = lumi$getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, EnumSkyBlock.Block.defaultLightValue);
    }

    @Override
    public int lumi$getLightValueMax(int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        return Math.max(EnumSkyBlock.Block.defaultLightValue, EnumSkyBlock.Sky.defaultLightValue);
    }

    @Override
    public void lumi$setLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ, int lightValue) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public int lumi$getLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
        }

        switch (lightType) {
            default:
            case Block:
                return EnumSkyBlock.Block.defaultLightValue;
            case Sky: {
                if (lumi$root().lumi$hasSky())
                    return EnumSkyBlock.Sky.defaultLightValue;
                return 0;
            }
        }
    }

    @Override
    public void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return EnumSkyBlock.Block.defaultLightValue;
    }

    @Override
    public void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue) {
        if (!lumi$root().lumi$hasSky())
            return;

        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
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

        val chunk = lumi$getChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        }

        return EnumSkyBlock.Sky.defaultLightValue;
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
    public int lumi$getBlockBrightness(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightOpacity(this, posX, posY, posZ);
    }
}
