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

package com.falsepattern.lumina.api.world;

import com.falsepattern.lumina.api.cache.LumiBlockCache;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.storage.LumiBlockStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiWorld extends LumiBlockStorage {
    @NotNull LumiWorldRoot lumi$root();

    @NotNull String lumi$worldID();

    @NotNull LumiChunk lumi$wrap(@NotNull Chunk chunkBase);

    @NotNull LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage subChunkBase);

    @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ);

    @NotNull LumiLightingEngine lumi$lightingEngine();

    void lumi$setLightValue(@NotNull LightType lightType, int posX, int posY, int posZ, int lightValue);

    void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue);

    void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue);

    @NotNull LumiBlockCache lumi$blockCache();
}
