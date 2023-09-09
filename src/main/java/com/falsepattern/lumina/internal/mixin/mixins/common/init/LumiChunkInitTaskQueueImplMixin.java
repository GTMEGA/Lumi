/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.init;

import com.falsepattern.lumina.api.init.LumiChunkInitTaskQueue;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Unique
@Mixin(Chunk.class)
public abstract class LumiChunkInitTaskQueueImplMixin implements LumiChunkInitTaskQueue {
    private List<Runnable> lumina$taskQueue;

    @Inject(method = "onChunkLoad",
            at = @At(value = "INVOKE",
                     target = "Lcpw/mods/fml/common/eventhandler/EventBus;post(Lcpw/mods/fml/common/eventhandler/Event;)Z",
                     remap = false),
            require = 1)
    private void onLoad(CallbackInfo ci) {
        lumi$executeInitTasks();
    }

    @Override
    public void lumi$addInitTask(Runnable task) {
        if (lumina$taskQueue == null)
            lumina$taskQueue = new ArrayList<>();
        lumina$taskQueue.add(task);
    }

    @Override
    public void lumi$executeInitTasks() {
        if (lumina$taskQueue == null)
            return;
        lumina$taskQueue.forEach(Runnable::run);
        lumina$taskQueue.clear();
    }
}
