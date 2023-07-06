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

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
@Accessors(fluent = true, chain = false)
public abstract class MixinWorldILumiWorld implements IBlockAccess, LumiWorld, LumiWorldRoot {
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

    @Setter
    @Getter
    private LumiLightingEngine lightingEngine;

    @Override
    public World world() {
        return (World) (Object) this;
    }

    @Override
    public LumiChunk toLumiChunk(Chunk chunk) {
        return (LumiChunk) chunk;
    }

    @Override
    public LumiSubChunk toLumiSubChunk(ExtendedBlockStorage subChunk) {
        return (LumiSubChunk) subChunk;
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
    public LumiWorldRoot worldRoot() {
        return this;
    }

    @Override
    public Profiler profiler() {
        return theProfiler;
    }

    @Override
    public boolean isClientSide() {
        return isRemote;
    }

    @Override
    public boolean hasSkyLight() {
        return !provider.hasNoSky;
    }

    @Override
    public void markBlockForRenderUpdate(int posX, int posY, int posZ) {
        func_147479_m(posX, posY, posZ);
    }

    @Override
    public IChunkProvider chunkProvider() {
        return chunkProvider;
    }

    @Override
    public boolean doesChunkCuboidExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ) {
        return checkChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
    }

    @Override
    public boolean doesChunkCubeExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange) {
        return doChunksNearChunkExist(centerPosX, centerPosY, centerPosZ, blockRange);
    }

    @Override
    public String luminaWorldID() {
        return Tags.MODID;
    }
}
