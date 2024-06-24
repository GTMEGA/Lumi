/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.internal.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import com.falsepattern.lumi.internal.Tags;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.falsepattern.lumi.internal.Tags.MOD_NAME;
import static cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;

@Name(MOD_NAME + "|ASM Plugin")
@MCVersion("1.7.10")
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions("com.falsepattern.lumi.internal.asm")
@NoArgsConstructor
public final class ASMLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Tags.GROUPNAME + ".internal.asm.LumiClassTransformer"};
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
    public void injectData(Map<String, Object> data) {}

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
