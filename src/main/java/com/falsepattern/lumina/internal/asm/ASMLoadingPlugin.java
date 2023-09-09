/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.falsepattern.lumina.internal.Tags.MOD_NAME;
import static cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;

@Name(MOD_NAME + "|ASM Plugin")
@MCVersion("1.7.10")
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions("com.falsepattern.lumina.internal.asm")
@NoArgsConstructor
public final class ASMLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{PhosphorDataInjector.class.getName()};
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
