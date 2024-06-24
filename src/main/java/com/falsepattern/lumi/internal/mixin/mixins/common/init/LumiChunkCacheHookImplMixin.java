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

import com.falsepattern.lumi.api.init.LumiChunkCacheInitHook;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Unique
@Mixin(ChunkCache.class)
public abstract class LumiChunkCacheHookImplMixin implements LumiChunkCacheInitHook {
    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void lumiChunkCacheInitHook(CallbackInfo ci) {
        lumi$onChunkCacheInit();
    }

    @Override
    public void lumi$onChunkCacheInit() {}
}
