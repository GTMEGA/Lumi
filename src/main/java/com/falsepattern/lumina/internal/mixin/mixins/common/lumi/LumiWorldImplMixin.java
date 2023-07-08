/*
 * Copyright (C) 2023 FalsePattern
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
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class LumiWorldImplMixin implements IBlockAccess, LumiWorld {
    @Shadow
    public abstract Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_);

    @Shadow
    public abstract Chunk getChunkFromBlockCoords(int p_72938_1_, int p_72938_2_);

    @Shadow
    public abstract Chunk getChunkFromChunkCoords(int p_72964_1_, int p_72964_2_);

    @Shadow
    public abstract int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_);

    private LumiLightingEngine lightingEngine;

    @Override
    public String luminaWorldID() {
        return Tags.MODID;
    }

    @Override
    public LumiWorldRoot rootWorld() {
        return (LumiWorldRoot) this;
    }

    @Override
    public LumiChunk toLumiChunk(Chunk vanillaChunk) {
        return (LumiChunk) vanillaChunk;
    }

    @Override
    public LumiSubChunk toLumiSubChunk(ExtendedBlockStorage vanillaSubChunk) {
        return (LumiSubChunk) vanillaSubChunk;
    }

    @Override
    public @Nullable LumiChunk getLumiChunkFromBlockPos(int posX, int posZ) {
        val vanillaChunk = getChunkFromBlockCoords(posX, posZ);
        if (vanillaChunk instanceof LumiChunk)
            return (LumiChunk) vanillaChunk;
        return null;
    }

    @Override
    public @Nullable LumiChunk getLumiChunkFromChunkPos(int chunkPosX, int chunkPosZ) {
        val vanillaChunk = getChunkFromChunkCoords(chunkPosX, chunkPosZ);
        if (vanillaChunk instanceof LumiChunk)
            return (LumiChunk) vanillaChunk;
        return null;
    }

    @Override
    public void lightingEngine(LumiLightingEngine lightingEngine) {
        this.lightingEngine = lightingEngine;
    }

    @Override
    public LumiLightingEngine lightingEngine() {
        return lightingEngine;
    }

    @Override
    public int getBrightnessAndBlockLightValueMax(int posX, int posY, int posZ) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.getBrightnessAndBlockLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        val blockBrightness = getBlockBrightness(posX, posY, posZ);
        return Math.max(blockBrightness, EnumSkyBlock.Block.defaultLightValue);
    }

    @Override
    public int getBlockSkyAndLightValueMax(int posX, int posY, int posZ) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.getBlockSkyAndLightValueMax(subChunkPosX, posY, subChunkPosZ);
        }
        return Math.max(EnumSkyBlock.Block.defaultLightValue, EnumSkyBlock.Sky.defaultLightValue);
    }

    @Override
    public void lumi$setLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ, int lightValue) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.lumi$setLightValue(lightType, subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public int getLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
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
                if (rootWorld().hasSky())
                    return EnumSkyBlock.Sky.defaultLightValue;
                return 0;
            }
        }
    }

    @Override
    public void setBlockLightValue(int posX, int posY, int posZ, int lightValue) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public int getBlockLightValue(int posX, int posY, int posZ) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        }
        return EnumSkyBlock.Block.defaultLightValue;
    }

    @Override
    public void setSkyLightValue(int posX, int posY, int posZ, int lightValue) {
        if (!rootWorld().hasSky())
            return;

        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            chunk.setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
        }
    }

    @Override
    public int getSkyLightValue(int posX, int posY, int posZ) {
        if (!rootWorld().hasSky())
            return 0;

        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return chunk.getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        }

        return EnumSkyBlock.Sky.defaultLightValue;
    }

    @Override
    public int getBlockBrightness(int posX, int posY, int posZ) {
        val block = getBlock(posX, posY, posZ);
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int getBlockOpacity(int posX, int posY, int posZ) {
        val block = getBlock(posX, posY, posZ);
        return block.getLightOpacity(this, posX, posY, posZ);
    }

    @Override
    public int getBlockBrightness(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int getBlockOpacity(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightOpacity(this, posX, posY, posZ);
    }
}
