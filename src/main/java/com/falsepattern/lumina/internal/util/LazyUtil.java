package com.falsepattern.lumina.internal.util;

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