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

//HOLY COW I'M TOTALLY GOING SO FAST OH F***
public class LumiWorldManager {
    //Not using a list because:
    //1. This array only needs to grow during init
    //2. Elements are never removed
    //3. Bounds are known
    //Growth factor is 1, because the number of custom providers is expected to be very small
    private static ILumiWorldProvider[] providers = new ILumiWorldProvider[0];

    public static int lumiWorldCount() {
        return providers.length;
    }

    //No bounds checking, because this is an internal method
    public static ILumiWorld getWorld(World world, int i) {
        return providers[i].getWorld(world);
    }

    //Synchronized just in case, only called during init anyway
    public static synchronized void addProvider(ILumiWorldProvider provider) {
        val oldProviders = providers;
        ILumiWorldProvider[] newProviders = new ILumiWorldProvider[oldProviders.length + 1];
        System.arraycopy(oldProviders, 0, newProviders, 0, oldProviders.length);
        newProviders[newProviders.length - 1] = provider;
        providers = newProviders;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static void initialize(World world) {
        val providers = LumiWorldManager.providers;
        for (int i = 0, providersLength = providers.length; i < providersLength; i++) {
            ILumiWorldProvider provider = providers[i];
            val lWorld = provider.getWorld(world);
            lWorld.setLightingEngine(new LightingEngine(lWorld));
        }
    }
}
