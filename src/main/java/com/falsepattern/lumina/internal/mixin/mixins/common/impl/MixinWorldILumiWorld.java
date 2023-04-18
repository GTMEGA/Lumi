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

import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.internal.world.lighting.LightingEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.util.Set;

@Mixin(World.class)
public abstract class MixinWorldILumiWorld implements ILumiWorld, IBlockAccess {
    @Shadow protected Set activeChunkSet;

    @Shadow public abstract IChunkProvider getChunkProvider();

    private LightingEngine lightingEngine;

    /**
     * @author Angeline
     * Initialize the lighting engine on world construction.
     */
    @Redirect(method = "<init>(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/ISaveHandler;loadWorldInfo()Lnet/minecraft/world/storage/WorldInfo;"))
    private WorldInfo onConstructed(ISaveHandler handler) {
        this.lightingEngine = new LightingEngine(this);
        return handler.loadWorldInfo();
    }


    /**
     * Directs the light update to the lighting engine and always returns a success value.
     * @author Angeline
     */
    @Inject(method = "updateLightByType", at = @At("HEAD"), cancellable = true)
    private void checkLightFor(EnumSkyBlock type, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        this.lightingEngine.scheduleLightUpdate(type, x, y, z);

        cir.setReturnValue(true);
    }

    @Override
    public LightingEngine getLightingEngine() {
        return this.lightingEngine;
    }

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
}
