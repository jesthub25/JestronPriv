package com.jestron.jestronpriv.fx.api;

public final class BlockDisplay extends Display {
    private final Material block;

    public BlockDisplay(Material block, Transformation t, long now) {
        super(t, now);
        this.block = block;
    }

    public Material block() { return block; }
}
