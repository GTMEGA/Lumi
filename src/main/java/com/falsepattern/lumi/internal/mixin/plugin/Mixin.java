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

package com.falsepattern.lumi.internal.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.*;
import static com.falsepattern.lib.mixin.IMixin.Side.CLIENT;
import static com.falsepattern.lib.mixin.IMixin.Side.COMMON;
import static com.falsepattern.lumi.internal.mixin.plugin.TargetedMod.ARCHAIC_FIX;
import static com.falsepattern.lumi.internal.mixin.plugin.TargetedMod.FASTCRAFT;
import static com.falsepattern.lumi.internal.mixin.plugin.TargetedMod.JOURNEYMAP;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // region Lighting Hooks
    common_AnvilChunkLoaderMixin(COMMON, always(), "AnvilChunkLoaderMixin"),
    common_ChunkMixin(COMMON, always(), "ChunkMixin"),
    common_ChunkProviderServerMixin(COMMON, always(), "ChunkProviderServerMixin"),
    common_S21PacketChunkDataMixin(COMMON, always(), "S21PacketChunkDataMixin"),
    common_WorldMixin(COMMON, always(), "WorldMixin"),

    client_MinecraftMixin(CLIENT, always(), "MinecraftMixin"),
    client_ChunkMixin(CLIENT, always(), "ChunkMixin"),
    client_ChunkCacheMixin(CLIENT, always(), "ChunkCacheMixin"),
    client_WorldMixin(CLIENT, always(), "WorldMixin"),
    // endregion

    // region Fixes & Optimizations
    common_ExtendedBlockStorageMixin(COMMON, always(), "ExtendedBlockStorageMixin"),
    common_NibbleArrayMixin(COMMON, always(), "NibbleArrayMixin"),
    common_MixinLongHashMap(COMMON, avoid(ARCHAIC_FIX), "LongHashMapMixin"),
    // endregion

    // region LUMI Initialization
    common_init_LumiChunkInitHookImplMixinMixin(COMMON, always(), "init.LumiChunkInitHookImplMixin"),
    common_init_LumiChunkInitTaskQueueImplMixin(COMMON, always(), "init.LumiChunkInitTaskQueueImplMixin"),
    common_init_LumiExtendedBlockStorageInitHookImplMixin(COMMON, always(), "init.LumiExtendedBlockStorageInitHookImplMixin"),
    common_init_LumiWorldInitHookImplMixin(COMMON, always(), "init.LumiWorldInitHookImplMixin"),
    common_init_LumiChunkCacheHookImplMixin(COMMON, always(), "init.LumiChunkCacheHookImplMixin"),

    client_init_LumiWorldInitHookImplMixinMixin(CLIENT, always(), "init.LumiWorldInitHookImplMixin"),
    // endregion

    // region LUMI Implementation
    common_lumi_LumiWorldImplMixin(COMMON, always(), "lumi.LumiWorldImplMixin"),
    common_lumi_LumiWorldRootImplMixin(COMMON, always(), "lumi.LumiWorldRootImplMixin"),

    common_lumi_LumiChunkImplMixin(COMMON, always(), "lumi.LumiChunkImplMixin"),
    common_lumi_LumiChunkRootImplMixin(COMMON, always(), "lumi.LumiChunkRootImplMixin"),

    common_lumi_LumiSubChunkImplMixin(COMMON, always(), "lumi.LumiSubChunkImplMixin"),
    common_lumi_LumiSubChunkRootImplMixin(COMMON, always(), "lumi.LumiSubChunkRootImplMixin"),

    common_lumi_LumiBlockCacheImplMixin(COMMON, always(), "lumi.LumiBlockCacheImplMixin"),
    common_lumi_LumiBlockCacheRootImplMixin(COMMON, always(), "lumi.LumiBlockCacheRootImplMixin"),
    // endregion

    // region Phosphor Implementation
    client_phosphor_PhosphorChunkImplMixin(COMMON, always(), "phosphor.PhosphorChunkImplMixin"),
    // endregion

    //region FastCraft Compat
    common_fastcraft_ChunkMixin(COMMON, require(FASTCRAFT), "fastcraft.ChunkMixin"),
    common_fastcraft_ChunkProviderServerMixin(COMMON, require(FASTCRAFT), "fastcraft.ChunkProviderServerMixin"),
    common_fastcraft_EntityPlayerMPMixin(COMMON, require(FASTCRAFT), "fastcraft.EntityPlayerMPMixin"),
    common_fastcraft_PlayerManagerMixin(COMMON, require(FASTCRAFT), "fastcraft.PlayerManagerMixin"),
    common_fastcraft_WorldMixin(COMMON, require(FASTCRAFT), "fastcraft.WorldMixin"),
    //endregion FastCraft Compat

    //region JourneyMap Compat
    client_journeymap_ForgeHelper_1_7_10Mixin(CLIENT, require(JOURNEYMAP), "journeymap.ForgeHelper_1_7_10Mixin"),
    //endregion JourneyMap Compat
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

