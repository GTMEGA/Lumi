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

package com.falsepattern.lumina.api.lighting;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.world.EnumSkyBlock;

@Getter
@Accessors(fluent = true, chain = false)
public enum LightValueType {
    BLOCK_LIGHT_VALUE(EnumSkyBlock.Block),
    SKY_LIGHT_VALUE(EnumSkyBlock.Sky),
    ;

    private final int defaultLightValue;

    LightValueType(EnumSkyBlock baseLightType) {
        defaultLightValue = baseLightType.defaultLightValue;
    }

    public static LightValueType of(EnumSkyBlock baseLightType) {
        switch (baseLightType) {
            default:
            case Block:
                return BLOCK_LIGHT_VALUE;
            case Sky:
                return SKY_LIGHT_VALUE;
        }
    }
}
