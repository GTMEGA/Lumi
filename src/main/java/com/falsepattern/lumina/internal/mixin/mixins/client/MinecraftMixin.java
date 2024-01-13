/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Final
    @Shadow
    public Profiler mcProfiler;

    @Shadow
    public WorldClient theWorld;

    //TODO: Benchmark

//    @Inject(method = "runTick",
//            at = @At(value = "CONSTANT",
//                     args = "stringValue=levelRenderer",
//                     shift = At.Shift.BY,
//                     by = -3),
//            require = 1)
//    private void updateClientLighting(CallbackInfo ci) {
//        LightingHooks.processLightUpdates(theWorld);
//    }

    @Inject(method = "runGameLoop",
            at = @At(value = "CONSTANT",
                     args = "stringValue=tick",
                     shift = At.Shift.AFTER),
            require = 1)
    private void updateClientLighting(CallbackInfo ci) {
        LightingHooks.processLightUpdates(theWorld);
    }
}
