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

package com.falsepattern.lumi.api.init;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.StableAPI.Internal;

import static com.falsepattern.lib.StableAPI.Expose;
import static com.falsepattern.lumi.api.LumiAPI.LUMI_MOD_NAME;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiExtendedBlockStorageInitHook {
    @Internal
    String LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_INFO = "Implemented by [" + LUMI_MOD_NAME + "] with the interface " +
                                                        "[com.falsepattern.lumi.api.init.LumiExtendedBlockStorageInitHook]";
    @Expose
    String LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_METHOD = "lumi$onExtendedBlockStorageInit()V";

    @Internal
    void lumi$doExtendedBlockStorageInit();

    @Internal
    void lumi$onExtendedBlockStorageInit();

    @Internal
    boolean lumi$initHookExecuted();
}
