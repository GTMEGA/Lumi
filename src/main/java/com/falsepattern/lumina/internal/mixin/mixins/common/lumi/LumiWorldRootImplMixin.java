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

import com.falsepattern.lumina.api.world.LumiWorldRoot;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Unique
@Mixin(World.class)
public abstract class LumiWorldRootImplMixin implements IBlockAccess, LumiWorldRoot {
    @Final
    @Shadow
    public WorldProvider provider;
    @Final
    @Shadow
    public Profiler theProfiler;

    @Shadow
    protected IChunkProvider chunkProvider;
    @Shadow
    public boolean isRemote;

    @Shadow
    public abstract Block getBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean doChunksNearChunkExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange);

    @Shadow
    public abstract boolean checkChunksExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    @Shadow
    public abstract int getBlockMetadata(int posX, int posY, int posZ);

    @Shadow
    public abstract void func_147479_m(int posX, int posY, int posZ);

    @Override
    public World lumi$base() {
        return (World) (Object) this;
    }

    @Override
    public Profiler lumi$profiler() {
        return theProfiler;
    }

    @Override
    public boolean lumi$isClientSide() {
        return isRemote;
    }

    @Override
    public boolean lumi$hasSky() {
        return !provider.hasNoSky;
    }

    @Override
    public Block lumi$getBlock(int posX, int posY, int posZ) {
        return getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return getBlockMetadata(posX, posY, posZ);
    }

    @Override
    public IChunkProvider lumi$chunkProvider() {
        return chunkProvider;
    }

    @Override
    public boolean lumi$doChunksExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ) {
        return checkChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
    }

    @Override
    public boolean lumi$doChunksExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange) {
        return doChunksNearChunkExist(centerPosX, centerPosY, centerPosZ, blockRange);
    }

    @Override
    public void lumi$markBlockForRenderUpdate(int posX, int posY, int posZ) {
        func_147479_m(posX, posY, posZ);
    }
}
