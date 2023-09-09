/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.POST_CHUNK_API_MIXIN_PRIORITY;

@Mixin(value = S21PacketChunkData.class, priority = POST_CHUNK_API_MIXIN_PRIORITY)
public abstract class S21PacketChunkDataMixin {
    @Inject(method = "func_149269_a",
            at = @At("HEAD"),
            require = 1)
    private static void processLightUpdatesOnReceive(Chunk chunkBase,
                                                     boolean hasSky,
                                                     int subChunkMask,
                                                     CallbackInfoReturnable<S21PacketChunkData.Extracted> cir) {
        LightingHooks.processLightUpdates(chunkBase);
    }
}
