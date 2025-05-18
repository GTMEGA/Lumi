/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.lumi;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.lumi.api.LumiChunkAPI;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.chunk.LumiChunkRoot;
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

import static com.falsepattern.lumi.api.init.LumiChunkInitHook.LUMI_CHUNK_INIT_HOOK_INFO;
import static com.falsepattern.lumi.api.init.LumiChunkInitHook.LUMI_CHUNK_INIT_HOOK_METHOD;
import static com.falsepattern.lumi.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumi.api.lighting.LightType.SKY_LIGHT_TYPE;

@Unique
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
    private int queuedLightChecks;
    @Shadow
    public boolean[] updateSkylightColumns;
    @Shadow
    public World worldObj;
    @Shadow
    public int[] heightMap;
    @Shadow
    public int heightMapMinimum;

    private LumiChunkRoot lumi$root;
    private LumiWorld lumi$world;
    private boolean lumi$isLightingInitialized;

    @Inject(method = LUMI_CHUNK_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    @Dynamic(LUMI_CHUNK_INIT_HOOK_INFO)
    private void lumiChunkInit(CallbackInfo ci) {
        this.lumi$root = (LumiChunkRoot) this;
        this.lumi$world = (LumiWorld) worldObj;
        this.lumi$isLightingInitialized = false;
    }

    @Override
    public @NotNull LumiChunkRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public @NotNull LumiWorld lumi$world() {
        return lumi$world;
    }

    @Override
    public @NotNull String lumi$chunkID() {
        return "lumi_chunk";
    }

    @Override
    public void lumi$writeToNBT(@NotNull NBTTagCompound output) {
        output.setIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, heightMap);
        output.setBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, lumi$isLightingInitialized);
    }

    @Override
    public void lumi$readFromNBT(@NotNull NBTTagCompound input) {
        lumi$isLightingInitialized = false;
        skyLightHeightMapValidCheck:
        {
            if (!input.hasKey(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, 1))
                break skyLightHeightMapValidCheck;
            val isLightInitializedInput = input.getBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME);
            if (!isLightInitializedInput)
                break skyLightHeightMapValidCheck;

            if (!input.hasKey(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, 11))
                break skyLightHeightMapValidCheck;
            val skyLightHeightMapInput = input.getIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME);
            if (skyLightHeightMapInput.length != HEIGHT_MAP_ARRAY_SIZE)
                break skyLightHeightMapValidCheck;

            System.arraycopy(skyLightHeightMapInput, 0, heightMap, 0, HEIGHT_MAP_ARRAY_SIZE);
            lumi$isLightingInitialized = true;
        }
        if (!lumi$isLightingInitialized)
            LumiChunkAPI.scheduleChunkLightingEngineInit(this);
    }

    @Override
    public void lumi$cloneFrom(@NotNull LumiChunk from) {
        heightMap = ArrayUtil.copyArray(from.lumi$skyLightHeightMap(), lumi$skyLightHeightMap());
        lumi$isLightingInitialized = from.lumi$isLightingInitialized();
    }

    @Override
    public void lumi$writeToPacket(@NotNull ByteBuffer output) {
    }

    @Override
    public void lumi$readFromPacket(@NotNull ByteBuffer input) {
        lumi$isLightingInitialized = true;
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiSubChunk lumi$getSubChunkIfPrepared(int chunkPosY) {
        val subChunk = storageArrays[chunkPosY];
        if (subChunk instanceof LumiSubChunk)
            return (LumiSubChunk) subChunk;
        return null;
    }

    @Override
    @SuppressWarnings("CastToIncompatibleInterface")
    public @NotNull LumiSubChunk lumi$getSubChunk(int chunkPosY) {
        lumi$root.lumi$prepareSubChunk(chunkPosY);
        val subChunk = storageArrays[chunkPosY];
        return (LumiSubChunk) subChunk;
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
    public void lumi$queuedRandomLightUpdates(int queuedRandomLightUpdates) {
        this.queuedLightChecks = queuedRandomLightUpdates;
    }

    @Override
    public int lumi$queuedRandomLightUpdates() {
        return queuedLightChecks;
    }

    @Override
    public void lumi$resetQueuedRandomLightUpdates() {
        queuedLightChecks = 0;
    }

    @Override
    public int lumi$getBrightness(@NotNull LightType lightType,
                                  int subChunkPosX,
                                  int posY,
                                  int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightness(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockBrightness = lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockBrightness, blockLightValue);
    }

    @Override
    public int lumi$getLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        val skyLightValue = lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockLightValue, skyLightValue);
    }

    @Override
    public void lumi$setLightValue(@NotNull LightType lightType,
                                   int subChunkPosX,
                                   int posY,
                                   int subChunkPosZ,
                                   int lightValue) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            case SKY_LIGHT_TYPE:
                lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        val chunkPosY = (posY & 255) / 16;

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunk(chunkPosY);
        subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        lumi$root.lumi$markDirty();
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$getSubChunk(chunkPosY);

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        return subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        if (!lumi$world.lumi$root().lumi$hasSky())
            return;

        val chunkPosY = (posY & 255) / 16;

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunk(chunkPosY);
        subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        lumi$root.lumi$markDirty();
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        if (!lumi$world.lumi$root().lumi$hasSky())
            return 0;

        val chunkPosY = (posY & 255) >> 4;

        subChunkPosX &= 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunk(chunkPosY);

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = lumi$root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = lumi$root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = lumi$root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = lumi$root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(@NotNull Block block,
                                       int blockMeta,
                                       int subChunkPosX,
                                       int posY,
                                       int subChunkPosZ) {
        if (!lumi$isLightingInitialized)
            return block.getLightValue();
        val posX = (xPosition << 4) + subChunkPosX;
        val posZ = (zPosition << 4) + subChunkPosZ;
        return lumi$world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(@NotNull Block block,
                                    int blockMeta,
                                    int subChunkPosX,
                                    int posY,
                                    int subChunkPosZ) {
        if (!lumi$isLightingInitialized)
            return block.getLightOpacity();
        val posX = (xPosition << 4) + subChunkPosX;
        val posZ = (zPosition << 4) + subChunkPosZ;
        return lumi$world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        val maxPosY = heightMap[index];
        return maxPosY <= posY;
    }

    @Override
    public void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        heightMap[index] = skyLightHeight;
    }

    @Override
    public int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        return heightMap[index];
    }

    @Override
    public void lumi$minSkyLightHeight(int minSkyLightHeight) {
        this.heightMapMinimum = minSkyLightHeight;
    }

    @Override
    public int lumi$minSkyLightHeight() {
        return heightMapMinimum;
    }

    @Override
    public void lumi$resetSkyLightHeightMap() {
        LumiChunkAPI.resetHeightMapArray(heightMap);
        heightMapMinimum = Integer.MAX_VALUE;
    }

    @Override
    public void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isHeightOutdated) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        updateSkylightColumns[index] = isHeightOutdated;
    }

    @Override
    public boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        return updateSkylightColumns[index];
    }

    @Override
    public void lumi$resetOutdatedHeightFlags() {
        LumiChunkAPI.resetUpdateSkylightColumns(updateSkylightColumns);
    }

    @Override
    public void lumi$isLightingInitialized(boolean isLightingInitialized) {
        this.lumi$isLightingInitialized = isLightingInitialized;
    }

    @Override
    public boolean lumi$isLightingInitialized() {
        return lumi$isLightingInitialized;
    }

    @Override
    public void lumi$resetLighting() {
        lumi$isLightingInitialized = false;
        lumi$world.lumi$lightingEngine().handleChunkInit(this);
    }

    @Override
    public int @NotNull [] lumi$skyLightHeightMap() {
        return heightMap;
    }
}
