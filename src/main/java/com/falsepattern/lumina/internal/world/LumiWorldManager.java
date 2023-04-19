/*
 * Copyright (C) 2023 FalsePattern
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

import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILumiWorldProvider;
import com.falsepattern.lumina.internal.world.lighting.LightingEngine;
import lombok.val;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LumiWorldManager {
    private static final List<ILumiWorldProvider> providers = new ArrayList<>();
    static {
        providers.add(world -> (ILumiWorld) world);
    }

    public static int lumiWorldCount() {
        return providers.size();
    }

    public static ILumiWorld getWorld(World world, int i) {
        return providers.get(i).getWorld(world);
    }

    public static void initialize(World world) {
        for (final ILumiWorldProvider provider: providers) {
            val lWorld = provider.getWorld(world);
            lWorld.setLightingEngine(new LightingEngine(lWorld));
        }
    }
}
