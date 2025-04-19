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

package com.falsepattern.lumi.api.world;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.storage.LumiBlockStorageRoot;
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
