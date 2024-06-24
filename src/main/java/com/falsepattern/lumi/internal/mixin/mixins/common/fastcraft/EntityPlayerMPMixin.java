/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.fastcraft;

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
