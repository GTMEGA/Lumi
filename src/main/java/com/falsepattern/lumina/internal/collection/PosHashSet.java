/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.collection;

import gnu.trove.set.hash.TLongHashSet;
import lombok.val;

public class PosHashSet extends TLongHashSet {
    private static final int HASH_PRIME = 92821;

    private static int ballerHash(long key) {
        val a = (int) key;
        val b = (int) (key >>> 32);
        return a + (b * HASH_PRIME);
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
