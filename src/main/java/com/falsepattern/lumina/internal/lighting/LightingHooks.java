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

package com.falsepattern.lumina.internal.lighting;

import com.falsepattern.lumina.api.lighting.LightType;
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

    public static int getCurrentLightValue(Chunk baseChunk,
                                           EnumSkyBlock baseLightType,
                                           int subChunkPosX,
                                           int posY,
                                           int subChunkPosZ) {
        val baseWorld = baseChunk.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (baseChunk.xPosition << 4) + subChunkPosX;
        val posZ = (baseChunk.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getCurrentLightValue(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static int getBrightnessAndLightValueMax(Chunk baseChunk,
                                                    EnumSkyBlock baseLightType,
                                                    int subChunkPosX,
                                                    int posY,
                                                    int subChunkPosZ) {
        val baseWorld = baseChunk.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (baseChunk.xPosition << 4) + subChunkPosX;
        val posZ = (baseChunk.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getBrightnessAndLightValueMax(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static boolean isChunkFullyLit(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        var chunkHasLighting = true;
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            chunkHasLighting &= lightingEngine.isChunkFullyLit(chunk);
        }
        return chunkHasLighting;
    }

    public static void handleChunkInit(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        resetPrecipitationHeightMap(baseChunk);
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleChunkInit(chunk);
        }
    }

    @SideOnly(CLIENT)
    public static void handleClientChunkInit(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        resetPrecipitationHeightMap(baseChunk);
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleClientChunkInit(chunk);
        }
    }

    @SideOnly(CLIENT)
    public static void markClientChunkLightingInitialized(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val chunk = world.lumi$wrap(baseChunk);
            chunk.lumi$isLightingInitialized(true);
        }
    }

    public static void handleSubChunkInit(Chunk baseChunk, ExtendedBlockStorage baseSubChunk) {
        val baseWorld = baseChunk.worldObj;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = world.lumi$wrap(baseSubChunk);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleSubChunkInit(Chunk baseChunk, int chunkPosY) {
        val baseWorld = baseChunk.worldObj;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = chunk.lumi$getSubChunk(chunkPosY);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleChunkLoad(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleChunkLoad(chunk);
        }
    }

    public static void doRandomChunkLightingUpdates(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val chunk = world.lumi$wrap(baseChunk);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.doRandomChunkLightingUpdates(chunk);
        }
    }

    public static void updateLightingForBlock(Chunk baseChunk,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        val baseWorld = baseChunk.worldObj;
        val posX = (baseChunk.xPosition << 4) + subChunkPosX;
        val posZ = (baseChunk.zPosition << 4) + subChunkPosZ;

        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.updateLightingForBlock(posX, posY, posZ);
        }
    }

    public static void scheduleLightingUpdate(World baseWorld,
                                              EnumSkyBlock baseLightType,
                                              int posX,
                                              int posY,
                                              int posZ) {
        val lightType = LightType.of(baseLightType);
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightingUpdate(lightType, posX, posY, posZ);
        }
    }

    public static void processLightUpdates(World baseWorld) {
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightingUpdatesForAllTypes();
        }
    }

    private static void resetPrecipitationHeightMap(Chunk baseChunk) {
        Arrays.fill(baseChunk.precipitationHeightMap, DEFAULT_PRECIPITATION_HEIGHT);
    }
}
