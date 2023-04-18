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
    common_lighting_MixinAnvilChunkLoader(Side.COMMON, always(), "lighting.MixinAnvilChunkLoader"),
    common_lighting_MixinChunk(Side.COMMON, always(), "lighting.MixinChunk"),
    common_lighting_MixinChunkProviderServer(Side.COMMON, always(), "lighting.MixinChunkProviderServer"),
    common_lighting_MixinChunkVanilla(Side.COMMON, always(), "lighting.MixinChunkVanilla"),
    common_lighting_MixinExtendedBlockStorage(Side.COMMON, always(), "lighting.MixinExtendedBlockStorage"),
    common_lighting_MixinSPacketChunkData(Side.COMMON, always(), "lighting.MixinSPacketChunkData"),
    common_lighting_MixinWorld(Side.COMMON, always(), "lighting.MixinWorld_Lighting"),
    client_lighting_MixinMinecraft(Side.CLIENT, always(), "lighting.MixinMinecraft"),
    client_lighting_MixinWorld(Side.CLIENT, always(), "lighting.MixinWorld"),
    client_lighting_MixinChunkCache(Side.CLIENT, always(), "lighting.MixinChunkCache"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

