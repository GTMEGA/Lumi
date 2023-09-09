/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.init;

import com.falsepattern.lumina.api.init.LumiSubChunkBaseInit;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiSubChunkBaseInitImplMixin implements LumiSubChunkBaseInit {
    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiChunkBaseInit(CallbackInfo ci) {
        lumi$subChunkBaseInit();
    }

    @Override
    public void lumi$subChunkBaseInit() {
    }
}
