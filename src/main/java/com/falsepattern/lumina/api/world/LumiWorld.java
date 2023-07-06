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

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiEBS;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.engine.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public interface LumiWorld extends LumiLightingEngineProvider {
    LumiChunk lumiWrap(Chunk chunk);

    LumiEBS lumiWrap(ExtendedBlockStorage ebs);

    void setLightingEngine(LumiLightingEngine engine);

    int lumiGetLightValue(final Block block, final int meta, final int x, final int y, final int z);

    int lumiGetLightOpacity(final Block block, final int meta, final int x, final int y, final int z);

    String lumiId();

    //Proxy this to carrier
    LumiWorldRoot root();
}
