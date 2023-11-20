/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.init;

import com.falsepattern.lumina.api.LumiChunkAPI;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import net.minecraft.world.chunk.Chunk;

/**
 * DO NOT IMPLEMENT.
 * <p>
 * This is implemented on {@link Chunk} with a mixin.
 *
 * @see LumiChunkAPI#scheduleChunkLightingEngineInit(LumiChunk)
 */
public interface LumiChunkInitTaskQueue {
    void lumi$addInitTask(Runnable task);

    void lumi$executeInitTasks();
}
