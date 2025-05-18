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

package com.falsepattern.lumi.api.chunk;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiChunk {
    @Expose
    int SUB_CHUNK_ARRAY_SIZE = 16;
    @Expose
    int HEIGHT_MAP_ARRAY_SIZE = 16 * 16;
    @Expose
    int UPDATE_SKYLIGHT_COLUMNS_ARRAY_SIZE = 16 * 16;
    @Expose
    int MAX_QUEUED_RANDOM_LIGHT_UPDATES = 16 * 16 * 16;

    @Expose
    String IS_LIGHT_INITIALIZED_NBT_TAG_NAME = "lighting_initialized";
    @Expose
    String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME = "sky_light_height_map";
    @Expose
    String LUMI_WORLD_TAG_PREFIX = "lumi_";
    @Expose
    String IS_LIGHT_INITIALIZED_NBT_TAG_NAME_VANILLA = "LightPopulated";
    @Expose
    String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME_VANILLA = "HeightMap";

    @Expose
    @NotNull
    LumiChunkRoot lumi$root();

    @Expose
    @NotNull
    LumiWorld lumi$world();

    @Expose
    @NotNull
    String lumi$chunkID();

    @Expose
    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    @Expose
    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    @Expose
    void lumi$cloneFrom(@NotNull LumiChunk from);

    @Expose
    void lumi$writeToPacket(@NotNull ByteBuffer output);

    @Expose
    void lumi$readFromPacket(@NotNull ByteBuffer input);

    @Nullable
    @Deprecated
    LumiSubChunk lumi$getSubChunkIfPrepared(int chunkPosY);

    @Expose
    @NotNull
    LumiSubChunk lumi$getSubChunk(int chunkPosY);

    @Expose
    int lumi$chunkPosX();

    @Expose
    int lumi$chunkPosZ();

    @Expose
    void lumi$queuedRandomLightUpdates(int queuedRandomLightUpdates);

    @Expose
    int lumi$queuedRandomLightUpdates();

    @Expose
    void lumi$resetQueuedRandomLightUpdates();

    @Expose
    int lumi$getBrightness(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    void lumi$setLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    @Expose
    int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    @Expose
    int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    @Expose
    int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight);

    @Expose
    int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ);

    @Expose
    void lumi$minSkyLightHeight(int minSkyLightHeight);

    @Expose
    int lumi$minSkyLightHeight();

    @Expose
    void lumi$resetSkyLightHeightMap();

    @Expose
    void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isHeightOutdated);

    @Expose
    boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ);

    @Expose
    void lumi$resetOutdatedHeightFlags();

    @Expose
    void lumi$isLightingInitialized(boolean isLightingInitialized);

    @Expose
    boolean lumi$isLightingInitialized();

    @Expose
    void lumi$resetLighting();

    @Expose
    int @NotNull [] lumi$skyLightHeightMap();
}
