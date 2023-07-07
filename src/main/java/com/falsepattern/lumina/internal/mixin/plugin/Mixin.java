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
import static com.falsepattern.lib.mixin.IMixin.Side.*;
import static com.falsepattern.lumina.internal.mixin.plugin.TargetedMod.ARCHAICFIX;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    common_MixinAnvilChunkLoader(COMMON, always(), "AnvilChunkLoaderMixin"),
    common_MixinChunk(COMMON, always(), "ChunkMixin"),
    common_MixinChunkProviderServer(COMMON, always(), "ChunkProviderServerMixin"),
    common_MixinExtendedBlockStorage(COMMON, always(), "ExtendedBlockStorageMixin"),
    common_MixinSPacketChunkData(COMMON, always(), "S21PacketChunkDataMixin"),
    common_MixinWorld(COMMON, always(), "WorldMixin"),

    client_MixinMinecraft(CLIENT, always(), "MinecraftMixin"),
    client_MixinChunk(CLIENT, always(), "ChunkMixin"),
    client_MixinChunkCache(CLIENT, always(), "ChunkCacheMixin"),
    client_MixinWorld(CLIENT, always(), "WorldMixin"),

    common_impl_MixinChunkILumiChunk(COMMON, always(), "impl.ChunkILumiChunkMixin"),
    common_impl_MixinExtendedBlockStorageILumiEBS(COMMON, always(), "impl.ExtendedBlockStorageILumiEBSMixin"),
    common_impl_MixinWorldILumiWorld(COMMON, always(), "impl.WorldILumiWorldMixin"),

    common_MixinLongHashMap(COMMON, avoid(ARCHAICFIX), "LongHashMapMixin"),
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

