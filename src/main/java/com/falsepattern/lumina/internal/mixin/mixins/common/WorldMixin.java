/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin implements IBlockAccess {
    @Inject(method = "tick",
            at = @At("HEAD"),
            require = 1)
    private void clearWorldBlockCacheRoot(CallbackInfo ci) {
        LightingHooks.clearWorldBlockCacheRoot(thiz());
    }

    /**
     * @author Ven
     * @reason Redirect into LUMINA
     */
    @Overwrite
    public boolean updateLightByType(EnumSkyBlock baseLightType, int posX, int posY, int posZ) {
        LightingHooks.scheduleLightingUpdate(thiz(), baseLightType, posX, posY, posZ);
        return true;
    }

    private World thiz() {
        return (World) (Object) this;
    }
}
