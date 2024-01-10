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

package com.falsepattern.lumina.internal.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lumina.internal.Tags;
import com.falsepattern.lumina.internal.cache.MultiHeadBlockCacheRoot;
import lombok.experimental.UtilityClass;

@UtilityClass
@Config(modid = Tags.MOD_ID)
public final class LumiConfig {
    @Config.Comment("Use locks when accessing the lighting engine state." +
                    "This option may improve performance but will allow illegal access to the lighting engine from other threads.")
    @Config.LangKey("config.lumina.enableLocks")
    @Config.DefaultBoolean(true)
    @Config.RequiresWorldRestart
    public static boolean ENABLE_LOCKS;

    @Config.Comment("Print a warning when the lighting engine is accessed by the wrong thread.")
    @Config.LangKey("config.lumina.enableIllegalThreadAccessWarnings")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS;

    @Config.Comment("Add extra caching to the lighting engine.\n" +
                    "0 to disable\n" +
                    "Causes a memory leak with threaded chunk rendering in FalseTweaks, so it's force-disabled if that feature is enabled.")
    @Config.LangKey("config.lumina.cacheCount")
    @Config.DefaultInt(0)
    @Config.RangeInt(min = 0, max = MultiHeadBlockCacheRoot.MAX_MULTI_HEAD_CACHE_COUNT)
    @Config.RequiresMcRestart
    public static int CACHE_COUNT;

    static {
        ConfigurationManager.selfInit();
    }

    public static void poke() {}
}
