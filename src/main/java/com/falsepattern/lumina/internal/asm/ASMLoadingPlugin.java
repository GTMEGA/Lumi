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

package com.falsepattern.lumina.internal.asm;

import com.falsepattern.lumina.internal.Tags;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import lombok.NoArgsConstructor;

import java.util.Map;

import static cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;

@Name(Tags.MOD_NAME + "|ASM Plugin")
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
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
