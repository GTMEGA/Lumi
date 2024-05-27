package com.falsepattern.lumina.internal.asm;

import com.falsepattern.lib.turboasm.MergeableTurboTransformer;

import java.util.Arrays;

public class LuminaClassTransformer extends MergeableTurboTransformer {
    public LuminaClassTransformer() {
        super(Arrays.asList(new PhosphorDataInjector()));
    }
}
