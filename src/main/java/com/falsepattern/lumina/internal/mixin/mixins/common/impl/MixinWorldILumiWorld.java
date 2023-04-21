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

import com.falsepattern.lumina.api.ILightingEngine;
import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(World.class)
public abstract class MixinWorldILumiWorld implements ILumiWorld, IBlockAccess, ILumiWorldRoot {
    @Getter
    @Setter
    private ILightingEngine lightingEngine;

    @Override
    public ILumiChunk wrap(Chunk chunk) {
        return (ILumiChunk) chunk;
    }

    @Override
    public ILumiEBS wrap(ExtendedBlockStorage ebs) {
        return (ILumiEBS) ebs;
    }

    @Override
    public int getLightValueForState(Block state, int x, int y, int z) {
        return state.getLightValue(this, x, y, z);
    }

    @Override
    public int getLightOpacity(Block state, int x, int y, int z) {
        return state.getLightOpacity(this, x, y, z);
    }

    @Shadow
    @Override
    public abstract boolean updateLightByType(EnumSkyBlock lightType, int x, int y, int z);

    @Shadow @Final public Profiler theProfiler;

    @Shadow public boolean isRemote;

    @Shadow @Final public WorldProvider provider;

    @Shadow public abstract void func_147479_m(int p_147479_1_, int p_147479_2_, int p_147479_3_);

    @Shadow protected IChunkProvider chunkProvider;

    @Override
    public ILumiWorldRoot root() {
        return this;
    }

    @Override
    public Profiler theProfiler() {
        return theProfiler;
    }

    @Override
    public boolean isRemote() {
        return isRemote;
    }

    @Override
    public boolean hasNoSky() {
        return provider.hasNoSky;
    }

    @Override
    public void markBlockForRenderUpdate(int x, int y, int z) {
        func_147479_m(x, y, z);
    }

    @Override
    public IChunkProvider provider() {
        return chunkProvider;
    }

    @Shadow
    @Override
    public abstract boolean checkChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    @Shadow
    @Override
    public abstract boolean doChunksNearChunkExist(int x, int y, int z, int dist);

    @Override
    public String id() {
        return Tags.MODID;
    }
}
