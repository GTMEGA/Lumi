/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin {
    @Shadow
    @SuppressWarnings("rawtypes")
    private Set chunksToUnload;
    @Shadow
    public WorldServer worldObj;

    @Inject(method = "saveChunks",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnSave(boolean saveAll, IProgressUpdate progressUpdate, CallbackInfoReturnable<Boolean> cir) {
        LightingHooks.processLightUpdates(worldObj);
    }

    @Inject(method = "unloadQueuedChunks",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnUnload(CallbackInfoReturnable<Boolean> cir) {
        if (worldObj.levelSaving)
            return;
        if (chunksToUnload.isEmpty())
            return;

        LightingHooks.processLightUpdates(worldObj);
    }
}
