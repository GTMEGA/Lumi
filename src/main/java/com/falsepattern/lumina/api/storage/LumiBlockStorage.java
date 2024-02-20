/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.storage;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LumiBlockStorage {
    @NotNull LumiBlockStorageRoot lumi$root();

    @NotNull String lumi$blockStorageID();

    @NotNull LumiWorld lumi$world();

    default int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return lumi$getBrightness(lumi$getChunkFromBlockPosIfExists(posX, posZ), lightType, posX, posY, posZ);
    }

    int lumi$getBrightness(@Nullable LumiChunk chunk, @NotNull LightType lightType, int posX, int posY, int posZ);

    default int lumi$getBrightness(int posX, int posY, int posZ) {
        return lumi$getBrightness(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    int lumi$getBrightness(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    default int lumi$getLightValue(int posX, int posY, int posZ) {
        return lumi$getLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    int lumi$getLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    default int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return lumi$getLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), lightType, posX, posY, posZ);
    }

    int lumi$getLightValue(@Nullable LumiChunk chunk, LightType lightType, int posX, int posY, int posZ);

    default int lumi$getBlockLightValue(int posX, int posY, int posZ) {
        return lumi$getBlockLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    int lumi$getBlockLightValue(LumiChunk chunk, int posX, int posY, int posZ);

    default int lumi$getSkyLightValue(int posX, int posY, int posZ) {
        return lumi$getSkyLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    int lumi$getSkyLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    int lumi$getBlockBrightness(int posX, int posY, int posZ);

    int lumi$getBlockOpacity(int posX, int posY, int posZ);

    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ);
}
