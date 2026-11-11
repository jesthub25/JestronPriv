package com.jestron.jestronpriv.fx.api;

public final class TextDisplay extends Display {
    private volatile String text;
    private final TextStyle style;

    public TextDisplay(String text, Transformation t, long now) {
        this(text, null, t, now);
    }

    public TextDisplay(String text, TextStyle style, Transformation t, long now) {
        super(t, now);
        this.text = text == null ? "" : text;
        this.style = style;
    }

    /** '§' renk kodlu, satirlar '\n' ile ayrilmis. */
    public String text() { return text; }

    /** Daktilo animasyonu gibi durumlar icin yaziyi degistirir. */
    public void setText(String text) { this.text = text == null ? "" : text; }

    /** null ise eski (mezar tasi) yazi cizimi kullanilir. */
    public TextStyle style() { return style; }
}
