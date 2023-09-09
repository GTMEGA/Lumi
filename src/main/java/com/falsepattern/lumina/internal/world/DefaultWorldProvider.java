/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DefaultWorldProvider implements LumiWorldProvider {
    private static final DefaultWorldProvider INSTANCE = new DefaultWorldProvider();

    public static DefaultWorldProvider defaultWorldProvider() {
        return INSTANCE;
    }

    @Override
    public @NotNull String worldProviderID() {
        return "lumi_world_provider";
    }

    @Override
    public @NotNull String worldProviderVersion() {
        return Tags.VERSION;
    }

    @Override
    @SuppressWarnings("InstanceofIncompatibleInterface")
    public @Nullable LumiWorld provideWorld(@Nullable World worldBase) {
        if (worldBase instanceof LumiWorld)
            return (LumiWorld) worldBase;
        return null;
    }
}
