/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.falsepattern.lumina.internal.storage;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;

import static com.falsepattern.lumina.api.world.LumiWorldProvider.WORLD_PROVIDER_VERSION_NBT_TAG_NAME;

public class Utils {
    public static NBTTagCompound readWorldTag(NBTTagCompound input, LumiWorld world, LumiWorldProvider worldProvider, boolean legacy) {
        val worldTagName = legacy ? world.lumi$worldID() : LumiChunk.LUMINA_WORLD_TAG_PREFIX + world.lumi$worldID();
        if (!input.hasKey(worldTagName, 10))
            return null;
        val worldTag = input.getCompoundTag(worldTagName);

        val worldProviderVersion = worldProvider.worldProviderVersion();
        if (!worldProviderVersion.equals(worldTag.getString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME)))
            return null;
        return worldTag;
    }

    public static NBTTagCompound writeWorldTag(NBTTagCompound output, LumiWorld world, LumiWorldProvider worldProvider) {
        val worldTagName = LumiChunk.LUMINA_WORLD_TAG_PREFIX + world.lumi$worldID();
        val worldProviderVersion = worldProvider.worldProviderVersion();
        val worldTag = new NBTTagCompound();
        worldTag.setString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME, worldProviderVersion);
        output.setTag(worldTagName, worldTag);
        return worldTag;
    }
}
