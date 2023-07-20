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

package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import lombok.NoArgsConstructor;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DefaultWorldProvider implements LumiWorldProvider {
    private static final DefaultWorldProvider INSTANCE = new DefaultWorldProvider();

    public static DefaultWorldProvider defaultWorldProvider() {
        return INSTANCE;
    }

    @Override
    public @NotNull String worldProviderID() {
        return "lumi_world_provider";
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiWorld provideWorld(@Nullable World worldBase) {
        if (worldBase instanceof LumiWorld)
            return (LumiWorld) worldBase;
        return null;
    }
}
