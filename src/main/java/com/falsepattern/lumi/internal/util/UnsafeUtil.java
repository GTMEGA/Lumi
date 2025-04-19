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
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

@UtilityClass
public final class UnsafeUtil {
    private static final Unsafe UNSAFE;

    private static final long BYTE_ARRAY_BASE_OFFSET;
    private static final long LONG_ARRAY_BASE_OFFSET;

    // region Setup Unsafe
    static {
        java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

        /*
        Different runtimes use different names for the Unsafe singleton,
        so we cannot use .getDeclaredField and we scan instead. For example:

        Oracle: theUnsafe
        PERC : m_unsafe_instance
        Android: THE_ONE
        */
        Unsafe unsafe = null;
        for (java.lang.reflect.Field field : fields) {
            if (!field.getType().equals(sun.misc.Unsafe.class)) {
                continue;
            }

            int modifiers = field.getModifiers();
            if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
                continue;
            }

            try {
                field.setAccessible(true);
                unsafe = (sun.misc.Unsafe) field.get(null);
                break;
            } catch (Exception ignored) {
            }
            break;
        }

        if (unsafe == null)
            throw new UnsupportedOperationException();

        UNSAFE = unsafe;
    }
    // endregion

    static {
        BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        LONG_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
    }

    public static void clearArray(byte @NotNull [] arr) {
        UNSAFE.setMemory(arr, BYTE_ARRAY_BASE_OFFSET, arr.length, (byte) 0);
    }

    public static void clearArray(long @NotNull [] arr) {
        UNSAFE.setMemory(arr, LONG_ARRAY_BASE_OFFSET, arr.length * Long.BYTES, (byte) 0);
    }
}
