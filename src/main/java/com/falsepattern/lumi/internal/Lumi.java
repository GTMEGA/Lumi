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

package com.falsepattern.lumi.internal;

import com.falsepattern.chunk.api.DataRegistry;
import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumi.internal.Share.LOG;
import static com.falsepattern.lumi.internal.Tags.*;
import static com.falsepattern.lumi.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumi.internal.storage.ChunkNBTManager.chunkNBTManager;
import static com.falsepattern.lumi.internal.storage.ChunkPacketManager.chunkPacketManager;
import static com.falsepattern.lumi.internal.storage.SubChunkNBTManager.subChunkNBTManager;
import static com.falsepattern.lumi.internal.world.WorldProviderManager.worldProviderManager;

@Mod(modid = MOD_ID,
     version = VERSION,
     name = MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:chunkapi@[0.6.1,);" +
             "required-after:falsepatternlib@[1.6.0,);" +
             "after:falsetweaks@[3.9.6,);",
     guiFactory = GROUPNAME + ".internal.config.LumiGuiFactory")
@NoArgsConstructor
public final class Lumi {
    public static Logger createLogger(String name) {
        return LogManager.getLogger(MOD_NAME + "|" + name);
    }

    private static boolean falseTweaks;
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        falseTweaks = Loader.isModLoaded("falsetweaks");
    }

    public static boolean lumi$isThreadedUpdates() {
        return falseTweaks && ThreadedChunkUpdates.isEnabled();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        worldProviderManager().registerWorldProviders();
        lightingEngineManager().registerLightingEngineProvider();

        chunkNBTManager().registerDataManager();
        subChunkNBTManager().registerDataManager();
        chunkPacketManager().registerDataManager();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        DataRegistry.disableDataManager("minecraft", "lighting");
        LOG.info("Disabled [minecraft:lighting] data manager");
        DataRegistry.disableDataManager("minecraft", "blocklight");
        LOG.info("Disabled [minecraft:blocklight] data manager");
        DataRegistry.disableDataManager("minecraft", "skylight");
        LOG.info("Disabled [minecraft:skylight] data manager");
    }
}
