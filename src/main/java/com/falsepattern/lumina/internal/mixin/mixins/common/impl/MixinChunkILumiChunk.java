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
import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILightingEngine;
import com.falsepattern.lumina.api.ILightingEngineProvider;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

@Mixin(value = Chunk.class)
public abstract class MixinChunkILumiChunk implements ILumiChunk {
    @Shadow
    public boolean isModified;
    @Shadow
    public boolean[] updateSkylightColumns;
    @Shadow
    public boolean isLightPopulated;
    @Shadow
    private boolean isGapLightingUpdated;
    @Shadow
    public World worldObj;

    private ILightingEngine lightingEngine;
    private short[] neighborLightChecks;
    private boolean isLightInitialized;

    @Shadow
    @Override
    public abstract void setLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z, int lightValue);

    @Shadow
    @Override
    public abstract int getSavedLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z);

    @Shadow
    @Override
    public abstract boolean canBlockSeeTheSky(int x, int y, int z);

    @Override
    public ILumiWorld world() {
        return (ILumiWorld) worldObj;
    }

    @Override
    public ILumiEBS getLumiEBS(int arrayIndex) {
        val ebs = storageArrays[arrayIndex];
        return ebs == null ? null : (ILumiEBS) ebs;
    }

    @Override
    public short[] getNeighborLightChecks() {
        return this.neighborLightChecks;
    }

    @Override
    public void setNeighborLightChecks(short[] data) {
        this.neighborLightChecks = data;
    }

    @Override
    public boolean isLightInitialized() {
        return this.isLightInitialized;
    }

    @Override
    public void setLightInitialized(boolean lightInitialized) {
        this.isLightInitialized = lightInitialized;
    }

    @Override
    public void isLightPopulated(boolean state) {
        isLightPopulated = state;
    }

    @Override
    public void isGapLightingUpdated(boolean b) {
        isGapLightingUpdated = b;
    }

    @Shadow
    protected abstract void recheckGaps(boolean p_150803_1_);

    @Shadow private ExtendedBlockStorage[] storageArrays;

    @Shadow public int[] heightMap;

    @Override
    public void setSkylightUpdatedPublic() {
        Arrays.fill(this.updateSkylightColumns, true);

        this.recheckGaps(false);
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

    //Proxies

    @Override
    public void setHeightValue(int x, int z, int val) {
        this.heightMap[z << 4 | x] = val;
    }

    @Shadow
    @Override
    public abstract int getHeightValue(int x, int z);

    @Shadow @Final public int xPosition;

    @Shadow @Final public int zPosition;

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

    @Shadow public int heightMapMinimum;

    @Override
    public int heightMapMinimum() {
        return heightMapMinimum;
    }

    @Override
    public void heightMapMinimum(int min) {
        heightMapMinimum = min;
    }

    @Override
    public boolean[] updateSkylightColumns() {
        return updateSkylightColumns;
    }

    @Override
    public void isModified(boolean b) {
        isModified = b;
    }
}
