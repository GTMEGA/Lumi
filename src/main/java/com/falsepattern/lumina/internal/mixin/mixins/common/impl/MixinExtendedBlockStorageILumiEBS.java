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

import com.falsepattern.lumina.api.ILumiEBS;
import com.falsepattern.lumina.api.ILumiEBSRoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorageILumiEBS implements ILumiEBS, ILumiEBSRoot {
    @Shadow public abstract void setExtBlocklightValue(int p_76677_1_, int p_76677_2_, int p_76677_3_, int p_76677_4_);

    @Shadow public abstract int getExtBlocklightValue(int p_76674_1_, int p_76674_2_, int p_76674_3_);

    @Shadow public abstract void setExtSkylightValue(int p_76657_1_, int p_76657_2_, int p_76657_3_, int p_76657_4_);

    @Shadow public abstract int getExtSkylightValue(int p_76670_1_, int p_76670_2_, int p_76670_3_);

    @Override
    public int lumiGetSkylight(int x, int y, int z) {
        return getExtSkylightValue(x, y, z);
    }

    @Override
    public void lumiSetSkylight(int x, int y, int z, int defaultLightValue) {
        setExtSkylightValue(x, y, z, defaultLightValue);
    }

    @Override
    public int lumiGetBlocklight(int x, int y, int z) {
        return getExtBlocklightValue(x, y, z);
    }

    @Override
    public void lumiSetBlocklight(int x, int y, int z, int defaultLightValue) {
        setExtBlocklightValue(x, y, z, defaultLightValue);
    }

    @Override
    public ILumiEBSRoot root() {
        return this;
    }

    @Override
    @Shadow
    public abstract Block getBlockByExtId(int x, int y, int z);

    @Override
    @Shadow
    public abstract int getYLocation();
}
