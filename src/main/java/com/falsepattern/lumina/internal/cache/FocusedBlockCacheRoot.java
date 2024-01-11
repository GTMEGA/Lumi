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

package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FocusedBlockCacheRoot extends ReadThroughBlockCacheRoot {
    @Nullable
    private LumiChunk focusedChunk;
    @Nullable
    private LumiChunkRoot focusedChunkRoot;

    private int focusedMinPosX = 0;
    private int focusedMinPosZ = 0;
    private int focusedMaxPosX = 0;
    private int focusedMaxPosZ = 0;

    public FocusedBlockCacheRoot(LumiWorldRoot worldRoot) {
        super(worldRoot);
    }

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_focused_block_cache_root";
    }

    @Override
    public @NotNull FocusedBlockCache lumi$createBlockCache(LumiWorld world) {
        return new FocusedBlockCache(world);
    }

    @Override
    public void lumi$prefetchChunk(@Nullable LumiChunk chunk) {
        if (chunk != null) {
            this.focusedChunkRoot = chunk.lumi$root();
            this.focusedChunk = chunk;

            val chunkPosX = chunk.lumi$chunkPosX();
            val chunkPosZ = chunk.lumi$chunkPosZ();

            this.focusedMinPosX = chunkPosX << 4;
            this.focusedMinPosZ = chunkPosZ << 4;

            this.focusedMaxPosX = focusedMinPosX + 15;
            this.focusedMaxPosZ = focusedMinPosZ + 15;
        } else {
            this.focusedChunk = null;
            this.focusedChunkRoot = null;
        }
    }

    @Override
    public void lumi$clearCache() {
        focusedChunkRoot = null;
        focusedChunk = null;
    }

    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_readthrough_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return worldRoot.lumi$isClientSide();
    }

    @Override
    public boolean lumi$hasSky() {
        return worldRoot.lumi$hasSky();
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        if (isInFocus(posX, posZ)) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return focusedChunkRoot.lumi$getBlock(subChunkPosX, posY & 255, subChunkPosZ);
        }
        return worldRoot.lumi$getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        if (isInFocus(posX, posZ)) {
            val subChunkPosX = posX & 15;
            val subChunkPosZ = posZ & 15;
            return focusedChunkRoot.lumi$getBlockMeta(subChunkPosX, posY & 255, subChunkPosZ);
        }
        return worldRoot.lumi$getBlockMeta(posX, posY, posZ);
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return worldRoot.lumi$isAirBlock(posX, posY, posZ);
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return worldRoot.lumi$getTileEntity(posX, posY, posZ);
    }

    private boolean isInFocus(int posX, int posZ) {
        check:
        {
            if (focusedChunk == null)
                break check;
            if (posX < focusedMinPosX || posX > focusedMaxPosX)
                break check;
            if (posZ < focusedMinPosZ || posZ > focusedMaxPosZ)
                break check;
            return true;
        }
        return false;
    }

    public final class FocusedBlockCache extends ReadThroughBlockCache {
        public FocusedBlockCache(LumiWorld world) {
            super(world);
        }

        @Override
        public @NotNull FocusedBlockCacheRoot lumi$root() {
            return FocusedBlockCacheRoot.this;
        }

        @Override
        public @NotNull String lumi$BlockCacheID() {
            return "lumi_focused_block_cache";
        }

        @Override
        public @NotNull String lumi$blockStorageID() {
            return "lumi_focused_block_cache";
        }


        @Override
        public int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBrightness(lightType, subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBrightness(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBrightness(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBrightness(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getLightValue(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getLightValue(lightType, subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getLightValue(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBlockLightValue(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBlockLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getSkyLightValue(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getSkyLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBlockBrightness(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBlockBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBlockOpacity(subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBlockOpacity(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            if (isInFocus(posX, posZ)) {
                val subChunkPosX = posX & 15;
                val subChunkPosZ = posZ & 15;
                return focusedChunk.lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY & 255, subChunkPosZ);
            }
            return world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
        }
    }
}
