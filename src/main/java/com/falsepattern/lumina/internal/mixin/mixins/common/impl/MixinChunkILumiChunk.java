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

import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiChunkRoot;
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILightingEngine;
import com.falsepattern.lumina.api.ILightingEngineProvider;
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
public abstract class MixinChunkILumiChunk implements ILumiChunk, ILumiChunkRoot {
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

    @Shadow
    public abstract Block getBlock(int x, int y, int z);

    @Shadow
    public abstract int getBlockMetadata(int x, int y, int z);

    private ILightingEngine lightingEngine;
    private short[] neighborLightChecks;
    private boolean isLightInitialized;

    @Override
    public ILumiWorld lumiWorld() {
        return (ILumiWorld) worldObj;
    }

    @Override
    public ILumiEBS lumiEBS(int arrayIndex) {
        val ebs = storageArrays[arrayIndex];
        return ebs == null ? null : (ILumiEBS) ebs;
    }

    @Override
    public short[] lumiGetNeighborLightChecks() {
        return neighborLightChecks;
    }

    @Override
    public void lumiGetNeighborLightChecks(short[] data) {
        this.neighborLightChecks = data;
    }

    @Override
    public boolean lumiIsLightInitialized() {
        return isLightInitialized;
    }

    @Override
    public void lumiIsLightInitialized(boolean lightInitialized) {
        this.isLightInitialized = lightInitialized;
    }

    @Override
    public int[] lumiHeightMap() {
        return heightMap;
    }

    @Override
    public ILightingEngine getLightingEngine() {
        if (lightingEngine == null) {
            lightingEngine = ((ILightingEngineProvider) worldObj).getLightingEngine();
            if (lightingEngine == null) {
                throw new IllegalStateException();
            }
        }
        return lightingEngine;
    }

    @Override
    public int lumiHeightMapMinimum() {
        return heightMapMinimum;
    }

    @Override
    public void lumiHeightMapMinimum(int min) {
        heightMapMinimum = min;
    }

    @Override
    public boolean[] lumiUpdateSkylightColumns() {
        return updateSkylightColumns;
    }

    @Override
    public ILumiChunkRoot root() {
        return this;
    }

    //Root

    @Override
    public int x() {
        return xPosition;
    }

    @Override
    public int z() {
        return zPosition;
    }

    @Override
    public void rootSetChunkModified() {
        setChunkModified();
    }

    @Override
    public Block rootGetBlock(int x, int y, int z) {
        return getBlock(x, y, z);
    }

    @Override
    public int rootGetBlockMetadata(int x, int y, int z) {
        return getBlockMetadata(x, y, z);
    }

    @Override
    public void rootIsGapLightingUpdated(boolean b) {
        isGapLightingUpdated = b;
    }

    @Override
    public void rootEnsureEBSPresent(int y) {
        val ebs = storageArrays[y >> 4];

        if (ebs == null) {
            storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !worldObj.provider.hasNoSky);
            for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                val world = LumiWorldManager.getWorld(worldObj, i);
                val lChunk = world.lumiWrap((Chunk) (Object) this);
                LightingHooks.initSkylightForSection(world, lChunk, lChunk.lumiEBS(y >> 4));
            }
        }
        rootSetChunkModified();
    }


    @Override
    public int rootGetTopFilledSegment() {
        return getTopFilledSegment();
    }

    @Override
    public int[] rootPrecipitationHeightMap() {
        return precipitationHeightMap;
    }
}
