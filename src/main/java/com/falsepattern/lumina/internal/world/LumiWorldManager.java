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
import com.falsepattern.lumina.internal.lighting.PhosphorLightingEngine;
import lombok.val;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicBoolean;

//HOLY COW I'M TOTALLY GOING SO FAST OH F***
public class LumiWorldManager {
    //Not using a list because:
    //1. This array only needs to grow during init
    //2. Elements are never removed
    //3. Bounds are known
    //Growth factor is 1, because the number of custom providers is expected to be very small
    private static LumiWorldProvider[] providers = new LumiWorldProvider[0];
    private static final AtomicBoolean initStarted = new AtomicBoolean(false);
    private static final AtomicBoolean locked = new AtomicBoolean(true);

    public static int lumiWorldCount() {
        return providers.length;
    }

    //No bounds checking, because this is an internal method
    public static LumiWorld getWorld(World world, int i) {
        return providers[i].lumi$wrap(world);
    }

    //Synchronized just in case, only called during init anyway
    public static synchronized void addProvider(LumiWorldProvider provider) {
        if (locked.get()) {
            throw new IllegalStateException("Providers can only be registered during init!");
        }
        val oldProviders = providers;
        LumiWorldProvider[] newProviders = new LumiWorldProvider[oldProviders.length + 1];
        System.arraycopy(oldProviders, 0, newProviders, 0, oldProviders.length);
        newProviders[newProviders.length - 1] = provider;
        providers = newProviders;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static void initialize(World world) {
        val providers = LumiWorldManager.providers;
        for (int i = 0, providersLength = providers.length; i < providersLength; i++) {
            LumiWorldProvider provider = providers[i];
            val lWorld = provider.lumi$wrap(world);
            lWorld.lumi$lightingEngine(new PhosphorLightingEngine(lWorld));
        }
    }

    public static void startInit() {
        if (initStarted.get()) {
            throw new IllegalStateException("Cannot start init twice!");
        }
        initStarted.set(true);
        locked.set(false);
    }

    public static void finishInit() {
        if (!initStarted.get()) {
            throw new IllegalStateException("Cannot lock before starting init!");
        }
        locked.set(true);
    }
}
