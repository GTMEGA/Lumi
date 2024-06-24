/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

package com.falsepattern.lumi.internal.collection;

import com.falsepattern.lumi.internal.util.UnsafeUtil;
import gnu.trove.set.hash.TLongHashSet;
import lombok.val;


public final class PosHashSet extends TLongHashSet {
//    private static final int MAX_FAST_CLEAR_LENGTH = 1_500_000;
//
//    private static final long[] EMPTY_SET = new long[MAX_FAST_CLEAR_LENGTH];
//    private static final byte[] EMPTY_STATES = new byte[MAX_FAST_CLEAR_LENGTH];
//
//    static {
//        Arrays.fill(EMPTY_SET, Constants.DEFAULT_LONG_NO_ENTRY_VALUE);
//        Arrays.fill(EMPTY_STATES, FREE);
//    }

    private static final int HASH_PRIME = 92821;

    public PosHashSet() {
    }

    public PosHashSet(int initialCapacity, float load_factor) {
        super(initialCapacity, load_factor);
    }

    private static int ballerHash(long key) {
        val a = (int) key;
        val b = (int) (key >>> 32);
        return a + (b * HASH_PRIME);
    }

    public void resetQuick() {
//        val length = _set.length;
//        if (length > MAX_FAST_CLEAR_LENGTH) {
//            System.out.println("Failed quick reset!: " + length);
//            clear();
//            return;
//        }

        _size = 0;
        _free = _states.length;

        UnsafeUtil.clearArray(_set);
        UnsafeUtil.clearArray(_states);

//        _set = new long[_set.length];
//        _states = new byte[_states.length];

//        System.arraycopy(EMPTY_SET, 0, _set, 0, length);
//        System.arraycopy(EMPTY_STATES, 0, _states, 0, length);
    }

    protected int index(long val) {
        int hash, probe, index, length;

        final byte[] states = _states;
        final long[] set = _set;
        length = states.length;
        hash = ballerHash(val) & 0x7fffffff;
        index = hash % length;
        byte state = states[index];

        if (state == FREE)
            return -1;

        if (state == FULL && set[index] == val)
            return index;

        return indexRehashed(val, index, hash, state);
    }

    int indexRehashed(long key, int index, int hash, byte state) {
        // see Knuth, p. 529
        int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;

        do {
            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];
            //
            if (state == FREE)
                return -1;

            //
            if (key == _set[index] && state != REMOVED)
                return index;
        } while (index != loopIndex);

        return -1;
    }

    protected int insertKey(long val) {
        int hash, index;

        hash = ballerHash(val) & 0x7fffffff;
        index = hash % _states.length;
        byte state = _states[index];

        consumeFreeSlot = false;

        if (state == FREE) {
            consumeFreeSlot = true;
            insertKeyAt(index, val);

            return index;       // empty, all done
        }

        if (state == FULL && _set[index] == val) {
            return -index - 1;   // already stored
        }

        // already FULL or REMOVED, must probe
        return insertKeyRehash(val, index, hash, state);
    }

    int insertKeyRehash(long val, int index, int hash, byte state) {
        // compute the double hash
        final int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;
        int firstRemoved = -1;

        /**
         * Look until FREE slot or we start to loop
         */
        do {
            // Identify first removed slot
            if (state == REMOVED && firstRemoved == -1)
                firstRemoved = index;

            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];

            // A FREE slot stops the search
            if (state == FREE) {
                if (firstRemoved != -1) {
                    insertKeyAt(firstRemoved, val);
                    return firstRemoved;
                } else {
                    consumeFreeSlot = true;
                    insertKeyAt(index, val);
                    return index;
                }
            }

            if (state == FULL && _set[index] == val) {
                return -index - 1;
            }

            // Detect loop
        } while (index != loopIndex);

        // We inspected all reachable slots and did not find a FREE one
        // If we found a REMOVED slot we return the first one found
        if (firstRemoved != -1) {
            insertKeyAt(firstRemoved, val);
            return firstRemoved;
        }

        // Can a resizing strategy be found that resizes the set?
        throw new IllegalStateException("No free or removed slots available. Key set full?!!");
    }

    void insertKeyAt(int index, long val) {
        _set[index] = val;  // insert value
        _states[index] = FULL;
    }
}
