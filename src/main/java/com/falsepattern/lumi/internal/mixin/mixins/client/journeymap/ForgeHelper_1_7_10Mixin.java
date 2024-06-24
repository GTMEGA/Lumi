/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.client.journeymap;

import com.falsepattern.lumi.internal.mixin.hook.LightingHooks;
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
