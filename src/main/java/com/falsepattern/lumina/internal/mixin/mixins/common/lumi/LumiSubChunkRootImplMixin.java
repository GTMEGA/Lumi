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

import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiSubChunkRootImplMixin implements LumiSubChunkRoot {
    @Shadow
    private int yBase;

    @Shadow
    public abstract Block getBlockByExtId(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Shadow
    public abstract int getExtBlockMetadata(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Override
    public @NotNull Block lumi$getBlock(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return getBlockByExtId(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockMeta(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return getExtBlockMetadata(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$posY() {
        return yBase;
    }
}
