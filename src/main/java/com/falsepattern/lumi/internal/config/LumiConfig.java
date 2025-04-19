/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

package com.falsepattern.lumi.internal.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lumi.internal.Tags;
import lombok.experimental.UtilityClass;

@UtilityClass
@Config(modid = Tags.MOD_ID)
public final class LumiConfig {
    @Config.Comment("Use locks when accessing the lighting engine state." +
                    "This option may improve performance but will allow illegal access to the lighting engine from other threads.")
    @Config.LangKey("config.lumi.enableLocks")
    @Config.DefaultBoolean(true)
    @Config.RequiresWorldRestart
    public static boolean ENABLE_LOCKS;

    @Config.Comment("Print a warning when the lighting engine is accessed by the wrong thread.")
    @Config.LangKey("config.lumi.enableIllegalThreadAccessWarnings")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS;

    @Config.Comment("Random light updates are disabled by default, as the reference Phosphor implementation provided is more robust compared to vanilla.")
    @Config.LangKey("config.lumi.doRandomLightUpdates")
    @Config.DefaultBoolean(false)
    public static boolean DO_RANDOM_LIGHT_UPDATES;

    @Config.Comment("Increases the lighting engine internal buffer sizes from 256kB to 4MB per serverside world. Uses more ram, but improves worldgen speed.\n" +
                    "Not recommended on 4GB heap or smaller.")
    @Config.LangKey("config.lumi.enoughRam")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean I_HAVE_ENOUGH_RAM;

    static {
        ConfigurationManager.selfInit();
    }

    public static void poke() {}
}
