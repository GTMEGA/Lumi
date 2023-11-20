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

package com.falsepattern.lumina.api.cache;

import com.falsepattern.lumina.api.storage.LumiBlockStorageRoot;
import com.falsepattern.lumina.api.world.LumiWorld;
import org.jetbrains.annotations.NotNull;

public interface LumiBlockCacheRoot extends LumiBlockStorageRoot {
    @NotNull String lumi$blockCacheRootID();

    @NotNull LumiBlockCache lumi$createBlockCache(LumiWorld world);

    /**
     * Should be called at the end of each tick.
     * <p>
     * On some implementations this will do nothing.
     */
    void lumi$clearCache();
}
