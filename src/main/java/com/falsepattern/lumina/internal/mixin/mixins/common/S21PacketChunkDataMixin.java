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

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.POST_CHUNK_API_MIXIN_PRIORITY;

@Mixin(value = S21PacketChunkData.class, priority = POST_CHUNK_API_MIXIN_PRIORITY)
public abstract class S21PacketChunkDataMixin {
    @Inject(method = "func_149269_a",
            at = @At("HEAD"),
            require = 1)
    private static void processLightUpdatesOnReceive(Chunk chunkBase,
                                                     boolean hasSky,
                                                     int subChunkMask,
                                                     CallbackInfoReturnable<S21PacketChunkData.Extracted> cir) {
        LightingHooks.processLightUpdates(chunkBase);
    }
}
