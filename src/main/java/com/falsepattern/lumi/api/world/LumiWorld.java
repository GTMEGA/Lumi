/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.api.world;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.api.storage.LumiBlockStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiWorld extends LumiBlockStorage {
    @Expose
    @NotNull
    LumiWorldRoot lumi$root();

    @Expose
    @NotNull
    String lumi$worldID();

    @Expose
    @NotNull
    LumiChunk lumi$wrap(@NotNull Chunk chunkBase);

    @Expose
    @NotNull
    LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage subChunkBase);

    @Expose
    @NotNull
    LumiLightingEngine lumi$lightingEngine();

    @Expose
    default void lumi$setLightValue(@NotNull LightType lightType, int posX, int posY, int posZ, int lightValue) {
        lumi$setLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), lightType, posX, posY, posZ, lightValue);
    }

    @Expose
    void lumi$setLightValue(@Nullable LumiChunk chunk,
                            @NotNull LightType lightType,
                            int posX,
                            int posY,
                            int posZ,
                            int lightValue);

    @Expose
    default void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue) {
        lumi$setBlockLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ, lightValue);
    }

    @Expose
    void lumi$setBlockLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ, int lightValue);

    @Expose
    default void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue) {
        lumi$setSkyLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ, lightValue);
    }

    @Expose
    void lumi$setSkyLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ, int lightValue);
}
