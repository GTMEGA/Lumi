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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.api.ILumiEBS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorage {
    @Shadow
    private int blockRefCount;
    @Shadow
    private NibbleArray skylightArray;
    @Shadow
    private NibbleArray blocklightArray;

    private int lightRefCount = -1;

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtSkylightValue(int posX, int posY, int posZ, int lightValue) {
        skylightArray.set(posX, posY, posZ, lightValue);
        lightRefCount = -1;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtBlocklightValue(int posX, int posY, int posZ, int lightValue) {
        blocklightArray.set(posX, posY, posZ, lightValue);
        lightRefCount = -1;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setBlocklightArray(NibbleArray array) {
        blocklightArray = array;
        lightRefCount = -1;
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
        if (blockRefCount != 0)
            return false;

        // -1 indicates the lightRefCount needs to be re-calculated
        if (lightRefCount == -1) {
            if (checkLightArrayEqual(skylightArray, (byte) 0xFF)
                && checkLightArrayEqual(blocklightArray, (byte) 0x00)) {
                lightRefCount = 0; // Lighting is trivial, don't send to clients
            } else {
                lightRefCount = 1; // Lighting is not trivial, send to clients
            }
        }

        return lightRefCount == 0;
    }

    private boolean checkLightArrayEqual(NibbleArray storage, byte val) {
        if (storage == null)
            return true;

        byte[] arr = storage.data;

        for (byte b : arr) {
            if (b != val) {
                return false;
            }
        }

        return true;
    }
}
