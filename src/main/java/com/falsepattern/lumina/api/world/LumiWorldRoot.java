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

package com.falsepattern.lumina.api.world;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.IChunkProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface LumiWorldRoot {
    @NotNull String lumi$worldRootID();

    boolean lumi$isClientSide();

    boolean lumi$hasSky();

    void lumi$markBlockForRenderUpdate(int posX, int posY, int posZ);

    void lumi$scheduleLightingUpdate(int posX, int posY, int posZ);

    @NotNull Block lumi$getBlock(int posX, int posY, int posZ);

    int lumi$getBlockMeta(int posX, int posY, int posZ);

    @NotNull IChunkProvider lumi$chunkProvider();

    boolean lumi$doChunksExistInRange(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    boolean lumi$doChunksExistInRange(int centerPosX, int centerPosY, int centerPosZ, int blockRange);
}
