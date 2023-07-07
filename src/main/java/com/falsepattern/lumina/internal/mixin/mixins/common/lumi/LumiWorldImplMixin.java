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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import com.falsepattern.lumina.internal.Tags;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class LumiWorldImplMixin implements IBlockAccess, LumiWorld {
    private LumiLightingEngine lightingEngine;

    @Override
    public String luminaWorldID() {
        return Tags.MODID;
    }

    @Override
    public LumiWorldRoot rootWorld() {
        return (LumiWorldRoot) this;
    }

    @Override
    public LumiChunk toLumiChunk(Chunk vanillaChunk) {
        return (LumiChunk) vanillaChunk;
    }

    @Override
    public LumiSubChunk toLumiSubChunk(ExtendedBlockStorage vanillaSubChunk) {
        return (LumiSubChunk) vanillaSubChunk;
    }

    @Override
    public void lightingEngine(LumiLightingEngine lightingEngine) {
        this.lightingEngine = lightingEngine;
    }

    @Override
    public LumiLightingEngine lightingEngine() {
        return lightingEngine;
    }

    @Override
    public int getBlockLightValue(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightValue(this, posX, posY, posZ);
    }

    @Override
    public int getBlockLightOpacity(Block block, int blockMeta, int posX, int posY, int posZ) {
        return block.getLightOpacity(this, posX, posY, posZ);
    }
}
