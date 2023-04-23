/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.api;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public interface ILumiWorld extends ILightingEngineProvider {
    ILumiChunk wrap(Chunk chunk);

    ILumiEBS wrap(ExtendedBlockStorage ebs);

    void setLightingEngine(ILightingEngine engine);

    int getLightValue(final Block block, final int meta, final int x, final int y, final int z);

    int getLightOpacity(final Block block, final int meta, final int x, final int y, final int z);

    String id();

    //Proxy this to carrier
    ILumiWorldRoot root();
}
