/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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
