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

package com.falsepattern.lumina.internal.mixin.mixins.common.impl;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.engine.LumiLightingEngineProvider;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(Chunk.class)
public abstract class ChunkILumiChunkMixin implements LumiChunk, LumiChunkRoot {
    @Shadow
    @Final
    public int xPosition;
    @Shadow
    @Final
    public int zPosition;
    @Shadow
    public World worldObj;
    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public int[] heightMap;
    @Shadow
    public boolean[] updateSkylightColumns;
    @Shadow
    private boolean isGapLightingUpdated;
    @Shadow
    public int heightMapMinimum;
    @Shadow
    public int[] precipitationHeightMap;

    @Shadow
    public abstract void setChunkModified();

    @Shadow
    public abstract int getTopFilledSegment();

    @Override
    @Shadow
    public abstract Block getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract int getBlockMetadata(int x, int y, int z);

    private LumiLightingEngine lightingEngine;
    private short[] neighborLightChecks;
    private boolean isLightInitialized;

    @Override
    public LumiWorld lumiWorld() {
        return (LumiWorld) worldObj;
    }

    @Override
    public LumiSubChunk subChunk(int index) {
        val ebs = storageArrays[index];
        return ebs == null ? null : (LumiSubChunk) ebs;
    }

    @Override
    public short[] neighborLightChecks() {
        return neighborLightChecks;
    }

    @Override
    public void neighborLightChecks(short[] neighborLightChecks) {
        this.neighborLightChecks = neighborLightChecks;
    }

    @Override
    public boolean lightInitialized() {
        return isLightInitialized;
    }

    @Override
    public void lightInitialized(boolean lightInitialized) {
        this.isLightInitialized = lightInitialized;
    }

    @Override
    public int[] skylightColumnHeightArray() {
        return heightMap;
    }

    @Override
    public LumiLightingEngine lightingEngine() {
        if (lightingEngine == null) {
            lightingEngine = ((LumiLightingEngineProvider) worldObj).lightingEngine();
            if (lightingEngine == null) {
                throw new IllegalStateException();
            }
        }
        return lightingEngine;
    }

    @Override
    public int minSkylightColumnHeight() {
        return heightMapMinimum;
    }

    @Override
    public void minSkylightColumnHeight(int minSkylightColumnHeight) {
        heightMapMinimum = minSkylightColumnHeight;
    }

    @Override
    public boolean[] outdatedSkylightColumns() {
        return updateSkylightColumns;
    }

    @Override
    public LumiChunkRoot chunkRoot() {
        return this;
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
    public void markDirty() {
        setChunkModified();
    }

    @Override
    public int getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public void rootIsGapLightingUpdated(boolean b) {
        isGapLightingUpdated = b;
    }

    @Override
    public void prepareSubChunk(int posY) {
        val ebs = storageArrays[posY >> 4];

        if (ebs == null) {
            storageArrays[posY >> 4] = new ExtendedBlockStorage(posY >> 4 << 4, !worldObj.provider.hasNoSky);
            for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                val world = LumiWorldManager.getWorld(worldObj, i);
                val lChunk = world.toLumiChunk((Chunk) (Object) this);
                LightingHooks.initSkylightForSection(world, lChunk, lChunk.subChunk(posY >> 4));
            }
        }
        markDirty();
    }

    @Override
    public int topExistingSubChunkIndex() {
        return getTopFilledSegment();
    }

    @Override
    public int[] precipitationHeightArray() {
        return precipitationHeightMap;
    }
}
