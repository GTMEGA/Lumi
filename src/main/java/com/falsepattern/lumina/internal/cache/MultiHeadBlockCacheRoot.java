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

package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.cache.LumiBlockCache;
import com.falsepattern.lumina.api.cache.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class MultiHeadBlockCacheRoot implements LumiBlockCacheRoot {
    public static final int MAX_MULTI_HEAD_CACHE_COUNT = 8;
    private final int multiHeadCacheCount;

    private final DynamicBlockCacheRoot fallback;
    private final DynamicBlockCacheRoot[] cacheRoots;
    private final LRU lru = new LRU();

    public MultiHeadBlockCacheRoot(@NotNull LumiWorldRoot worldRoot, int multiHeadCacheCount) {
        multiHeadCacheCount = Math.min(Math.max(multiHeadCacheCount, 2), MAX_MULTI_HEAD_CACHE_COUNT);
        this.multiHeadCacheCount = multiHeadCacheCount;
        cacheRoots = new DynamicBlockCacheRoot[multiHeadCacheCount];
        for (int i = 0; i < multiHeadCacheCount; i++)
            cacheRoots[i] = new DynamicBlockCacheRoot(worldRoot);
        fallback = cacheRoots[0];
    }

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_multihead_dynamic_block_cache_root";
    }

    @Override
    public @NotNull MultiHeadBlockCacheRoot.MultiHeadBlockCache lumi$createBlockCache(LumiWorld world) {
        return new MultiHeadBlockCache(world);
    }

    @Override
    public void lumi$clearCache() {
        for (var i = 0; i < multiHeadCacheCount; i++)
            cacheRoots[i].lumi$clearCache();
    }

    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_multihead_dynamic_block_cache";
    }

    @Override
    public boolean lumi$isClientSide() {
        return fallback.lumi$isClientSide();
    }

    @Override
    public boolean lumi$hasSky() {
        return fallback.lumi$hasSky();
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        val bestCache = getCacheRoot(posX >> 4, posZ >> 4);
        return bestCache.lumi$getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        val bestCache = getCacheRoot(posX >> 4, posZ >> 4);
        return bestCache.lumi$getBlockMeta(posX, posY, posZ);
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        val bestCache = getCacheRoot(posX >> 4, posZ >> 4);
        return bestCache.lumi$isAirBlock(posX, posY, posZ);
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return fallback.lumi$getTileEntity(posX, posY, posZ);
    }

    private boolean inRange(DynamicBlockCacheRoot cache, int chunkPosX, int chunkPosZ) {
        return cache.lumi$minChunkPosX() <= chunkPosX &&
               cache.lumi$maxChunkPosX() >= chunkPosX &&
               cache.lumi$minChunkPosZ() <= chunkPosZ &&
               cache.lumi$maxChunkPosZ() >= chunkPosZ;
    }

    private LumiBlockCacheRoot getCacheRoot(int chunkPosX, int chunkPosZ) {
        for (int i = 0; i < multiHeadCacheCount; i++) {
            if (inRange(cacheRoots[i], chunkPosX, chunkPosZ)) {
                lru.update(i);
                return cacheRoots[i];
            }
        }
        int last = lru.last();
        return cacheRoots[last];
    }

    public final class MultiHeadBlockCache implements LumiBlockCache {
        private final DynamicBlockCacheRoot.DynamicBlockCache fallback;
        private final DynamicBlockCacheRoot.DynamicBlockCache[] caches = new DynamicBlockCacheRoot.DynamicBlockCache[multiHeadCacheCount];

        public MultiHeadBlockCache(@NotNull LumiWorld world) {
            for (var i = 0; i < multiHeadCacheCount; i++)
                caches[i] = cacheRoots[i].lumi$createBlockCache(world);
            fallback = caches[0];
        }

        @Override
        public @NotNull MultiHeadBlockCacheRoot lumi$root() {
            return MultiHeadBlockCacheRoot.this;
        }

        @Override
        public @NotNull String lumi$blockStorageID() {
            return "lumi_multihead_dynamic_block_cache";
        }

        @Override
        public @NotNull LumiWorld lumi$world() {
            return fallback.lumi$world();
        }

        @Override
        public int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return fallback.lumi$getBrightness(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBrightness(int posX, int posY, int posZ) {
            return fallback.lumi$getBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(int posX, int posY, int posZ) {
            return fallback.lumi$getLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return fallback.lumi$getLightValue(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
            return fallback.lumi$getBlockLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
            return fallback.lumi$getSkyLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
            return getCache(posX >> 4, posZ >> 4).lumi$getBlockBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
            return getCache(posX >> 4, posZ >> 4).lumi$getBlockOpacity(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return getCache(posX >> 4, posZ >> 4).lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return getCache(posX >> 4, posZ >> 4).lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
        }

        @Override
        public @NotNull String lumi$BlockCacheID() {
            return "lumi_multihead_dynamic_block_cache";
        }

        @Override
        public void lumi$clearCache() {
            for (var i = 0; i < multiHeadCacheCount; i++)
                caches[i].lumi$clearCache();
        }

        private boolean inRange(DynamicBlockCacheRoot.DynamicBlockCache cache, int chunkPosX, int chunkPosZ) {
            val cacheRoot = cache.lumi$root();
            return cacheRoot.lumi$minChunkPosX() <= chunkPosX &&
                   cacheRoot.lumi$maxChunkPosX() > chunkPosX &&
                   cacheRoot.lumi$minChunkPosZ() <= chunkPosZ &&
                   cacheRoot.lumi$maxChunkPosZ() > chunkPosZ;
        }

        private LumiBlockCache getCache(int chunkPosX, int chunkPosZ) {
            for (var i = 0; i < multiHeadCacheCount; i++) {
                if (inRange(caches[i], chunkPosX, chunkPosZ)) {
                    lru.update(i);
                    return caches[i];
                }
            }
            return caches[lru.last()];
        }
    }

    private class LRU {
        private final int[] keys;

        private LRU() {
            this.keys = new int[multiHeadCacheCount];
            for (int i = 0; i < multiHeadCacheCount; i++) {
                keys[i] = i;
            }
        }

        public synchronized void update(int value) {
            for (var i = 0; i < multiHeadCacheCount; i++) {
                if (keys[i] != value)
                    continue;
                if (i != 0) {
                    shiftRight(i);
                    keys[0] = value;
                }
                return;
            }
            throw new IllegalStateException("Illegal state in LRU cache. Keys: " + Arrays.toString(keys) + ", value: " + value + ".");
        }

        public synchronized int last() {
            val last = keys[multiHeadCacheCount - 1];
            shiftRight(multiHeadCacheCount - 1);
            keys[0] = last;
            return last;
        }

        private void shiftRight(int start) {
            for (var i = start; i > 0; i--)
                keys[i] = keys[i - 1];
        }
    }
}
