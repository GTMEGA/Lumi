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

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.internal.world.lighting.LightingEngine;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
//TODO
@Mixin(World.class)
public abstract class MixinWorld {
    @SuppressWarnings({"FieldCanBeLocal", "unused"}) //Used by MixinWorldILumiWorld in common
    private LightingEngine lightingEngine;

    @Inject(method = "finishSetup", at = @At("RETURN"), remap = false)
    private void onConstructed(CallbackInfo ci) {
        this.lightingEngine = new LightingEngine((ILumiWorld) this);
    }

    @Redirect(method = { "getSkyBlockTypeBrightness", "getSavedLightValue" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"))
    private int useBlockIntrinsicBrightness(Chunk instance, EnumSkyBlock type, int x, int y, int z) {
        if(type == EnumSkyBlock.Block)
            return LightingHooks.getIntrinsicOrSavedBlockLightValue((ILumiChunk) instance, x, y, z);
        else
            return instance.getSavedLightValue(type, x, y, z);
    }
}
