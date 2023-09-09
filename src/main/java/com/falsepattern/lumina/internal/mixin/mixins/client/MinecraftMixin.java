/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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

    @Inject(method = "runTick",
            at = @At(value = "CONSTANT",
                     args = "stringValue=levelRenderer",
                     shift = At.Shift.BY,
                     by = -3),
            require = 1)
    private void updateClientLighting(CallbackInfo ci) {
        mcProfiler.endStartSection("lighting");
        LightingHooks.processLightUpdates(theWorld);
    }
}
