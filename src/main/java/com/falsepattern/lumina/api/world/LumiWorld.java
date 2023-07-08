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

package com.falsepattern.lumina.api.world;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;

public interface LumiWorld {
    String luminaWorldID();

    LumiWorldRoot rootWorld();

    LumiChunk toLumiChunk(Chunk vanillaChunk);

    LumiSubChunk toLumiSubChunk(ExtendedBlockStorage vanillaSubChunk);

    @Nullable LumiChunk getLumiChunkFromBlockPos(int posX, int posZ);

    @Nullable LumiChunk getLumiChunkFromChunkPos(int chunkPosX, int chunkPosZ);

    void lightingEngine(LumiLightingEngine lightingEngine);

    LumiLightingEngine lightingEngine();

    int getBrightnessAndBlockLightValueMax(int posX, int posY, int posZ);

    int getBlockSkyAndLightValueMax(int posX, int posY, int posZ);

    void lumi$setLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ, int lightValue);

    int getLightValue(EnumSkyBlock lightType, int posX, int posY, int posZ);

    void setBlockLightValue(int posX, int posY, int posZ, int lightValue);

    int getBlockLightValue(int posX, int posY, int posZ);

    void setSkyLightValue(int posX, int posY, int posZ, int lightValue);

    int getSkyLightValue(int posX, int posY, int posZ);

    int getBlockBrightness(int posX, int posY, int posZ);

    int getBlockOpacity(int posX, int posY, int posZ);

    int getBlockBrightness(Block block, int blockMeta, int posX, int posY, int posZ);

    int getBlockOpacity(Block block, int blockMeta, int posX, int posY, int posZ);
}
