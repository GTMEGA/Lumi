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

package com.falsepattern.lumina.api;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.init.LumiChunkInitTaskQueue;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import lombok.val;

public final class LumiChunkAPI {
    /**
     * Use this in places where you would need to call {@link LumiLightingEngine#handleChunkInit} otherwise.
     * NOTE: <b>ONLY USE WHEN LOADING CHUNK FROM NBT!</b>
     */
    public static void scheduleChunkLightingEngineInit(LumiChunk chunk) {
        scheduleChunkInitTask(chunk, () -> chunk.lumi$world().lumi$lightingEngine().handleChunkInit(chunk));
    }

    public static void scheduleChunkInitTask(LumiChunk chunk, Runnable task) {
        val chunkInitTaskQueue = (LumiChunkInitTaskQueue) chunk.lumi$root();
        chunkInitTaskQueue.lumi$addInitTask(task);
    }
}
