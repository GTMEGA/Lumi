/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lumina.api.chunk.LumiChunk;

public interface PhosphorChunk extends LumiChunk {
    /**
     * 2 light types * 4 directions * 2 halves * (inwards + outwards)
     */
    int LIGHT_CHECK_FLAGS_LENGTH = 32;

    short[] phosphor$lightCheckFlags();
}
