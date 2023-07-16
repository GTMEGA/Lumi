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

import com.falsepattern.lumina.api.lighting.LightType;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface LumiSubChunk {
    String BLOCK_LIGHT_NBT_TAG_NAME = "block_light";
    String SKY_LIGHT_NBT_TAG_NAME = "sky_light";

    @NotNull LumiSubChunkRoot lumi$root();

    @NotNull String lumi$subChunkID();

    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    void lumi$setLightValue(@NotNull LightType lightType, int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    void lumi$setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);
}
