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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    private LumiLightingEngine lightingEngine;
    private short[] neighborLightChecks;
    private boolean isLightInitialized;


    @Override
    public LumiChunkRoot rootChunk() {
        return (LumiChunkRoot) this;
    }

    @Override
    public LumiWorld lumiWorld() {
        return (LumiWorld) worldObj;
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
        if (lightingEngine != null)
            return lightingEngine;

        val lumiWorld = ((LumiWorld) worldObj);
        val lightingEngine = lumiWorld.lightingEngine();
        if (lightingEngine == null)
            throw new IllegalStateException();
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
    public void minSkyLightPosY(int minSkyLightPosY) {
        heightMapMinimum = minSkyLightPosY;
    }

    @Override
    public int minSkyLightPosY() {
        return heightMapMinimum;
    }

    @Override
    public void minSkyLightColumns(short[] minSkyLightColumns) {
        this.neighborLightChecks = minSkyLightColumns;
    }

    @Override
    public int[] minSkyLightColumns() {
        return heightMap;
    }

    @Override
    public short[] neighborLightChecks() {
        return neighborLightChecks;
    }

    @Override
    public void hasLightInitialized(boolean hasLightInitialized) {
        this.isLightInitialized = hasLightInitialized;
    }

    @Override
    public boolean hasLightInitialized() {
        return isLightInitialized;
    }

    @Override
    public boolean[] outdatedSkylightColumns() {
        return updateSkylightColumns;
    }
}
