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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(value = Chunk.class)
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
        return this.neighborLightChecks;
    }

    @Override
    public void lumiGetNeighborLightChecks(short[] data) {
        this.neighborLightChecks = data;
    }

    @Override
    public boolean lumiIsLightInitialized() {
        return this.isLightInitialized;
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
        if (this.lightingEngine == null) {
            this.lightingEngine = ((ILightingEngineProvider) this.worldObj).getLightingEngine();
            if (this.lightingEngine == null) {
                throw new IllegalStateException();
            }
        }
        return this.lightingEngine;
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

    @Shadow
    @Override
    public abstract void setChunkModified();

    @Shadow
    @Override
    public abstract Block getBlock(int x, int y, int z);

    @Override
    public void isGapLightingUpdated(boolean b) {
        isGapLightingUpdated = b;
    }

    @Override
    public void ensureEBSPresent(int y) {
        val ebs = storageArrays[y >> 4];

        if (ebs == null) {
            this.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !this.worldObj.provider.hasNoSky);
            for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                val world = LumiWorldManager.getWorld(worldObj, i);
                val lChunk = world.wrap((Chunk) (Object) this);
                LightingHooks.initSkylightForSection(world, lChunk, lChunk.lumiEBS(y >> 4));
            }
        }
        setChunkModified();
    }
}
