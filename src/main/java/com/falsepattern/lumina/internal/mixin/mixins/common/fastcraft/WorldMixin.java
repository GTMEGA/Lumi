/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.fastcraft;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        return world.func_147451_t(x, y, z);
    }

    @Redirect(method = "setActivePlayerChunksAndCheckLight",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;d(Lnet/minecraft/world/World;III)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks2(World world, int x, int y, int z) {
        return world.func_147451_t(x, y, z);
    }

    @Redirect(method = "markAndNotifyBlock",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;w(Lnet/minecraft/world/chunk/Chunk;)Z",
                       remap = false),
              remap = false,
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks3(Chunk chunk) {
        return chunk.func_150802_k();
    }

    @Redirect(method = "setBlockMetadataWithNotify",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/H;w(Lnet/minecraft/world/chunk/Chunk;)Z",
                       remap = false),
              require = 0,
              expect = 0)
    @Dynamic
    private boolean undoFastCraftHooks4(Chunk chunk) {
        return chunk.func_150802_k();
    }
}
