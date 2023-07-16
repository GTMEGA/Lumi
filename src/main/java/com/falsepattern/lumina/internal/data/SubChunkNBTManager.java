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

package com.falsepattern.lumina.internal.data;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SubChunkNBTManager implements ChunkDataManager.SectionNBTDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Sub Chunk NBT Manager");

    private static final SubChunkNBTManager INSTANCE = new SubChunkNBTManager();

    private boolean isRegistered = false;

    public static SubChunkNBTManager subChunkNBTManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        ChunkDataRegistry.registerDataManager(this);
        isRegistered = true;
        LOG.info("Registered data manager");
    }

    @Override
    public String domain() {
        return Tags.MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_sub_chunk";
    }

    @Override
    public void writeSectionToNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound output) {
        val chunk = (LumiChunk) chunkBase;
        val subChunk = (LumiSubChunk) subChunkBase;
    }

    @Override
    public void readSectionFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
        val chunk = (LumiChunk) chunkBase;
        val subChunk = (LumiSubChunk) subChunkBase;
    }
}
