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
    public LumiChunk getLumiChunkFromBlockPos(int posX, int posZ) {
        val vanillaChunk = getChunkFromBlockCoords(posX, posZ);
        if (vanillaChunk instanceof LumiChunk)
            return (LumiChunk) vanillaChunk;
        return null;
    }

    @Override
    public LumiChunk getLumiChunkFromChunkPos(int chunkPosX, int chunkPosZ) {
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
    public int getBrightnessOrBlockLightValueMax(int posX, int posY, int posZ) {
        val block = getBlock(posX, posY, posZ);
        val blockBrightness = block.getLightValue();

        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;

            val lightValue = chunk.getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            return Math.max(blockBrightness, lightValue);
        }

        return blockBrightness;
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
    public int getSkyLightValue(int posX, int posY, int posZ) {
        val chunk = getLumiChunkFromBlockPos(posX, posZ);
        if (chunk != null) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;

            return chunk.getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        }

        return EnumSkyBlock.Sky.defaultLightValue;
    }

    @Override
    public int getOpacity(int posX, int posY, int posZ) {
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
