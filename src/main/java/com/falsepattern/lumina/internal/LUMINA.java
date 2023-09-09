/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal;

import com.falsepattern.chunk.api.ChunkDataRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.internal.Tags.*;
import static com.falsepattern.lumina.internal.data.ChunkNBTManager.chunkNBTManager;
import static com.falsepattern.lumina.internal.data.ChunkPacketManager.chunkPacketManager;
import static com.falsepattern.lumina.internal.data.SubChunkNBTManager.subChunkNBTManager;
import static com.falsepattern.lumina.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;

@Mod(modid = MOD_ID,
     version = VERSION,
     name = MOD_NAME,
     acceptedMinecraftVersions = MINECRAFT_VERSION,
     dependencies = DEPENDENCIES)
@NoArgsConstructor
public final class LUMINA {
    private static final Logger LOG = LogManager.getLogger(MOD_NAME);

    public static Logger createLogger(String name) {
        return LogManager.getLogger(MOD_NAME + "|" + name);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
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
        ChunkDataRegistry.disableDataManager("minecraft", "lighting");
        LOG.info("Disabled [minecraft:lighting] data manager");
        ChunkDataRegistry.disableDataManager("minecraft", "blocklight");
        LOG.info("Disabled [minecraft:blocklight] data manager");
        ChunkDataRegistry.disableDataManager("minecraft", "skylight");
        LOG.info("Disabled [minecraft:skylight] data manager");
    }
}
