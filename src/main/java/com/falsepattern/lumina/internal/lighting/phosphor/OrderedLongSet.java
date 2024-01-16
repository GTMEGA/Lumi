/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;
import lombok.val;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public final class OrderedLongSet {
    //    private final PosHashSet theSet;
    private final TLongArrayList theList;

    private final PersistentIterator persistentIterator;

    public OrderedLongSet(int initialSize) {
//        theSet = new PosHashSet(initialSize, 0.95F);
        theList = new TLongArrayList(initialSize);
        persistentIterator = new PersistentIterator();
    }

    public OrderedLongSet() {
//        theSet = new PosHashSet();
        theList = new TLongArrayList();
        persistentIterator = new PersistentIterator();
    }

    public boolean isEmpty() {
        return theList.isEmpty();
    }

    public void reset() {
//        theSet.resetQuick();
        theList.resetQuick();
        persistentIterator.reset();
    }

    public TLongIterator persistentIterator() {
        return theList.iterator();
    }

    public void add(long value) {
//        if (theSet.add(value))
            theList.add(value);
    }

    public int size() {
        return theList.size();
    }

    private final class PersistentIterator implements TLongIterator {
        private int cursor = 0;
        private int lastRet = -1;

        private void reset() {
            cursor = 0;
            lastRet = -1;
        }

        public boolean hasNext() {
            return cursor < size();
        }

        public long next() {
            try {
                val next = theList.get(cursor);
                cursor++;
                lastRet = cursor;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();

            try {
                theList.remove(lastRet, 1);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
