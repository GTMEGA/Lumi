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
import lombok.var;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.world.chunk.Chunk;
@Mixin(value = S21PacketChunkData.class,
       priority = 1001)
public abstract class MixinSPacketChunkData {
    /**
     * @author Angeline
     * Injects a callback into SPacketChunkData#calculateChunkSize(Chunk, booolean, int) to force light updates to be
     * processed before creating the client payload. We use this method rather than the constructor as it is not valid
     * to inject elsewhere other than the RETURN of a ctor, which is too late for our needs.
     */
    @Inject(method = "func_149269_a",
            at = @At("HEAD"),
            require = 1)
    private static void onCalculateChunkSize(Chunk chunk,
                                             boolean hasSkyLight,
                                             int changedSectionFilter,
                                             CallbackInfoReturnable<S21PacketChunkData.Extracted> cir) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(chunk.worldObj, i);
            val lumiChunk = lumiWorld.lumiWrap(chunk);
            lumiChunk.getLightingEngine().processLightUpdates();
        }
    }
}
