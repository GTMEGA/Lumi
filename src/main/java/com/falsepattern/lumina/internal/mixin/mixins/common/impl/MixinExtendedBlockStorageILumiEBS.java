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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorageILumiEBS implements ILumiEBS {
    @Shadow
    private int blockRefCount;

    @Shadow private NibbleArray skylightArray;
    @Shadow private NibbleArray blocklightArray;
    private int lightRefCount = -1;

    @Shadow
    @Override
    public abstract int getExtSkylightValue(int x, int y, int z);

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtSkylightValue(int x, int y, int z, int value) {
        this.skylightArray.set(x, y, z, value);
        this.lightRefCount = -1;
    }

    @Shadow
    @Override
    public abstract int getExtBlocklightValue(int x, int y, int z);

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtBlocklightValue(int x, int y, int z, int value) {
        this.blocklightArray.set(x, y, z, value);
        this.lightRefCount = -1;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setBlocklightArray(NibbleArray array) {
        this.blocklightArray = array;
        this.lightRefCount = -1;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setSkylightArray(NibbleArray array) {
        this.skylightArray = array;
        this.lightRefCount = -1;
    }


    /**
     * @author Angeline
     * @reason Send light data to clients when lighting is non-trivial
     */
    @Overwrite
    public boolean isEmpty() {
        if (this.blockRefCount != 0) {
            return false;
        }

        // -1 indicates the lightRefCount needs to be re-calculated
        if (this.lightRefCount == -1) {
            if (this.checkLightArrayEqual(this.skylightArray, (byte) 0xFF)
                    && this.checkLightArrayEqual(this.blocklightArray, (byte) 0x00)) {
                this.lightRefCount = 0; // Lighting is trivial, don't send to clients
            } else {
                this.lightRefCount = 1; // Lighting is not trivial, send to clients
            }
        }

        return this.lightRefCount == 0;
    }

    private boolean checkLightArrayEqual(NibbleArray storage, byte val) {
        if (storage == null) {
            return true;
        }

        byte[] arr = storage.data;

        for (byte b : arr) {
            if (b != val) {
                return false;
            }
        }

        return true;
    }

    @Shadow
    @Override
    public abstract Block getBlockByExtId(int x, int y, int z);

    @Shadow
    @Override
    public abstract int getYLocation();
}
