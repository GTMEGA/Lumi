/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.internal.world.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

@Mixin(ChunkCache.class)
public class MixinChunkCache {
    @Redirect(method = "getSpecialBlockBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"))
    private int getIntrinsicValue(Chunk instance, EnumSkyBlock type, int posX, int posY, int posZ) {
        val lWorld = LumiWorldManager.getWorld(instance.worldObj, 0);
        val lChunk = lWorld.wrap(instance);
        return type == EnumSkyBlock.Sky ?
                instance.getSavedLightValue(type, posX, posY, posZ) :
                LightingHooks.getIntrinsicOrSavedBlockLightValue(lChunk, posX, posY, posZ);
    }
}
