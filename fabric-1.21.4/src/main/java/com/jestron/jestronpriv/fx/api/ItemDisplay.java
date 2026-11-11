package com.jestron.jestronpriv.fx.api;

public final class ItemDisplay extends Display {
    private final ItemStack stack;

    public ItemDisplay(ItemStack stack, Transformation t, long now) {
        super(t, now);
        this.stack = stack;
    }

    public ItemStack stack() { return stack; }
}
