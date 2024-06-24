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

package com.falsepattern.lumi.internal.lighting.phosphor;

import com.falsepattern.lumi.api.chunk.LumiChunk;

public interface PhosphorChunk extends LumiChunk {
    /**
     * 2 light types * 4 directions * 2 halves * (inwards + outwards)
     */
    int LIGHT_CHECK_FLAGS_LENGTH = 32;

    short[] phosphor$lightCheckFlags();
}
