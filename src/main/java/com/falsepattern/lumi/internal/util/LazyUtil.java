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

package com.falsepattern.lumi.internal.util;

import lombok.experimental.UtilityClass;

import net.minecraft.world.chunk.NibbleArray;

@UtilityClass
public final class LazyUtil {
    public static NibbleArray ensurePresent(NibbleArray arr) {
        if (arr == null) {
            arr = new NibbleArray(4096, 4);
        }
        return arr;
    }

    public static int lazyGet(NibbleArray arr, int x, int y, int z) {
        if (arr == null) {
            return 0;
        }
        return arr.get(x, y, z);
    }

    public static NibbleArray lazySet(NibbleArray arr, int x, int y, int z, int val) {
        if (arr == null) {
            if (val == 0) {
                return null;
            }
            arr = new NibbleArray(4096, 4);
        }
        arr.set(x, y, z, val);
        return arr;
    }
}
