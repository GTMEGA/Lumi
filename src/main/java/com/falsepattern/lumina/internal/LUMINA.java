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

package com.falsepattern.lumina.internal;

import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.LumiWorldProviderRegistry;
import com.falsepattern.lumina.internal.saving.LuminaDataManager;
import com.falsepattern.lumina.internal.world.LumiWorldManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.11,);required-after:chunkapi@[0.2,)")
public class LUMINA {
    private static final AtomicBoolean hijacked = new AtomicBoolean(false);
    private static final AtomicBoolean hijackLocked = new AtomicBoolean(false);
    public static void hijack() {
        if (hijackLocked.get()) {
            throw new IllegalStateException("Hijacking the default lighting engine is only possible during preInit!");
        }
        hijacked.set(true);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LumiWorldManager.startInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        hijackLocked.set(true);
        if (!hijacked.get()) {
            LumiWorldProviderRegistry.registerWorldProvider(world -> (LumiWorld)world);
        }
        ChunkDataRegistry.registerDataManager(new LuminaDataManager());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LumiWorldManager.finishInit();
        if (hijacked.get() && LumiWorldManager.lumiWorldCount() == 0) {
            throw new IllegalStateException("Lumina was hijacked but no default world manager was registered!");
        }
        ChunkDataRegistry.disableDataManager("minecraft", "lighting");
    }
}
