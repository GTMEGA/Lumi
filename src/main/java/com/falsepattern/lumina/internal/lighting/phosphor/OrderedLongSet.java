/*
 * Copyright (c) 2023 FalsePattern, Ven
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
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lumina.internal.collection.PosHashSet;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;

public class OrderedLongSet {
    private final PosHashSet theSet;
    private final TLongArrayList theList;
    public OrderedLongSet() {
        theSet = new PosHashSet();
        theList = new TLongArrayList();
    }

    public boolean isEmpty() {
        return theList.isEmpty();
    }

    public void clear() {
        theSet.clear();
        theList.resetQuick();
    }

    public TLongIterator iterator() {
        return theList.iterator();
    }

    public void add(long value) {
        if (!theSet.contains(value)) {
            theSet.add(value);
            theList.add(value);
        }
    }

    public int size() {
        return theList.size();
    }
}
