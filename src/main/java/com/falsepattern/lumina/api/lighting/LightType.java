/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.lighting;

import net.minecraft.world.EnumSkyBlock;

public enum LightType {
    BLOCK_LIGHT_TYPE(EnumSkyBlock.Block.defaultLightValue, true, false),
    SKY_LIGHT_TYPE(EnumSkyBlock.Sky.defaultLightValue, false, true);

    private static final int MIN_BASE_LIGHT_VALUE;
    private static final int MAX_BASE_LIGHT_VALUE;

    static {
        int minBaseLightValue = Integer.MAX_VALUE;
        int maxBaseLightValue = Integer.MIN_VALUE;
        for (final LightType lightType : values()) {
            final int defaultLightValue = lightType.defaultLightValue;
            minBaseLightValue = Math.min(maxBaseLightValue, defaultLightValue);
            maxBaseLightValue = Math.max(maxBaseLightValue, defaultLightValue);
        }
        MIN_BASE_LIGHT_VALUE = minBaseLightValue;
        MAX_BASE_LIGHT_VALUE = maxBaseLightValue;
    }

    private final int defaultLightValue;
    private final boolean isBlock;
    private final boolean isSky;

    LightType(int defaultLightValue, boolean isBlock, boolean isSky) {
        this.defaultLightValue = defaultLightValue;
        this.isBlock = isBlock;
        this.isSky = isSky;
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

    public int defaultLightValue() {return this.defaultLightValue;}

    public boolean isBlock() {return this.isBlock;}

    public boolean isSky() {return this.isSky;}
}
