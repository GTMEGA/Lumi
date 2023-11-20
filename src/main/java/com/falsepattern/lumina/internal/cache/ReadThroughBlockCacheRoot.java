/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.cache.LumiBlockCache;
import com.falsepattern.lumina.api.cache.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

@RequiredArgsConstructor
public class ReadThroughBlockCacheRoot implements LumiBlockCacheRoot {
    private final LumiWorldRoot worldRoot;

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_readthrough_block_cache_root";
    }

    @Override
    public @NotNull ReadThroughBlockCache lumi$createBlockCache(LumiWorld world) {
        return new ReadThroughBlockCache(world);
    }

    @Override
    public void lumi$clearCache() {

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
        return worldRoot.lumi$getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
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

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ReadThroughBlockCache implements LumiBlockCache {
        private final LumiWorld world;

        @Override
        public @NotNull ReadThroughBlockCacheRoot lumi$root() {
            return ReadThroughBlockCacheRoot.this;
        }

        @Override
        public @NotNull String lumi$BlockCacheID() {
            return "lumi_readthrough_block_cache";
        }

        @Override
        public void lumi$clearCache() {

        }

        @Override
        public @NotNull String lumi$blockStorageID() {
            return "lumi_readthrough_block_cache";
        }

        @Override
        public @NotNull LumiWorld lumi$world() {
            return world;
        }

        @Override
        public int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return world.lumi$getBrightness(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBrightness(int posX, int posY, int posZ) {
            return world.lumi$getBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(int posX, int posY, int posZ) {
            return world.lumi$getLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return world.lumi$getLightValue(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
            return world.lumi$getBlockLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
            return world.lumi$getSkyLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
            return world.lumi$getBlockBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
            return world.lumi$getBlockOpacity(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
        }
    }
}
