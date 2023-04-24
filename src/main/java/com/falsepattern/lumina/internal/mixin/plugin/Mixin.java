/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.internal.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lumina.internal.mixin.plugin.TargetedMod.ARCHAICFIX;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    common_MixinLongHashMap(Side.COMMON, avoid(ARCHAICFIX), "MixinLongHashMap"),
    common_MixinAnvilChunkLoader(Side.COMMON, always(), "MixinAnvilChunkLoader"),
    common_MixinChunk(Side.COMMON, always(), "MixinChunk"),
    common_MixinChunkProviderServer(Side.COMMON, always(), "MixinChunkProviderServer"),
    common_MixinExtendedBlockStorage(Side.COMMON, always(), "MixinExtendedBlockStorage"),
    common_MixinSPacketChunkData(Side.COMMON, always(), "MixinSPacketChunkData"),
    common_MixinWorld(Side.COMMON, always(), "MixinWorld"),
    common_impl_MixinChunkILumiChunk(Side.COMMON, always(), "impl.MixinChunkILumiChunk"),
    common_impl_MixinExtendedBlockStorageILumiEBS(Side.COMMON, always(), "impl.MixinExtendedBlockStorageILumiEBS"),
    common_impl_MixinWorldILumiWorld(Side.COMMON, always(), "impl.MixinWorldILumiWorld"),
    client_MixinMinecraft(Side.CLIENT, always(), "MixinMinecraft"),
    client_MixinChunk(Side.CLIENT, always(), "MixinChunk"),
    client_MixinChunkCache(Side.CLIENT, always(), "MixinChunkCache"),
    client_MixinWorld(Side.CLIENT, always(), "MixinWorld"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

