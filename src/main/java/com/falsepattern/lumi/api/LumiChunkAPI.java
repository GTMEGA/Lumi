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

package com.falsepattern.lumi.api;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.init.LumiChunkInitTaskQueue;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.falsepattern.lumi.api.chunk.LumiChunk.HEIGHT_MAP_ARRAY_SIZE;
import static com.falsepattern.lumi.api.chunk.LumiChunk.UPDATE_SKYLIGHT_COLUMNS_ARRAY_SIZE;

@StableAPI(since = "__EXPERIMENTAL__")
public final class LumiChunkAPI {
    private static final int[] INITIAL_HEIGHT_MAP_ARRAY = new int[HEIGHT_MAP_ARRAY_SIZE];
    private static final boolean[] INITIAL_UPDATE_SKYLIGHT_COLUMNS_ARRAY = new boolean[UPDATE_SKYLIGHT_COLUMNS_ARRAY_SIZE];

    static {
        Arrays.fill(INITIAL_HEIGHT_MAP_ARRAY, Integer.MAX_VALUE);
        Arrays.fill(INITIAL_UPDATE_SKYLIGHT_COLUMNS_ARRAY, true);
    }

    private LumiChunkAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Use this in places where you would need to call {@link LumiLightingEngine#handleChunkInit} otherwise.
     * NOTE: <b>ONLY USE WHEN LOADING CHUNK FROM NBT!</b>
     */
    @StableAPI.Expose
    public static void scheduleChunkLightingEngineInit(@NotNull LumiChunk chunk) {
        scheduleChunkInitTask(chunk, () -> chunk.lumi$world().lumi$lightingEngine().handleChunkInit(chunk));
    }

    @StableAPI.Expose
    @SuppressWarnings("CastToIncompatibleInterface")
    public static void scheduleChunkInitTask(@NotNull LumiChunk chunk, @NotNull Runnable task) {
        final LumiChunkInitTaskQueue chunkInitTaskQueue = (LumiChunkInitTaskQueue) chunk.lumi$root();
        chunkInitTaskQueue.lumi$addInitTask(task);
    }

    @StableAPI.Expose
    public static void resetHeightMapArray(int @NotNull [] heightMap) {
        System.arraycopy(INITIAL_HEIGHT_MAP_ARRAY, 0, heightMap, 0, HEIGHT_MAP_ARRAY_SIZE);
    }

    @StableAPI.Expose
    public static void resetUpdateSkylightColumns(boolean @NotNull [] updateSkylightColumns) {
        System.arraycopy(INITIAL_UPDATE_SKYLIGHT_COLUMNS_ARRAY,
                         0,
                         updateSkylightColumns,
                         0,
                         UPDATE_SKYLIGHT_COLUMNS_ARRAY_SIZE);
    }
}
