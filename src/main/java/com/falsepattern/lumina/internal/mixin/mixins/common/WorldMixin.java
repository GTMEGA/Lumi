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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.val;
import lombok.var;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin implements IBlockAccess {
    @Redirect(method = "<init>(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/profiler/Profiler;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/storage/ISaveHandler;loadWorldInfo()Lnet/minecraft/world/storage/WorldInfo;"),
              require = 1)
    private WorldInfo initLumiWorld(ISaveHandler handler) {
//        LumiWorldManager.initialize(thiz());
        return handler.loadWorldInfo();
    }

    @Inject(method = "updateLightByType",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void scheduleLightUpdate(EnumSkyBlock lightType, int posX, int posY, int posZ, CallbackInfoReturnable<Boolean> cir) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(thiz(), i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
        }
        cir.setReturnValue(true);
    }

    private World thiz() {
        return (World) (Object) this;
    }
}
