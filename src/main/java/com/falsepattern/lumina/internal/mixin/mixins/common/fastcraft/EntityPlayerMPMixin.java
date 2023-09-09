/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.fastcraft;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin {
    @Redirect(method = "onUpdate",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;w(Lnet/minecraft/world/chunk/Chunk;)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks1(Chunk chunk) {
        return chunk.func_150802_k();
    }

    @Redirect(method = "localOnUpdate",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;w(Lnet/minecraft/world/chunk/Chunk;)Z",
                       remap = false),
              remap = false,
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks2(Chunk chunk) {
        return chunk.func_150802_k();
    }
}
