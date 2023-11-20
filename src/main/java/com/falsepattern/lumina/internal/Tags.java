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

package com.falsepattern.lumina.internal;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Tags {
    public static final String MOD_ID = "GRADLETOKEN_MODID";
    public static final String MOD_NAME = "GRADLETOKEN_MODNAME";
    public static final String VERSION = "GRADLETOKEN_VERSION";

    public static final String MINECRAFT_VERSION = "[1.7.10]";
    public static final String DEPENDENCIES = "required-after:chunkapi@[0.4.0,0.5.0);" +
                                              "required-after:falsepatternlib@[1.0.0,);" +
                                              "after:falsetweaks;" +
                                              "required-after:gasstation";
}
