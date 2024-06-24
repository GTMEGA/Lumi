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

package com.falsepattern.lumi.internal.mixin.mixins.client;

import com.falsepattern.lumi.internal.mixin.hook.LightingHooks;
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
