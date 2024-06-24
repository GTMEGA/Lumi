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

package com.falsepattern.lumi.internal.world;

import com.falsepattern.lumi.api.world.LumiWorld;
import com.falsepattern.lumi.api.world.LumiWorldProvider;
import com.falsepattern.lumi.internal.Tags;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DefaultWorldProvider implements LumiWorldProvider {
    private static final DefaultWorldProvider INSTANCE = new DefaultWorldProvider();
    @Getter
    private static boolean isRegistered;

    public static DefaultWorldProvider defaultWorldProvider() {
        return INSTANCE;
    }

    public static void setRegistered() {
        isRegistered = true;
    }

    @Override
    public @NotNull String worldProviderID() {
        return "lumi_world_provider";
    }

    @Override
    public @NotNull String worldProviderVersion() {
        return Tags.VERSION;
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiWorld provideWorld(@Nullable World worldBase) {
        if (worldBase instanceof LumiWorld)
            return (LumiWorld) worldBase;
        return null;
    }
}
