/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

/**
 * Implement own queue with pooled segments to reduce allocation costs and reduce idle memory footprint
 */
@RequiredArgsConstructor(access = PRIVATE)
final class PooledLongQueue {
    /**
     * Maximum number of segments which each pool will keep
     */
    private static final int LONG_SEGMENT_QUEUE_SIZE_LIMIT = 4096;
    /**
     * Size of the {@code long[]} in each segment.
     */
    private static final int LONG_SEGMENT_DATA_SIZE = 1024;

    private final Pool queuePool;

    private @Nullable Segment headSegment = null;
    private @Nullable Segment tailSegment = null;

    private int size = 0;

    /**
     * Stores whether the queue is empty. Updates to this field will be seen by all threads immediately. Writes
     * to volatile fields are generally quite a bit more expensive, so we avoid repeatedly setting this flag to true.
     */
    private volatile boolean empty;

    static Pool createPool() {
        return new Pool();
    }

    /**
     * Not thread-safe! If you must know whether the queue is empty, please use {@link PooledLongQueue#isEmpty()}.
     *
     * @return The number of encoded values present in this queue
     */
    int size() {
        return size;
    }

    /**
     * Thread-safe method to check whether this queue has work to do. Significantly cheaper than acquiring a lock.
     *
     * @return True if the queue is empty, otherwise false
     */
    boolean isEmpty() {
        return empty;
    }

    /**
     * Not thread-safe! Adds a long value into this queue.
     *
     * @param value The long to add
     */
    void add(long value) {
        if (headSegment == null) {
            val segment = queuePool.acquire();
            this.headSegment = segment;
            this.tailSegment = segment;
            this.empty = false;
        } else if (tailSegment.isFull()) {
            val segment = queuePool.acquire();
            tailSegment.nextSegment = segment;
            tailSegment = segment;
        }

        tailSegment.data[tailSegment.index] = value;
        tailSegment.index++;
        size++;
    }

    /**
     * Not thread safe! Creates an iterator over the values in this queue. Values will be returned in a FIFO fashion.
     *
     * @return The iterator
     */
    LongQueueIterator iterator() {
        return new LongQueueIterator(headSegment);
    }

    private void clear() {
        var currentSegment = headSegment;
        while (currentSegment != null) {
            val nextSegment = currentSegment.nextSegment;
            currentSegment.release();
            currentSegment = nextSegment;
        }

        this.headSegment = null;
        this.tailSegment = null;
        this.size = 0;
        this.empty = true;
    }

    @NoArgsConstructor(access = PRIVATE)
    static final class Pool {
        private final Deque<Segment> segments = new ArrayDeque<>();

        PooledLongQueue createQueue() {
            return new PooledLongQueue(this);
        }

        private Segment acquire() {
            if (segments.isEmpty())
                return new Segment(this);
            return segments.pop();
        }

        private void release(Segment segment) {
            if (segments.size() < LONG_SEGMENT_QUEUE_SIZE_LIMIT)
                segments.push(segment);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class Segment {
        private final long[] data = new long[LONG_SEGMENT_DATA_SIZE];
        private final Pool pool;

        private Segment nextSegment;
        private int index = 0;

        private void release() {
            index = 0;
            nextSegment = null;
            pool.release(this);
        }

        private boolean isFull() {
            return index >= LONG_SEGMENT_DATA_SIZE;
        }
    }

    @Accessors(fluent = true, chain = false)
    final class LongQueueIterator {
        private @Nullable Segment currentSegment;
        private long @Nullable [] data;

        private int capacity;
        private int index;

        @Getter(PACKAGE)
        private boolean hasNext;

        private LongQueueIterator(@Nullable Segment currentSegment) {
            if (currentSegment != null) {
                this.currentSegment = currentSegment;
                this.data = currentSegment.data;
                this.capacity = currentSegment.index;
                this.index = 0;
                this.hasNext = true;
            } else {
                this.currentSegment = null;
                this.data = null;
                this.capacity = 0;
                this.index = 0;
                this.hasNext = false;
            }
        }

        long next() {
            if (!hasNext)
                throw new IllegalStateException("Iterator has no more elements");

            val value = data[index];
            index++;

            if (index >= capacity) {
                val nextSegment = currentSegment.nextSegment;
                if (nextSegment != null) {
                    currentSegment = nextSegment;
                    data = nextSegment.data;
                    capacity = nextSegment.index;
                    index = 0;
                } else {
                    clear();

                    currentSegment = null;
                    data = null;
                    capacity = 0;
                    index = 0;
                    hasNext = false;
                }
            }

            return value;
        }
    }
}
