/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
