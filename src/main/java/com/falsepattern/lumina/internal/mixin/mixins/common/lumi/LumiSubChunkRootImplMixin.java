/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = ExtendedBlockStorage.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiSubChunkRootImplMixin implements LumiSubChunkRoot {
    @Shadow
    private int yBase;

    @Override
    public @NotNull String lumi$subChunkRootID() {
        return "lumi_sub_chunk_root";
    }

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
