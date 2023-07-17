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

    public static int getBrightnessAndLightValueMax(Chunk chunkBase,
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
            val lightValue = lightingEngine.getBrightnessAndLightValueMax(lightType, posX, posY, posZ);
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
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            lightingEngine.handleChunkInit(chunk);
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
        val worldBase = chunkBase.worldObj;

        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.doRandomChunkLightingUpdates(chunk);
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
