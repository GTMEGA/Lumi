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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.api.ILumiWorldProvider;
import com.falsepattern.lumina.internal.LumiWorldManager;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {
    @Shadow private Set chunksToUnload;

    @Shadow public WorldServer worldObj;

    /**
     * Injects a callback into the start of saveChunks(boolean) to force all light updates to be processed before saving.
     *
     * @author Angeline
     */
    @Inject(method = "saveChunks", at = @At("HEAD"))
    private void onSaveChunks(boolean all, IProgressUpdate update, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            world.getLightingEngine().processLightUpdates();
        }
    }

    /**
     * Injects a callback into the start of the onTick() method to process all pending light updates. This is not necessarily
     * required, but we don't want our work queues getting too large.
     *
     * @author Angeline
     */
    @Inject(method = "unloadQueuedChunks", at = @At("HEAD"))
    private void onTick(CallbackInfoReturnable<Boolean> cir) {
        if (!this.worldObj.levelSaving) {
            if (!this.chunksToUnload.isEmpty()) {
                for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                    val world = LumiWorldManager.getWorld(worldObj, i);
                    world.getLightingEngine().processLightUpdates();
                }
            }
        }
    }
}
