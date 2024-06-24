/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.api.init;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.LumiChunkAPI;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lib.StableAPI.Expose;
import static com.falsepattern.lib.StableAPI.Internal;

/**
 * DO NOT IMPLEMENT.
 * <p>
 * This is implemented on {@link Chunk} with a mixin.
 *
 * @see LumiChunkAPI#scheduleChunkLightingEngineInit(LumiChunk)
 */
@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiChunkInitTaskQueue {
    @Expose
    void lumi$addInitTask(@NotNull Runnable task);

    @Internal
    void lumi$executeInitTasks();
}
