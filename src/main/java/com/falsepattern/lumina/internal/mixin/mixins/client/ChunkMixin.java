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

package com.falsepattern.lumina.internal.mixin.mixins.client;

import com.falsepattern.lumina.internal.lighting.LightingHooksOld;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import lombok.var;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Shadow
    public World worldObj;

    /**
     * @author FalsePattern
     * @reason Fix
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void generateHeightMap() {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            LightingHooksOld.generateHeightMap(chunk);
        }
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
