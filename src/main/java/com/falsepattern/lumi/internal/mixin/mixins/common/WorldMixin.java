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

import com.falsepattern.lumi.internal.mixin.hook.LightingHooks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(World.class)
public abstract class WorldMixin implements IBlockAccess {
    /**
     * @author Ven
     * @reason Redirect into LUMI
     */
    @Overwrite
    public boolean updateLightByType(EnumSkyBlock baseLightType, int posX, int posY, int posZ) {
        LightingHooks.scheduleLightingUpdate(thiz(), baseLightType, posX, posY, posZ);
        return true;
    }

    private World thiz() {
        return (World) (Object) this;
    }
}
