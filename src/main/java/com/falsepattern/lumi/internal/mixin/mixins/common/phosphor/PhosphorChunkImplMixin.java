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

package com.falsepattern.lumi.internal.mixin.mixins.common.phosphor;

import com.falsepattern.lumi.internal.lighting.phosphor.PhosphorChunk;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Unique
@Mixin(Chunk.class)
public abstract class PhosphorChunkImplMixin implements PhosphorChunk {
    private short[] phosphor$lightCheckFlags;

    @Override
    public short[] phosphor$lightCheckFlags() {
        if (phosphor$lightCheckFlags == null)
            phosphor$lightCheckFlags = new short[LIGHT_CHECK_FLAGS_LENGTH];
        return phosphor$lightCheckFlags;
    }
}
