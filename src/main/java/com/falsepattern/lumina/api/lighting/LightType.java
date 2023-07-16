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

import lombok.val;
import lombok.var;
import net.minecraft.world.EnumSkyBlock;

@SuppressWarnings("unused")
public enum LightType {
    BLOCK_LIGHT_TYPE(EnumSkyBlock.Block.defaultLightValue),
    SKY_LIGHT_TYPE(EnumSkyBlock.Sky.defaultLightValue),
    ;

    private static final int MIN_BASE_LIGHT_VALUE;
    private static final int MAX_BASE_LIGHT_VALUE;

    static {
        var minBaseLightValue = Integer.MAX_VALUE;
        var maxBaseLightValue = Integer.MIN_VALUE;
        for (val lightType : values()) {
            val defaultLightValue = lightType.defaultLightValue;
            minBaseLightValue = Math.min(maxBaseLightValue, defaultLightValue);
            maxBaseLightValue = Math.max(maxBaseLightValue, defaultLightValue);
        }
        MIN_BASE_LIGHT_VALUE = minBaseLightValue;
        MAX_BASE_LIGHT_VALUE = maxBaseLightValue;
    }

    private final int defaultLightValue;

    LightType(int defaultLightValue) {
        this.defaultLightValue = defaultLightValue;
    }

    public static LightType of(EnumSkyBlock baseLightType) {
        switch (baseLightType) {
            default:
            case Block:
                return BLOCK_LIGHT_TYPE;
            case Sky:
                return SKY_LIGHT_TYPE;
        }
    }

    public static int minBaseLightValue() {
        return MIN_BASE_LIGHT_VALUE;
    }

    public static int maxBaseLightValue() {
        return MAX_BASE_LIGHT_VALUE;
    }

    public int defaultLightValue() {
        return this.defaultLightValue;
    }
}
