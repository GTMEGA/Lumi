/*
 * Copyright (C) 2023 FalsePattern
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

import static com.falsepattern.lumina.internal.world.lighting.LightingHooks.FLAG_COUNT;

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

    @Shadow
    public abstract Block getBlock(int p_150810_1_, int p_150810_2_, int p_150810_3_);

    @Shadow
    public abstract int getBlockMetadata(int p_76628_1_, int p_76628_2_, int p_76628_3_);

    private LumiChunkRoot rootChunk;
    private LumiWorld world;
    private LumiLightingEngine lightingEngine;
    private short[] neighborLightCheckFlags;
    private boolean isLightInitialized;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiChunkInit(CallbackInfo ci) {
        this.rootChunk = (LumiChunkRoot) this;
        this.world = (LumiWorld) worldObj;
        this.lightingEngine = world.lightingEngine();
        this.neighborLightCheckFlags = new short[FLAG_COUNT];
        this.isLightInitialized = false;
    }

    @Override
    public LumiChunkRoot rootChunk() {
        return rootChunk;
    }

    @Override
    public LumiWorld lumiWorld() {
        return world;
    }

    @Override
    public @Nullable LumiSubChunk subChunk(int chunkPosY) {
        val subChunk = storageArrays[chunkPosY];
        if (subChunk instanceof LumiSubChunk)
            return (LumiSubChunk) subChunk;
        return null;
    }

    @Override
    public LumiLightingEngine lightingEngine() {
        return lightingEngine;
    }

    @Override
    public int chunkPosX() {
        return xPosition;
    }

    @Override
    public int chunkPosZ() {
        return zPosition;
    }

    @Override
    public int getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockBrightness = getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
        val blockLightValue = getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockBrightness, blockLightValue);
    }

    @Override
    public int getBlockSkyAndLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockLightValue = getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        val skyLightValue = getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockLightValue, skyLightValue);
    }

    @Override
    public void lumi$setLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        switch (lightType) {
            case Block:
                setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            case Sky:
                setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case Block:
                return getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            case Sky:
                return getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public void setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        rootChunk.prepareSubChunk(chunkPosY);
        val subChunk = subChunk(chunkPosY);
        subChunk.setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        rootChunk.markDirty();
    }

    @Override
    public int getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val chunkPosY = (posY & 255) / 16;

        val subChunk = subChunk(chunkPosY);
        if (subChunk == null)
            return EnumSkyBlock.Block.defaultLightValue;

        val subChunkPosY = posY & 15;
        return subChunk.getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        if (!world.rootWorld().hasSky())
            return;

        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        rootChunk.prepareSubChunk(chunkPosY);
        val subChunk = subChunk(chunkPosY);
        subChunk.setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        rootChunk.markDirty();
    }

    @Override
    public int getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        if (!world.rootWorld().hasSky())
            return 0;

        val chunkPosY = (posY & 255) / 16;

        val subChunk = subChunk(chunkPosY);
        if (subChunk == null) {
            if (isBlockOnTop(subChunkPosX, posY, subChunkPosZ))
                return EnumSkyBlock.Sky.defaultLightValue;
            return 0;
        }

        val subChunkPosY = posY & 15;
        return subChunk.getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
        return getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
        return getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int getBlockBrightness(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition * 16) + subChunkPosX;
        val posZ = (zPosition * 16) + subChunkPosZ;
        return world.getBlockBrightness(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public int getBlockOpacity(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition * 16) + subChunkPosX;
        val posZ = (zPosition * 16) + subChunkPosZ;
        return world.getBlockOpacity(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public boolean isBlockOnTop(int subChunkPosX, int posY, int subChunkPosZ) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        val maxPosY = heightMap[index];
        return maxPosY <= posY;
    }

    @Override
    public int[] skyLightHeights() {
        return heightMap;
    }

    @Override
    public void minSkyLightHeight(int minSkyLightHeight) {
        heightMapMinimum = minSkyLightHeight;
    }

    @Override
    public int minSkyLightHeight() {
        return heightMapMinimum;
    }

    @Override
    public boolean[] outdatedSkyLightColumns() {
        return updateSkylightColumns;
    }

    @Override
    public void neighborLightCheckFlags(short[] neighborLightCheckFlags) {
        this.neighborLightCheckFlags = neighborLightCheckFlags;
    }

    @Override
    public short[] neighborLightCheckFlags() {
        return neighborLightCheckFlags;
    }

    @Override
    public void hasLightInitialized(boolean hasLightInitialized) {
        this.isLightInitialized = hasLightInitialized;
    }

    @Override
    public boolean hasLightInitialized() {
        return isLightInitialized;
    }
}
