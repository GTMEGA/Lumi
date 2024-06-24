/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.internal.storage;

import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.world.LumiWorld;
import com.falsepattern.lumi.api.world.LumiWorldProvider;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;

import static com.falsepattern.lumi.api.world.LumiWorldProvider.WORLD_PROVIDER_VERSION_NBT_TAG_NAME;

public class Utils {
    public static NBTTagCompound readWorldTag(NBTTagCompound input, LumiWorld world, LumiWorldProvider worldProvider, boolean legacy) {
        val worldTagName = legacy ? world.lumi$worldID() : LumiChunk.LUMI_WORLD_TAG_PREFIX + world.lumi$worldID();
        if (!input.hasKey(worldTagName, 10))
            return null;
        val worldTag = input.getCompoundTag(worldTagName);

        val worldProviderVersion = worldProvider.worldProviderVersion();
        if (!worldProviderVersion.equals(worldTag.getString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME)))
            return null;
        return worldTag;
    }

    public static NBTTagCompound writeWorldTag(NBTTagCompound output, LumiWorld world, LumiWorldProvider worldProvider) {
        val worldTagName = LumiChunk.LUMI_WORLD_TAG_PREFIX + world.lumi$worldID();
        val worldProviderVersion = worldProvider.worldProviderVersion();
        val worldTag = new NBTTagCompound();
        worldTag.setString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME, worldProviderVersion);
        output.setTag(worldTagName, worldTag);
        return worldTag;
    }
}
