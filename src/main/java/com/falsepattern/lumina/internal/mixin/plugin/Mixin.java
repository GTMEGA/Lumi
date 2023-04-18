/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    common_MixinAnvilChunkLoader(Side.COMMON, always(), "MixinAnvilChunkLoader"),
    common_MixinChunk(Side.COMMON, always(), "MixinChunk"),
    common_MixinChunkProviderServer(Side.COMMON, always(), "MixinChunkProviderServer"),
    common_MixinChunkVanilla(Side.COMMON, always(), "MixinChunkVanilla"),
    common_MixinExtendedBlockStorage(Side.COMMON, always(), "MixinExtendedBlockStorage"),
    common_MixinSPacketChunkData(Side.COMMON, always(), "MixinSPacketChunkData"),
    common_MixinWorld(Side.COMMON, always(), "MixinWorld"),
    client_MixinMinecraft(Side.CLIENT, always(), "MixinMinecraft"),
    client_MixinWorld(Side.CLIENT, always(), "MixinWorld"),
    client_MixinChunkCache(Side.CLIENT, always(), "MixinChunkCache"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

