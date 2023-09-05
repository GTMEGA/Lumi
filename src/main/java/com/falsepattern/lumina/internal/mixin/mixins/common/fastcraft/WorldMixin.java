/*
 * Copyright (c) 2023 FalsePattern, Ven
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

package com.falsepattern.lumina.internal.mixin.mixins.common.fastcraft;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.World;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract boolean func_147451_t(int p_147451_1_, int p_147451_2_, int p_147451_3_);

    @Redirect(method = "setBlock(IIILnet/minecraft/block/Block;II)Z",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;d(Lnet/minecraft/world/World;III)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks1(World world, int x, int y, int z) {
        return this.func_147451_t(x, y, z);
    }

    @Redirect(method = "setActivePlayerChunksAndCheckLight",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;d(Lnet/minecraft/world/World;III)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks2(World world, int x, int y, int z) {
        return this.func_147451_t(x, y, z);
    }
}
