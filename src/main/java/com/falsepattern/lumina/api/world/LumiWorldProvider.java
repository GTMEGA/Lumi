/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.world;

import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiWorldProvider {
    static final String WORLD_PROVIDER_VERSION_NBT_TAG_NAME = "lumi_world_provider_version";

    @NotNull String worldProviderID();

    @NotNull String worldProviderVersion();

    @Nullable LumiWorld provideWorld(@Nullable World worldBase);
}
