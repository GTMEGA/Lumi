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

import com.falsepattern.lumina.internal.collection.PosHashSet;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;

public class OrderedLongSet {
    private final PosHashSet theSet;
    private final TLongArrayList theList;

    public OrderedLongSet(int initialSize) {
        theSet = new PosHashSet(initialSize, 0.7F);
        theList = new TLongArrayList(initialSize);
    }

    public OrderedLongSet() {
        theSet = new PosHashSet();
        theList = new TLongArrayList();
    }

    public boolean isEmpty() {
        return theList.isEmpty();
    }

    public void clear() {
        theSet.resetQuick();
        theList.resetQuick();
    }

    public TLongIterator iterator() {
        return theList.iterator();
    }

    public void add(long value) {
        if (theSet.add(value))
            theList.add(value);
    }

    public int size() {
        return theList.size();
    }
}
