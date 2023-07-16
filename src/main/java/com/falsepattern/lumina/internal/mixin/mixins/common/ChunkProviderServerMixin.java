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

import com.falsepattern.lumina.internal.lighting.LightingHooks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin {
    @Shadow
    @SuppressWarnings("rawtypes")
    private Set chunksToUnload;
    @Shadow
    public WorldServer worldObj;

    @Inject(method = "saveChunks",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnSave(boolean saveAll, IProgressUpdate progressUpdate, CallbackInfoReturnable<Boolean> cir) {
        LightingHooks.processLightUpdates(worldObj);
    }

    @Inject(method = "unloadQueuedChunks",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnUnload(CallbackInfoReturnable<Boolean> cir) {
        if (worldObj.levelSaving)
            return;
        if (chunksToUnload.isEmpty())
            return;

        LightingHooks.processLightUpdates(worldObj);
    }
}
