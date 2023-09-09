/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.lighting;

import net.minecraft.world.EnumSkyBlock;

@SuppressWarnings("unused")
public enum LightType {
    BLOCK_LIGHT_TYPE(EnumSkyBlock.Block.defaultLightValue),
    SKY_LIGHT_TYPE(EnumSkyBlock.Sky.defaultLightValue),
    ;

    private static final int MIN_BASE_LIGHT_VALUE;
    private static final int MAX_BASE_LIGHT_VALUE;

    static {
        int minBaseLightValue = Integer.MAX_VALUE;
        int maxBaseLightValue = Integer.MIN_VALUE;
        for (LightType lightType : values()) {
            final int defaultLightValue = lightType.defaultLightValue;
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
