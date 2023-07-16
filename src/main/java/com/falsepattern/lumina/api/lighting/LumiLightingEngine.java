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

package com.falsepattern.lumina.api.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import static cpw.mods.fml.relauncher.Side.CLIENT;

@SuppressWarnings("unused")
public interface LumiLightingEngine {
    @NotNull String lumi$engineID();

    int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    void handleChunkInit(@NotNull LumiChunk chunk);

    @SideOnly(CLIENT)
    void handleClientChunkInit(@NotNull LumiChunk chunk);

    void handleSubChunkInit(@NotNull LumiChunk chunk,
                            @NotNull LumiSubChunk subChunk);

    void handleChunkLoad(@NotNull LumiChunk chunk);

    void updateLightingForBlock(@NotNull BlockPos blockPos);

    void updateLightingForBlock(int posX, int posY, int posZ);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        @NotNull BlockPos startBlockPos,
                                        @NotNull BlockPos endBlockPos);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        int startPosX,
                                        int startPosY,
                                        int startPosZ,
                                        int endPosX,
                                        int endPosY,
                                        int endPosZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int startPosY, int endPosY);

    void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ);

    void processLightingUpdatesForType(@NotNull LightType lightType);

    void processLightingUpdatesForAllTypes();
}
