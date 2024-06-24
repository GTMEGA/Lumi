/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.api.chunk;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.lighting.LightType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.NibbleArray;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiSubChunk {
    @Expose
    String BLOCK_LIGHT_NBT_TAG_NAME = "block_light";
    @Expose
    String SKY_LIGHT_NBT_TAG_NAME = "sky_light";
    @Expose
    String BLOCK_LIGHT_NBT_TAG_NAME_VANILLA = "BlockLight";
    @Expose
    String SKY_LIGHT_NBT_TAG_NAME_VANILLA = "SkyLight";

    @Expose
    @NotNull
    LumiSubChunkRoot lumi$root();

    @Expose
    @NotNull
    String lumi$subChunkID();

    @Expose
    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    @Expose
    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    @Expose
    void lumi$cloneFrom(LumiSubChunk from);

    @Expose
    void lumi$writeToPacket(@NotNull ByteBuffer output);

    @Expose
    void lumi$readFromPacket(@NotNull ByteBuffer input);

    @Expose
    void lumi$setLightValue(@NotNull LightType lightType,
                            int subChunkPosX,
                            int subChunkPosY,
                            int subChunkPosZ,
                            int lightValue);

    @Expose
    int lumi$getLightValue(@NotNull LightType lightType,
                           int subChunkPosX,
                           int subChunkPosY,
                           int subChunkPosZ);

    @Expose
    void lumi$setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    @Expose
    int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Expose
    void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    @Expose
    int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Expose
    NibbleArray lumi$getBlockLightArray();

    @Expose
    NibbleArray lumi$getSkyLightArray();
}
