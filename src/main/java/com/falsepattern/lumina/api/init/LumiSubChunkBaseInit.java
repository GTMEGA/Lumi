/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.init;

import static com.falsepattern.lumina.api.LumiAPI.LUMI_MOD_NAME;

@SuppressWarnings("unused")
public interface LumiSubChunkBaseInit {
    String LUMI_SUB_CHUNK_BASE_INIT_MIXIN_VALUE = "Implemented by [" + LUMI_MOD_NAME + "] with the interface " +
                                                  "[com.falsepattern.lumina.api.init.LumiSubChunkBaseInit]";
    String LUMI_SUB_CHUNK_BASE_INIT_METHOD_REFERENCE = "lumi$subChunkBaseInit()V";

    void lumi$subChunkBaseInit();
}
