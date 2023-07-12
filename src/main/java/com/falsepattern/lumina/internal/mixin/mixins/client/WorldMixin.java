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

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import lombok.val;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin implements IBlockAccess {
    @Inject(method = "finishSetup",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void initClientLumiWorld(CallbackInfo ci) {
//        LumiWorldManager.initialize(thiz());
    }

    @Redirect(method = {"getSkyBlockTypeBrightness", "getSavedLightValue"},
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 1)
    private int getBrightnessAndLightValueMax(Chunk baseChunk,
                                              EnumSkyBlock lightType,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        val chunk = (LumiChunk) baseChunk;
        return chunk.lumi$getBrightnessAndLightValueMax(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    private World thiz() {
        return (World) (Object) this;
    }
}
