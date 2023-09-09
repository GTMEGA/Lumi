/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import lombok.val;
import net.minecraft.util.LongHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LongHashMap.class)
public abstract class LongHashMapMixin {
    private static final int HASH_PRIME = 92821;

    /**
     * @author FalsePattern
     * @reason Small perf snippet, ported from ArchaicFix, LGPLv3.
     */
    @Overwrite
    private static int getHashedKey(long key) {
        val a = (int) key;
        val b = (int) (key >>> 32);
        return a + (b * HASH_PRIME);
    }
}
