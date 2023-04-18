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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILightingEngineProvider;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
//TODO
@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    /**
     * Injects into the head of saveChunk() to forcefully process all pending light updates. Fail-safe.
     *
     * @author Angeline
     */
    @Inject(method = "saveChunk", at = @At("HEAD"))
    private void onConstructed(World world, Chunk chunkIn, CallbackInfo callbackInfo) {
        ((ILightingEngineProvider) world).getLightingEngine().processLightUpdates();
    }

    /**
     * Injects the deserialization logic for chunk data on load so we can extract whether or not we've populated light yet.
     *
     * @author Angeline
     */
    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private void onReadChunkFromNBT(World world, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();

        LightingHooks.readNeighborLightChecksFromNBT((ILumiChunk)chunk, compound);

        ((ILumiChunk) chunk).setLightInitialized(compound.getBoolean("LightPopulated"));

    }

    /**
     * Injects the serialization logic for chunk data on save so we can store whether or not we've populated light yet.
     * @author Angeline
     */
    @Inject(method = "writeChunkToNBT", at = @At("RETURN"))
    private void onWriteChunkToNBT(Chunk chunk, World world, NBTTagCompound compound, CallbackInfo ci) {
        LightingHooks.writeNeighborLightChecksToNBT((ILumiChunk)chunk, compound);

        compound.setBoolean("LightPopulated", ((ILumiChunk) chunk).isLightInitialized());
    }
}
