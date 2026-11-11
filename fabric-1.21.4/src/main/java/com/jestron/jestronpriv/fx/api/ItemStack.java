package com.jestron.jestronpriv.fx.api;

/** Gosterilecek esya. "profile" istemci katmaninin oyuncu kafasi icin tasidigi opak nesnedir. */
public final class ItemStack implements Cloneable {
    private final Material type;
    private final Object profile;

    public ItemStack(Material type) {
        this(type, null);
    }

    public ItemStack(Material type, Object profile) {
        this.type = type;
        this.profile = profile;
    }

    public Material getType() { return type; }
    public Object profile() { return profile; }

    @Override
    public ItemStack clone() {
        return new ItemStack(type, profile);
    }
}
