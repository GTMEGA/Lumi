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
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorageILumiEBS implements ILumiEBS, ILumiEBSRoot {
    @Shadow private NibbleArray blocklightArray;
    @Shadow private NibbleArray skylightArray;

    @Override
    public NibbleArray lumiSkylightArray() {
        return skylightArray;
    }

    @Override
    public NibbleArray lumiBlocklightArray() {
        return blocklightArray;
    }

    @Override
    public ILumiEBSRoot root() {
        return this;
    }

    @Shadow
    public abstract Block getBlockByExtId(int x, int y, int z);

    @Override
    public Block rootGetBlockByExtId(int x, int y, int z) {
        return getBlockByExtId(x, y, z);
    }

    @Shadow
    public abstract int getExtBlockMetadata(int x, int y, int z);

    @Override
    public int rootGetExtBlockMetadata(int x, int y, int z) {
        return getExtBlockMetadata(x, y, z);
    }

    @Shadow
    public abstract int getYLocation();

    @Override
    public int rootGetYLocation() {
        return getYLocation();
    }
}
