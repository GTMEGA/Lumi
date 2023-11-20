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

package com.falsepattern.lumina.internal.mixin.mixins.common.init;

import com.falsepattern.lumina.api.init.LumiExtendedBlockStorageInitHook;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiExtendedBlockStorageInitHookImplMixin implements LumiExtendedBlockStorageInitHook {
    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiExtendedBlockStorageHook(CallbackInfo ci) {
        lumi$onExtendedBlockStorageInit();
    }

    @Override
    public void lumi$onExtendedBlockStorageInit() {}
}
