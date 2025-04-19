/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.common;

import com.falsepattern.lumi.internal.ArrayHelper;
import lombok.val;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumi.internal.util.LazyUtil.ensurePresent;
import static com.falsepattern.lumi.internal.util.LazyUtil.lazyGet;
import static com.falsepattern.lumi.internal.util.LazyUtil.lazySet;

@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin {
    @Shadow
    private int blockRefCount;
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    private NibbleArray skylightArray;

    @Unique
    private boolean lumi$isDirty;
    @Unique
    private boolean lumi$isTrivial;

    private static final NibbleArray DUMMY = new NibbleArray(new byte[0], 4);

    @Redirect(method = "<init>",
                   at = @At(value = "NEW",
                            target = "(II)Lnet/minecraft/world/chunk/NibbleArray;",
                            ordinal = 1),
                   require = 1)
    private NibbleArray noBlocklightArray(int p_i1992_1_, int p_i1992_2_) {
        return DUMMY;
    }

    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;blocklightArray:Lnet/minecraft/world/chunk/NibbleArray;"),
              require = 1)
    private void noPutBlockLight(ExtendedBlockStorage instance, NibbleArray value) {

    }

    @Redirect(method = "<init>",
                   at = @At(value = "NEW",
                            target = "(II)Lnet/minecraft/world/chunk/NibbleArray;",
                            ordinal = 2),
                   require = 1)
    private NibbleArray noSkylightArray(int p_i1992_1_, int p_i1992_2_) {
        return DUMMY;
    }

    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;skylightArray:Lnet/minecraft/world/chunk/NibbleArray;"),
              require = 1)
    private void noPutSkyLight(ExtendedBlockStorage instance, NibbleArray value) {

    }


    /**
     * @author FalsePattern
     * @reason Lazy init compat
     */
    @Overwrite
    public NibbleArray getBlocklightArray() {
        return blocklightArray = ensurePresent(blocklightArray);
    }

    /**
     * @author FalsePattern
     * @reason Lazy init compat
     */
    @Overwrite
    public int getExtBlocklightValue(int x, int y, int z) {
        return lazyGet(blocklightArray, x, y, z);
    }

    /**
     * @author FalsePattern
     * @reason Lazy init compat
     */
    @Overwrite
    public NibbleArray getSkylightArray() {
        return skylightArray = ensurePresent(skylightArray);
    }

    /**
     * @author FalsePattern
     * @reason Lazy init compat
     */
    @Overwrite
    public int getExtSkylightValue(int x, int y, int z) {
        return lazyGet(skylightArray, x, y, z);
    }

    @Inject(method = "<init>*",
            at = @At(value = "RETURN",
                     target = "Ljava/util/Random;nextInt(I)I"),
            require = 1)
    private void lumiSubChunkInit(int posY, boolean hasSky, CallbackInfo ci) {
        this.lumi$isDirty = true;
        this.lumi$isTrivial = false;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtSkylightValue(int posX, int posY, int posZ, int lightValue) {
        skylightArray = lazySet(skylightArray, posX, posY, posZ, lightValue);
        if (skylightArray != null)
            lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtBlocklightValue(int posX, int posY, int posZ, int lightValue) {
        blocklightArray = lazySet(blocklightArray, posX, posY, posZ, lightValue);
        if (skylightArray != null)
            lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setBlocklightArray(NibbleArray blockLightArray) {
        this.blocklightArray = blockLightArray;
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setSkylightArray(NibbleArray skyLightArray) {
        this.skylightArray = skyLightArray;
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Send light data to clients when lighting is non-trivial
     */
    @Overwrite
    public boolean isEmpty() {
        if (blockRefCount != 0)
            return false;

        if (lumi$isDirty) {
            val blockLightEmpty = ArrayHelper.isEmpty(blocklightArray);
            val skyLightEmpty = ArrayHelper.isEmpty(skylightArray);
            lumi$isTrivial = blockLightEmpty && skyLightEmpty;
            lumi$isDirty = false;
        }

        return lumi$isTrivial;
    }
}
