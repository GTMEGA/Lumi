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
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

@UtilityClass
public final class LightingHooks {
    private static final int DEFAULT_PRECIPITATION_HEIGHT = -999;

    @Deprecated
    public static void initChunkSkyLight(World baseWorld, Chunk baseChunk) {
        resetPrecipitationHeightMap(baseChunk);
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.initChunkSkyLight(chunk);
        }
    }

    @Deprecated
    public static void initClientChunkSkyLight(World baseWorld, Chunk baseChunk) {
        resetPrecipitationHeightMap(baseChunk);
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.initClientChunkSkyLight(chunk);
        }
    }

    public static int getMaxLightValue(World baseWorld,
                                       Chunk baseChunk,
                                       EnumSkyBlock baseLightType,
                                       int subChunkPosX,
                                       int posY,
                                       int subChunkPosZ) {
        val posX = (baseChunk.xPosition << 4) + subChunkPosX;
        val posZ = (baseChunk.zPosition << 4) + subChunkPosZ;
        val lightType = LightType.of(baseLightType);

        var maxLightValue = 0;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightUpdatesForType(lightType);
            val lightValue = lightingEngine.getCurrentLightValue(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    @Deprecated
    public static void initSkyLightForSubChunk(World baseWorld, Chunk baseChunk, ExtendedBlockStorage baseSubChunk) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = world.lumi$wrap(baseSubChunk);
            LightingHooksOld.initSkyLightForSubChunk(world, chunk, subChunk);
        }
    }

    @Deprecated
    public static void initSkyLightForSubChunk(World baseWorld, Chunk baseChunk, int chunkPosY) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            LightingHooksOld.initSkyLightForSubChunk(world, chunk, subChunk);
        }
    }

    public static void scheduleLightUpdates(World baseWorld,
                                            EnumSkyBlock baseLightType,
                                            int posX,
                                            int posY,
                                            int posZ) {
        val lightType = LightType.of(baseLightType);
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
        }
    }

    public static void processLightUpdates(World baseWorld) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightUpdatesForAllTypes();
        }
    }

    @Deprecated
    public static void updateSkyLightForBlock(World baseWorld,
                                              Chunk baseChunk,
                                              int subChunkPosX,
                                              int basePosY,
                                              int subChunkPosZ) {
        val posY = basePosY + 1;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            if (chunk.lumi$canBlockSeeSky(subChunkPosX, posY, subChunkPosZ))
                LightingHooksOld.updateSkyLightForBlock(chunk, subChunkPosX, posY, subChunkPosZ);
        }
    }

    @Deprecated
    public static void scheduleRelightChecksForChunkBoundaries(World baseWorld, Chunk baseChunk) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.scheduleRelightChecksForChunkBoundaries(world, chunk);
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
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            chunkHasLighting &= LightingHooksOld.checkChunkLighting(world, chunk);
        }
        return chunkHasLighting;
    }

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

        val worldCount = LumiWorldManager.lumiWorldCount();

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

            for (var i = 0; i < worldCount; i++) {
                val world = LumiWorldManager.getWorld(baseWorld, i);
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

    private static void resetPrecipitationHeightMap(Chunk baseChunk) {
        Arrays.fill(baseChunk.precipitationHeightMap, DEFAULT_PRECIPITATION_HEIGHT);
    }
}
