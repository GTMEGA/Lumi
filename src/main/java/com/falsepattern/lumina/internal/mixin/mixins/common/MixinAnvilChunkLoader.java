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

import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    /**
     * Injects into the head of saveChunk() to forcefully process all pending light updates. Fail-safe.
     *
     * @author Angeline
     */
    @Inject(method = "saveChunk",
            at = @At("HEAD"),
            require = 1)
    private void onSaveChunk(World world, Chunk chunk, CallbackInfo callbackInfo) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (int i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(world, i);
            lumiWorld.lightingEngine().processLightUpdates();
        }
    }
}
