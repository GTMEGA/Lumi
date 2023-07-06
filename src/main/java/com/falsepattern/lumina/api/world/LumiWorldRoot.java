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

package com.falsepattern.lumina.api.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.chunk.IChunkProvider;

public interface LumiWorldRoot {
    Profiler rootTheProfiler();
    boolean rootIsRemote();
    //.provider.hasNoSky
    boolean rootHasNoSky();
    void rootMarkBlockForRenderUpdate(int x, int y, int z);
    IChunkProvider rootProvider();
    boolean rootCheckChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    boolean rootDoChunksNearChunkExist(int x, int y, int z, int dist);
}
