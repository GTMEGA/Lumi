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

/**
 * Chunk placeholder for implementation.
 */
public interface ILumiChunk extends ILightingEngineProvider {
    //Implement these
    ILumiEBS lumiEBS(int arrayIndex);
    ILumiWorld lumiWorld();

    int[] lumiHeightMap();

    short[] lumiGetNeighborLightChecks();
    void lumiGetNeighborLightChecks(short[] data);

    boolean lumiIsLightInitialized();
    void lumiIsLightInitialized(boolean val);

    boolean[] lumiUpdateSkylightColumns();

    int lumiHeightMapMinimum();
    void lumiHeightMapMinimum(int min);

    //Proxy this to carrier
    ILumiChunkRoot root();

    //Keeping this here for now
    int x();
    int z();
}
