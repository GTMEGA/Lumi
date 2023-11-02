/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.init;

import static com.falsepattern.lumina.api.LumiAPI.LUMI_MOD_NAME;

@SuppressWarnings("unused")
public interface LumiChunkInitHook {
    String LUMI_CHUNK_INIT_HOOK_INFO = "Implemented by [" + LUMI_MOD_NAME + "] with the interface " +
                                       "[com.falsepattern.lumina.api.init.LumiChunkInitHook]";
    String LUMI_CHUNK_INIT_HOOK_METHOD = "lumi$onChunkInit()V";

    void lumi$onChunkInit();
}
