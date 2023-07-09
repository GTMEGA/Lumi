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

import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ExtendedBlockStorage.class)
public abstract class LumiSubChunkImplMixin implements LumiSubChunk {
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    @Nullable
    private NibbleArray skylightArray;

    @Override
    public void lumi$setLightValue(EnumSkyBlock lightType,
                                   int subChunkPosX,
                                   int subChunkPosY,
                                   int subChunkPosZ,
                                   int lightValue) {
        switch (lightType) {
            case Block:
                lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                break;
            case Sky:
                lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(EnumSkyBlock lightType,
                                  int subChunkPosX,
                                  int subChunkPosY,
                                  int subChunkPosZ) {
        switch (lightType) {
            case Block:
                return lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            case Sky:
                return lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            default:
                return lightType.defaultLightValue;
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue) {
        blocklightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return blocklightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue) {
        if (skylightArray == null)
            return;
        skylightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        if (skylightArray != null)
            return skylightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
        return 0;
    }

    @Override
    public LumiSubChunkRoot lumi$root() {
        return (LumiSubChunkRoot) this;
    }
}
