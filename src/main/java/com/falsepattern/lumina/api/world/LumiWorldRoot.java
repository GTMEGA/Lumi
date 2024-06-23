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

package com.falsepattern.lumina.api.world;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumina.api.storage.LumiBlockStorageRoot;
import net.minecraft.world.chunk.IChunkProvider;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiWorldRoot extends LumiBlockStorageRoot {
    @Expose
    @NotNull
    String lumi$worldRootID();

    @Expose
    void lumi$markBlockForRenderUpdate(int posX, int posY, int posZ);

    @Expose
    void lumi$scheduleLightingUpdate(int posX, int posY, int posZ);

    @Expose
    @NotNull
    IChunkProvider lumi$chunkProvider();

    @Expose
    boolean lumi$doChunksExistInRange(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    @Expose
    boolean lumi$doChunksExistInRange(int centerPosX, int centerPosY, int centerPosZ, int blockRange);
}
