/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.hook;

import com.falsepattern.lumina.api.LumiChunkAPI;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static cpw.mods.fml.relauncher.Side.CLIENT;

@UtilityClass
public final class LightingHooks {
    private static final int DEFAULT_PRECIPITATION_HEIGHT = -999;

    public static void clearWorldBlockCacheRoot(World worldBase) {
        if (worldBase instanceof LumiWorldRoot) {
            val worldRoot = (LumiWorldRoot) worldBase;
            worldRoot.lumi$blockCacheRoot().lumi$clearCache();
        }
    }

    public static int getCurrentLightValue(Chunk chunkBase,
                                           EnumSkyBlock baseLightType,
                                           int subChunkPosX,
                                           int posY,
                                           int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getCurrentLightValue(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static int getCurrentLightValueUncached(Chunk chunkBase,
                                                   EnumSkyBlock baseLightType,
                                                   int subChunkPosX,
                                                   int posY,
                                                   int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getCurrentLightValueUncached(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static int getMaxBrightness(Chunk chunkBase,
                                       EnumSkyBlock baseLightType,
                                       int subChunkPosX,
                                       int posY,
                                       int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightValue = world.lumi$getBrightness(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static boolean isChunkFullyLit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        var chunkHasLighting = true;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            chunkHasLighting &= lightingEngine.isChunkFullyLit(chunk);
        }
        return chunkHasLighting;
    }

    public static void handleChunkInit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        resetPrecipitationHeightMap(chunkBase);
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            LumiChunkAPI.scheduleChunkLightingEngineInit(chunk);
        }
    }

    @SideOnly(CLIENT)
    public static void handleClientChunkInit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        resetPrecipitationHeightMap(chunkBase);
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            lightingEngine.handleClientChunkInit(chunk);
        }
    }

    public static void handleSubChunkInit(Chunk chunkBase, ExtendedBlockStorage subChunkBase) {
        val worldBase = chunkBase.worldObj;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = world.lumi$wrap(subChunkBase);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleSubChunkInit(Chunk chunkBase, int chunkPosY) {
        val worldBase = chunkBase.worldObj;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = chunk.lumi$getSubChunk(chunkPosY);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleChunkLoad(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            lightingEngine.handleChunkLoad(chunk);
        }
    }

    public static void doRandomChunkLightingUpdates(Chunk chunkBase) {
        if (!chunkBase.worldObj.isRemote && chunkBase.inhabitedTime < 10 * 20)
            return;

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.doRandomChunkLightingUpdates(chunk);
        }
    }

    public static void resetQueuedRandomLightUpdates(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            chunk.lumi$resetQueuedRandomLightUpdates();
        }
    }

    public static void updateLightingForBlock(Chunk chunkBase,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.updateLightingForBlock(posX, posY, posZ);
        }
    }

    public static void scheduleLightingUpdate(World worldBase,
                                              EnumSkyBlock baseLightType,
                                              int posX,
                                              int posY,
                                              int posZ) {
        val lightType = LightType.of(baseLightType);
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightingUpdate(lightType, posX, posY, posZ);
        }
    }

    public static void processLightUpdates(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;
        processLightUpdates(worldBase);
    }

    public static void processLightUpdates(World worldBase) {
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightingUpdatesForAllTypes();
        }
    }

    private static void resetPrecipitationHeightMap(Chunk chunkBase) {
        Arrays.fill(chunkBase.precipitationHeightMap, DEFAULT_PRECIPITATION_HEIGHT);
    }
}
