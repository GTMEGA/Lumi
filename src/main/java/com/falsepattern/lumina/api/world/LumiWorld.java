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

package com.falsepattern.lumina.api.world;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiWorld {
    @NotNull LumiWorldRoot lumi$root();

    @NotNull String lumi$worldID();

    @NotNull LumiChunk lumi$wrap(@NotNull Chunk chunkBase);

    @NotNull LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage subChunkBase);

    @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ);

    @NotNull LumiLightingEngine lumi$lightingEngine();

    int lumi$getBrightnessAndLightValueMax(@NotNull LightType lightType, int posX, int posY, int posZ);

    int lumi$getBrightnessAndBlockLightValueMax(int posX, int posY, int posZ);

    int lumi$getLightValueMax(int posX, int posY, int posZ);

    void lumi$setLightValue(@NotNull LightType lightType, int posX, int posY, int posZ, int lightValue);

    int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue);

    int lumi$getBlockLightValue(int posX, int posY, int posZ);

    void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue);

    int lumi$getSkyLightValue(int posX, int posY, int posZ);

    int lumi$getBlockBrightness(int posX, int posY, int posZ);

    int lumi$getBlockOpacity(int posX, int posY, int posZ);

    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);
}
