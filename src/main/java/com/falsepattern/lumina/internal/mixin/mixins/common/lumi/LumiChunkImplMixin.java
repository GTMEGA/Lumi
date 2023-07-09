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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumina.internal.engine.LightingHooks.FLAG_COUNT;

@Mixin(Chunk.class)
public abstract class LumiChunkImplMixin implements LumiChunk {
    @Final
    @Shadow
    public int xPosition;
    @Final
    @Shadow
    public int zPosition;

    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public boolean[] updateSkylightColumns;
    @Shadow
    public World worldObj;
    @Shadow
    public int[] heightMap;
    @Shadow
    public int heightMapMinimum;

    private LumiChunkRoot root;
    private LumiWorld world;
    private LumiLightingEngine lightingEngine;
    private short[] neighborLightCheckFlags;
    private boolean isLightInitialized;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiChunkInit(CallbackInfo ci) {
        this.root = (LumiChunkRoot) this;
        this.world = (LumiWorld) worldObj;
        this.lightingEngine = world.lumi$lightingEngine();
        this.neighborLightCheckFlags = new short[FLAG_COUNT];
        this.isLightInitialized = false;
    }

    @Override
    public LumiChunkRoot lumi$root() {
        return root;
    }

    @Override
    public LumiWorld lumi$world() {
        return world;
    }

    @Override
    public @Nullable LumiSubChunk lumi$subChunk(int chunkPosY) {
        val subChunk = storageArrays[chunkPosY];
        if (subChunk instanceof LumiSubChunk)
            return (LumiSubChunk) subChunk;
        return null;
    }

    @Override
    public LumiLightingEngine lumi$lightingEngine() {
        return lightingEngine;
    }

    @Override
    public int lumi$chunkPosX() {
        return xPosition;
    }

    @Override
    public int lumi$chunkPosZ() {
        return zPosition;
    }

    @Override
    public int lumi$getBrightnessAndLightValueMax(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case Block:
                return lumi$getBrightnessAndBlockLightValueMax(subChunkPosX, posY, subChunkPosZ);
            case Sky:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockBrightness = lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockBrightness, blockLightValue);
    }

    @Override
    public int lumi$getLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        val skyLightValue = lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockLightValue, skyLightValue);
    }

    @Override
    public void lumi$setLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        switch (lightType) {
            case Block:
                lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            case Sky:
                lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case Block:
                return lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            case Sky:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        root.lumi$prepareSubChunk(chunkPosY);
        val subChunk = lumi$subChunk(chunkPosY);
        subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        root.lumi$markDirty();
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$subChunk(chunkPosY);
        if (subChunk == null)
            return EnumSkyBlock.Block.defaultLightValue;

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        if (!world.lumi$root().lumi$hasSky())
            return;

        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        root.lumi$prepareSubChunk(chunkPosY);
        val subChunk = lumi$subChunk(chunkPosY);
        subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        root.lumi$markDirty();
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        if (!world.lumi$root().lumi$hasSky())
            return 0;

        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$subChunk(chunkPosY);
        if (subChunk == null) {
            if (lumi$isBlockOnTop(subChunkPosX, posY, subChunkPosZ))
                return EnumSkyBlock.Sky.defaultLightValue;
            return 0;
        }

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition * 16) + subChunkPosX;
        val posZ = (zPosition * 16) + subChunkPosZ;
        return world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition * 16) + subChunkPosX;
        val posZ = (zPosition * 16) + subChunkPosZ;
        return world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public boolean lumi$isBlockOnTop(int subChunkPosX, int posY, int subChunkPosZ) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        val maxPosY = heightMap[index];
        return maxPosY <= posY;
    }

    @Override
    public int[] lumi$skyLightHeights() {
        return heightMap;
    }

    @Override
    public void lumi$minSkyLightHeight(int minSkyLightHeight) {
        heightMapMinimum = minSkyLightHeight;
    }

    @Override
    public int lumi$minSkyLightHeight() {
        return heightMapMinimum;
    }

    @Override
    public boolean[] lumi$outdatedSkyLightColumns() {
        return updateSkylightColumns;
    }

    @Override
    public short[] lumi$neighborLightCheckFlags() {
        return neighborLightCheckFlags;
    }

    @Override
    public void lumi$hasLightInitialized(boolean hasLightInitialized) {
        this.isLightInitialized = hasLightInitialized;
    }

    @Override
    public boolean lumi$hasLightInitialized() {
        return isLightInitialized;
    }
}
