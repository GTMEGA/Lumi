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

package com.falsepattern.lumi.internal.mixin.mixins.common.init;

import com.falsepattern.lumi.api.init.LumiWorldInitHook;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Unique
@Mixin(World.class)
public abstract class LumiWorldInitHookImplMixin implements LumiWorldInitHook {
    @Mutable
    @Final
    @Shadow
    public Profiler theProfiler;

    @Redirect(method = "<init>(" +
                       "Lnet/minecraft/world/storage/ISaveHandler;" +
                       "Ljava/lang/String;" +
                       "Lnet/minecraft/world/WorldSettings;" +
                       "Lnet/minecraft/world/WorldProvider;" +
                       "Lnet/minecraft/profiler/Profiler;" +
                       ")V",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/world/World;" +
                                "theProfiler:Lnet/minecraft/profiler/Profiler;"),
              require = 1)
    private void lumiWorldInitHook(World thiz, Profiler profiler) {
        this.theProfiler = profiler;
        lumi$onWorldInit();
    }

    @Override
    public void lumi$onWorldInit() {}
}
