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

package com.falsepattern.lumi.api.storage;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.world.LumiWorld;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiBlockStorage {
    @Expose
    @NotNull
    LumiBlockStorageRoot lumi$root();

    @Expose
    @NotNull
    String lumi$blockStorageID();

    @Expose
    @NotNull
    LumiWorld lumi$world();

    @Expose
    default int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return lumi$getBrightness(lumi$getChunkFromBlockPosIfExists(posX, posZ), lightType, posX, posY, posZ);
    }

    @Expose
    int lumi$getBrightness(@Nullable LumiChunk chunk, @NotNull LightType lightType, int posX, int posY, int posZ);

    @Expose
    default int lumi$getBrightness(int posX, int posY, int posZ) {
        return lumi$getBrightness(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    @Expose
    int lumi$getBrightness(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    @Expose
    default int lumi$getLightValue(int posX, int posY, int posZ) {
        return lumi$getLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    @Expose
    int lumi$getLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    @Expose
    default int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
        return lumi$getLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), lightType, posX, posY, posZ);
    }

    @Expose
    int lumi$getLightValue(@Nullable LumiChunk chunk, LightType lightType, int posX, int posY, int posZ);

    @Expose
    default int lumi$getBlockLightValue(int posX, int posY, int posZ) {
        return lumi$getBlockLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    @Expose
    int lumi$getBlockLightValue(LumiChunk chunk, int posX, int posY, int posZ);

    @Expose
    default int lumi$getSkyLightValue(int posX, int posY, int posZ) {
        return lumi$getSkyLightValue(lumi$getChunkFromBlockPosIfExists(posX, posZ), posX, posY, posZ);
    }

    @Expose
    int lumi$getSkyLightValue(@Nullable LumiChunk chunk, int posX, int posY, int posZ);

    @Expose
    int lumi$getBlockBrightness(int posX, int posY, int posZ);

    @Expose
    int lumi$getBlockOpacity(int posX, int posY, int posZ);

    @Expose
    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    @Expose
    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    @Expose
    @Nullable
    LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ);

    @Expose
    @Nullable
    LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ);
}
