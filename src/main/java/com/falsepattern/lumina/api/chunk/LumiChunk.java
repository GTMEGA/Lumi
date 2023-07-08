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

package com.falsepattern.lumina.api.chunk;

import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import org.jetbrains.annotations.Nullable;

public interface LumiChunk {
    LumiChunkRoot rootChunk();

    LumiWorld lumiWorld();

    @Nullable LumiSubChunk subChunk(int chunkPosY);

    LumiLightingEngine lightingEngine();

    int chunkPosX();

    int chunkPosZ();

    int getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    int getBlockSkyAndLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ);

    void setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    void setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    int getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    int getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ);

    int getBlockBrightness(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    int getBlockOpacity(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    boolean isBlockOnTop(int subChunkPosX, int posY, int subChunkPosZ);

    // TODO: Add skyLightHeight setter/getter
    int[] skyLightHeights();

    void minSkyLightHeight(int minSkyLightPosY);

    int minSkyLightHeight();

    // TODO: Add outdatedSkylightColumn setter/getter
    boolean[] outdatedSkyLightColumns();

    // TODO: This is currently late-initialized, it should be tacked onto the constructor
    @Deprecated
    void neighborLightCheckFlags(short[] neighborLightCheckFlags);

    short @Nullable [] neighborLightCheckFlags();

    void hasLightInitialized(boolean hasLightInitialized);

    boolean hasLightInitialized();
}
