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

package com.falsepattern.lumina.api.chunk;

import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import org.jetbrains.annotations.Nullable;

public interface LumiChunk {
    LumiChunkRoot lumi$root();

    LumiWorld lumi$world();

    @Nullable LumiSubChunk lumi$subChunk(int chunkPosY);

    int lumi$chunkPosX();

    int lumi$chunkPosZ();

    int lumi$getBrightnessAndLightValueMax(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight);

    int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ);

    void lumi$minSkyLightHeight(int minSkyLightPosY);

    int lumi$minSkyLightHeight();

    void lumi$resetSkyLightHeightMap();

    void lumi$precipitationHeight(int subChunkPosX, int subChunkPosZ, int precipitationHeight);

    int lumi$precipitationHeight(int subChunkPosX, int subChunkPosZ);

    void lumi$resetPrecipitationHeightMap();

    void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isOutdated);

    boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ);

    void lumi$resetOutdatedHeightFlags();

    short[] lumi$neighborLightCheckFlags();

    void lumi$lightingInitialized(boolean lightingInitialized);

    boolean lumi$lightingInitialized();
}
