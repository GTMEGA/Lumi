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

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Shadow protected abstract void recheckGaps(boolean p_150803_1_);

    @Shadow public abstract void func_150809_p();

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;c(Lnet/minecraft/world/chunk/Chunk;III)V",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private void undoFastCraftHooks1(Chunk chunk, int x, int y, int z) {

    }

    @Redirect(method = "func_150804_b",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;a(Lnet/minecraft/world/chunk/Chunk;)V",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private void undoFastCraftHooks2(Chunk chunk) {
        this.func_150809_p();
    }

    @Redirect(method = "func_150804_b",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;b(Lnet/minecraft/world/chunk/Chunk;Z)V",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private void undoFastCraftHooks3(Chunk chunk, boolean isRemote) {
        this.recheckGaps(isRemote);
    }


    @Redirect(method = "func_150804_b",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;v(Lnet/minecraft/world/chunk/Chunk;Z)V",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private void undoFastCraftHooks4(Chunk chunk, boolean isRemote) {

    }

    @Redirect(method = "func_150811_f",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;d(Lnet/minecraft/world/World;III)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks5(World world, int x, int y, int z) {
        return world.func_147451_t(x, y, z);
    }
}
