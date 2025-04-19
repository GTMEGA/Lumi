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

package com.falsepattern.lumi.api.event;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.lighting.LumiLightingEngineRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@StableAPI(since = "__EXPERIMENTAL__")
public final class LumiLightingEngineRegistrationEvent extends Event {
    private final @NotNull LumiLightingEngineRegistry registry;

    @StableAPI.Internal
    public LumiLightingEngineRegistrationEvent(@NotNull LumiLightingEngineRegistry registry) {
        this.registry = registry;
    }

    @StableAPI.Expose
    public @NotNull LumiLightingEngineRegistry registry() {
        return this.registry;
    }
}
