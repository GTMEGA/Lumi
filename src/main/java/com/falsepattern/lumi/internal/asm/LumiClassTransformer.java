package com.falsepattern.lumi.internal.asm;

import com.falsepattern.lib.turboasm.MergeableTurboTransformer;

import java.util.Arrays;

public class LumiClassTransformer extends MergeableTurboTransformer {
    public LumiClassTransformer() {
        super(Arrays.asList(new PhosphorDataInjector()));
    }
}
