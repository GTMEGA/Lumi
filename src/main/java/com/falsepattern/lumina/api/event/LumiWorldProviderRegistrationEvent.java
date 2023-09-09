/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.event;

import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class LumiWorldProviderRegistrationEvent extends Event {
    private final @NotNull LumiWorldProviderRegistry registry;

    public LumiWorldProviderRegistrationEvent(@NotNull LumiWorldProviderRegistry registry) {
        this.registry = registry;
    }

    public @NotNull LumiWorldProviderRegistry registry() {
        return registry;
    }
}
