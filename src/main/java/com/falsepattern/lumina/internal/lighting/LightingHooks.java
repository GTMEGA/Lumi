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

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.internal.lighting.phosphor.LightingHooksOld;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.world.ChunkCoordIntPair;
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

    public static int getMaxCurrentLightValue(World baseWorld,
                                              Chunk baseChunk,
                                              EnumSkyBlock baseLightType,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        val posX = (baseChunk.xPosition << 4) + subChunkPosX;
        val posZ = (baseChunk.zPosition << 4) + subChunkPosZ;
        val lightType = LightType.of(baseLightType);

        var maxLightValue = 0;
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightingUpdatesForType(lightType);
            val lightValue = lightingEngine.getCurrentLightValue(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static void handleChunkInit(World baseWorld, Chunk baseChunk) {
        resetPrecipitationHeightMap(baseChunk);
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleChunkInit(chunk);
        }
    }

    @SideOnly(CLIENT)
    public static void handleClientChunkInit(World baseWorld, Chunk baseChunk) {
        resetPrecipitationHeightMap(baseChunk);
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleClientChunkInit(chunk);
        }
    }

    public static void handleSubChunkInit(World baseWorld, Chunk baseChunk, ExtendedBlockStorage baseSubChunk) {
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = world.lumi$wrap(baseSubChunk);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleSubChunkInit(World baseWorld, Chunk baseChunk, int chunkPosY) {
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = chunk.lumi$getSubChunk(chunkPosY);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleChunkLoad(World baseWorld, Chunk baseChunk) {
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            lightingEngine.handleChunkLoad(chunk);
        }
    }

    public static void updateLightingForBlock(World baseWorld,
                                              Chunk baseChunk,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
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

    public static int getBrightnessAndLightValueMax(Chunk baseChunk,
                                                    EnumSkyBlock baseLightType,
                                                    int subChunkPosX,
                                                    int posY,
                                                    int subChunkPosZ) {
        val chunk = (LumiChunk) baseChunk;
        val lightType = LightType.of(baseLightType);
        return chunk.lumi$getBrightnessAndLightValueMax(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    public static boolean doesChunkHaveLighting(World baseWorld, Chunk baseChunk) {
        var chunkHasLighting = true;
        for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
            val chunk = world.lumi$wrap(baseChunk);
            chunkHasLighting &= LightingHooksOld.checkChunkLighting(world, chunk);
        }
        return chunkHasLighting;
    }

    // TODO: Make Lighting Engine handle this [3]
    @Deprecated
    public static void randomLightUpdates(World baseWorld, Chunk baseChunk) {
        if (baseChunk.queuedLightChecks >= (16 * 16 * 16))
            return;

        val chunkPosX = baseChunk.xPosition;
        val chunkPosZ = baseChunk.zPosition;
        val chunkPos = new ChunkCoordIntPair(chunkPosX, chunkPosZ);

        val isActiveChunk = baseWorld.activeChunkSet.contains(chunkPos);
        final int maxUpdateIterations;
        if (baseWorld.isRemote && isActiveChunk) {
            maxUpdateIterations = 256;
        } else if (baseWorld.isRemote) {
            maxUpdateIterations = 64;
        } else {
            maxUpdateIterations = 32;
        }

        val minPosX = chunkPosX << 4;
        val minPosZ = chunkPosZ << 4;

        var remainingIterations = maxUpdateIterations;
        while (remainingIterations > 0) {
            if (baseChunk.queuedLightChecks >= (16 * 16 * 16))
                return;
            remainingIterations--;

            val chunkPosY = baseChunk.queuedLightChecks % 16;
            val subChunkPosX = (baseChunk.queuedLightChecks / 16) % 16;
            val subChunkPosZ = baseChunk.queuedLightChecks / (16 * 16);
            baseChunk.queuedLightChecks++;

            val minPosY = chunkPosY << 4;

            val posX = minPosX + subChunkPosX;
            val posZ = minPosZ + subChunkPosZ;

            for (val world : lumiWorldsFromBaseWorld(baseWorld)) {
                val chunk = world.lumi$wrap(baseChunk);
                if (!chunk.lumi$root().lumi$isSubChunkPrepared(chunkPosY))
                    continue;

                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    val posY = minPosY + subChunkPosY;

                    notCornerCheck:
                    {
                        if (subChunkPosX != 0 && subChunkPosX != 15)
                            break notCornerCheck;
                        if (subChunkPosY != 0 && subChunkPosY != 15)
                            break notCornerCheck;
                        if (subChunkPosZ != 0 && subChunkPosZ != 15)
                            break notCornerCheck;

                        // Perform a full lighting update
                        baseWorld.func_147451_t(posX, posY, posZ);
                        continue;
                    }

                    renderUpdateCheck:
                    {
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                        if (blockOpacity < 15)
                            break renderUpdateCheck;

                        val blockBrightness = chunk.lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
                        if (blockBrightness > 0)
                            break renderUpdateCheck;

                        val lightValue = chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
                        if (lightValue == 0)
                            continue;

                        chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, 0);
                        baseWorld.markBlockRangeForRenderUpdate(posX, posY, posZ, posX, posY, posZ);
                        break;
                    }

                    // Perform a full lighting update
                    baseWorld.func_147451_t(posX, posY, posZ);
                    break;
                }
            }
        }
    }

    public static void markClientChunkLightingInitialized(Chunk baseChunk) {
        for (val world : lumiWorldsFromBaseWorld(baseChunk.worldObj)) {
            val chunk = world.lumi$wrap(baseChunk);
            chunk.lumi$isLightingInitialized(true);
        }
    }

    private static void resetPrecipitationHeightMap(Chunk baseChunk) {
        Arrays.fill(baseChunk.precipitationHeightMap, DEFAULT_PRECIPITATION_HEIGHT);
    }
}
