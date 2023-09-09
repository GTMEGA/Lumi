/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.phosphor;

import com.falsepattern.lumina.internal.lighting.phosphor.PhosphorChunk;
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
