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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import lombok.val;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin {
    @Shadow
    private int blockRefCount;
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    private NibbleArray skylightArray;

    private boolean isDirty;
    private boolean isTrivial;

    @Inject(method = "<init>*",
            at = @At(value = "RETURN",
                     target = "Ljava/util/Random;nextInt(I)I"),
            require = 1)
    private void lumiSubChunkInit(int posY, boolean hasSky, CallbackInfo ci) {
        this.isDirty = true;
        this.isTrivial = false;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtSkylightValue(int posX, int posY, int posZ, int lightValue) {
        skylightArray.set(posX, posY, posZ, lightValue);
        isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtBlocklightValue(int posX, int posY, int posZ, int lightValue) {
        blocklightArray.set(posX, posY, posZ, lightValue);
        isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setBlocklightArray(NibbleArray blockLightArray) {
        this.blocklightArray = blockLightArray;
        isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setSkylightArray(NibbleArray skyLightArray) {
        this.skylightArray = skyLightArray;
        isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Send light data to clients when lighting is non-trivial
     */
    @Overwrite
    public boolean isEmpty() {
        if (blockRefCount != 0)
            return false;

        if (isDirty) {
            val blockLightEqual = checkLightArrayEqual(blocklightArray, EnumSkyBlock.Block);
            val skyLightEqual = checkLightArrayEqual(skylightArray, EnumSkyBlock.Sky);
            isTrivial = blockLightEqual && skyLightEqual;
            isDirty = false;
        }

        return isTrivial;
    }

    private boolean checkLightArrayEqual(NibbleArray storage, EnumSkyBlock lightType) {
        if (storage == null)
            return true;

        val expectedValue = (byte) lightType.defaultLightValue;
        val data = storage.data;
        for (val value : data)
            if (value != expectedValue)
                return false;

        return true;
    }
}
