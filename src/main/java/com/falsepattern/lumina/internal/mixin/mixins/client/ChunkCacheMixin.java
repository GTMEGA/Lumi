/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin implements IBlockAccess {
    @Redirect(method = "getSpecialBlockBrightness",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 1)
    private int getBrightnessAndLightValueMax(Chunk chunkBase,
                                              EnumSkyBlock baseLightType,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        return LightingHooks.getMaxBrightness(chunkBase, baseLightType, subChunkPosX, posY, subChunkPosZ);
    }
}
