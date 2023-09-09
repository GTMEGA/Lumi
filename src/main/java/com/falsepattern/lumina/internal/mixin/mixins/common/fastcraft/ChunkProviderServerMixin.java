/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.fastcraft;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin {
    @Redirect(method = "populate",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;a(Lnet/minecraft/world/chunk/Chunk;)V",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private void undoFastCraftHooks1(Chunk chunk) {
        chunk.func_150809_p();
    }
}
