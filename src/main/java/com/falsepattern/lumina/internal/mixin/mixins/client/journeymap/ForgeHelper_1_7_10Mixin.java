/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.falsepattern.lumina.internal.mixin.mixins.client.journeymap;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import journeymap.client.forge.helper.impl.ForgeHelper_1_7_10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

@Mixin(value = ForgeHelper_1_7_10.class,
       remap = false)
public abstract class ForgeHelper_1_7_10Mixin {
    @Shadow public abstract EnumSkyBlock getSkyBlock();

    /**
     * @author FalsePattern
     * @reason Compat fix, obliterates the cache otherwise
     */
    @Overwrite
    public int getSavedLightValue(Chunk chunk, int localX, int y, int localZ) {
        try {
            return LightingHooks.getCurrentLightValueUncached(chunk, this.getSkyBlock(), localX & 15, y, localZ & 15);
        } catch (ArrayIndexOutOfBoundsException var6) {
            return 1;
        }
    }
}
