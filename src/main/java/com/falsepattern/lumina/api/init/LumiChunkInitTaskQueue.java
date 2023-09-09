/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
