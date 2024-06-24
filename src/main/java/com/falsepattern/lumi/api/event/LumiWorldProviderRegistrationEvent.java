/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.api.event;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.world.LumiWorldProviderRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@StableAPI(since = "__EXPERIMENTAL__")
public final class LumiWorldProviderRegistrationEvent extends Event {
    private final @NotNull LumiWorldProviderRegistry registry;

    @StableAPI.Internal
    public LumiWorldProviderRegistrationEvent(@NotNull LumiWorldProviderRegistry registry) {
        this.registry = registry;
    }

    @StableAPI.Expose
    public @NotNull LumiWorldProviderRegistry registry() {
        return registry;
    }
}
