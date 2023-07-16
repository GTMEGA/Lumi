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

package com.falsepattern.lumina.api.event;

import com.falsepattern.lumina.api.world.LumiWorldRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class LumiWorldRegistrationEvent extends Event {
    private final @NotNull LumiWorldRegistry registry;

    public LumiWorldRegistrationEvent(@NotNull LumiWorldRegistry registry) {
        this.registry = registry;
    }

    public @NotNull LumiWorldRegistry registry() {
        return this.registry;
    }
}
