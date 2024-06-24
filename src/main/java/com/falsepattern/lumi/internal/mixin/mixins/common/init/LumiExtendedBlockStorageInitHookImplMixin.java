/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.init;

import com.falsepattern.lumi.api.init.LumiExtendedBlockStorageInitHook;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiExtendedBlockStorageInitHookImplMixin implements LumiExtendedBlockStorageInitHook {
    @Unique
    private boolean lumi$initHookExecuted;
    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiExtendedBlockStorageHook(CallbackInfo ci) {
        lumi$doExtendedBlockStorageInit();
    }

    @Override
    public void lumi$doExtendedBlockStorageInit() {
        if (lumi$initHookExecuted)
            return;
        lumi$onExtendedBlockStorageInit();
        lumi$initHookExecuted = true;
    }

    @Override
    public boolean lumi$initHookExecuted() {
        return lumi$initHookExecuted;
    }

    @Override
    public void lumi$onExtendedBlockStorageInit() {}
}
