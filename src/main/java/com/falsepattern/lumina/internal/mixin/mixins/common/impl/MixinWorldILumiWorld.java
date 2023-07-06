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

package com.falsepattern.lumina.internal.mixin.mixins.common.impl;

import com.falsepattern.lumina.api.LumiLightingEngine;
import com.falsepattern.lumina.api.LumiChunk;
import com.falsepattern.lumina.api.LumiEBS;
import com.falsepattern.lumina.api.LumiWorld;
import com.falsepattern.lumina.api.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(World.class)
public abstract class MixinWorldILumiWorld implements LumiWorld, IBlockAccess, LumiWorldRoot {
    @Shadow
    @Final
    public Profiler theProfiler;
    @Shadow
    public boolean isRemote;
    @Shadow
    @Final
    public WorldProvider provider;
    @Shadow
    protected IChunkProvider chunkProvider;

    @Shadow
    public abstract void func_147479_m(int p_147479_1_, int p_147479_2_, int p_147479_3_);

    @Shadow
    public abstract boolean checkChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    @Shadow
    public abstract boolean doChunksNearChunkExist(int x, int y, int z, int dist);

    @Getter
    @Setter
    private LumiLightingEngine lightingEngine;

    @Override
    public LumiChunk lumiWrap(Chunk chunk) {
        return (LumiChunk) chunk;
    }

    @Override
    public LumiEBS lumiWrap(ExtendedBlockStorage ebs) {
        return (LumiEBS) ebs;
    }

    @Override
    public int lumiGetLightValue(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int lumiGetLightOpacity(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightOpacity(this, posX, posY, posZ);
    }

    @Override
    public LumiWorldRoot root() {
        return this;
    }

    @Override
    public Profiler rootTheProfiler() {
        return theProfiler;
    }

    @Override
    public boolean rootIsRemote() {
        return isRemote;
    }

    @Override
    public boolean rootHasNoSky() {
        return provider.hasNoSky;
    }

    @Override
    public void rootMarkBlockForRenderUpdate(int posX, int posY, int posZ) {
        func_147479_m(posX, posY, posZ);
    }

    @Override
    public IChunkProvider rootProvider() {
        return chunkProvider;
    }

    @Override
    public boolean rootCheckChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean rootDoChunksNearChunkExist(int x, int y, int z, int dist) {
        return doChunksNearChunkExist(x, y, z, dist);
    }

    @Override
    public String lumiId() {
        return Tags.MODID;
    }
}
