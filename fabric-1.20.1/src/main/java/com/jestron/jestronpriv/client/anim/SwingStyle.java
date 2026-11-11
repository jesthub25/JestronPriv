package com.jestron.jestronpriv.client.anim;

import java.util.Locale;

/** Birinci sahis vurus (el sallama) stilleri. */
public enum SwingStyle {
    SWIPE("Savurma", "Kılıcı yandan savurur."),
    DOWN("Aşağı Darbe", "Yukarıdan aşağı sert darbe."),
    SMOOTH("Yumuşak", "Vanilla'ya yakın, akıcı vuruş."),
    SMOOTH_2("Yumuşak 2", "Sade, öne doğru yumuşak vuruş."),
    POWER("Güç", "Geriye çekip güçlü savurma."),
    FEAST("Ziyafet", "Geniş açılı yatay savurma."),
    TWIST("Burgu", "Bilekten burgulu vuruş."),
    DEFAULT("Klasik", "Eli aşağı yukarı indiren sade vuruş."),
    SLASH("Çapraz Kesik", "Sağ üstten sol alta çapraz kılıç kesiği."),
    BACKHAND("Ters Savurma", "Kılıcı içeriden dışarı ters savurur."),
    OVERHEAD("Ağır Darbe", "Kılıcı kaldırıp tepeden indirir."),
    STAB("Saplama", "Kılıcı ileri doğru saplar."),
    SPIN("Çevirme", "Vuruşta kılıcı elinde tam tur döndürür."),
    BLOCKHIT("1.7 Blok-Vuruş", "Eski PvP tarzı: kılıç blok pozundayken vurur."),
    KNIGHT("Şövalye", "Kılıcı omuzdan kaldırıp ağır, çapraz bir kesikle indirir."),
    KATANA("Katana", "Iaido: bıçak yatay, sağdan sola tek hızlı kesik ve kan silkeleme."),
    XSLASH("Çift Kesik (X)", "Tek vuruşta iki çapraz kesik: önce sağdan, sonra soldan."),
    LUNGE("Eskrim Hamlesi", "Kılıcı geri çekip ileri doğru hızlı, burgulu bir hamle."),
    SMASH("Yer Sarsan", "Tepeden ağır darbe; çarpınca el sarsılarak toparlanır."),
    FLIP("Takla (Sinematik)", "Vururken eşya bilekten öne doğru tam tur takla atar."),
    TWIRL("Pervane (Sinematik)", "Eşya ekranın ortasında iki tur fırıl fırıl döner, sonra keser."),
    REVERSE("Ters Tutuş (Sinematik)", "Eşyayı ters tutuşa çevirip aşağı saplar, sonra geri döndürür."),
    KARAMBIT("Bıçak Gösterisi (Sinematik)", "Eşya kendi ekseninde iki tur döner; CS bıçak gösterisi gibi."),
    DRAGON("Ejder Kombosu (Sinematik)", "Geriye kurulur, tam tur savrulur ve tepeden bitirir."),
    CRESCENT("Hilal Kesik", "Sağ üstten geniş bir hilal çizerek sol alta iner, takip hareketiyle biter."),
    UPPERCUT("Yükselen Kesik", "Sol alttan sağ üste doğru yükselen sert kesik."),
    CROSS("Haç Kesiği", "Önce tepeden dikey, ardından yatay: havada artı çizer."),
    DOUBLESTAB("Çifte Saplama", "İleri doğru art arda iki hızlı saplama."),
    EXECUTION("İnfaz", "Kılıcı tepeye kaldırır, bir an bekler ve sarsıntıyla tepeden indirir."),
    RONIN("Ronin Çekişi", "Kınından çeker gibi kalçadan yukarı çapraz çekiş ve bilek silkmesi."),
    PHANTOM("Hayalet Adım", "Yana çekilip şimşek hızında karşıya geçen kesik, hafif taşma ile durur."),
    WHIRLWIND("Kasırga (Sinematik)", "Yatay savururken eşya ekran düzleminde tam tur döner."),
    TORNADO("Hortum (Sinematik)", "İleri hamle yaparken eşya kendi ekseninde üç tur burgulanır."),
    MOONLIGHT("Ay Işığı (Sinematik)", "Eşya geriye doğru tam takla atar ve tepeden kesikle biter."),
    ZIGZAG("Zikzak", "Art arda üç hızlı kesik: sağdan sola, soldan sağa, tekrar sola."),
    HAMMER("Balyoz", "Omuzdan yukarı kaldırır, bekler ve sarsıntıyla yere vurur gibi indirir."),
    RIPOSTE("Eskrim Riposte", "Önce çapraz savuşturma, ardından ileri doğru hızlı saplama."),
    SICKLE("Orak", "Dışarı açılıp içe doğru kancalayarak süpürür, sonra geri çeker."),
    AXE("Balta", "Sağ omuzdan sol alta ağır çapraz balta darbesi ve takip."),
    PUNCH("Yumruk", "Eşyayı geri çekip düz bir yumruk gibi ileri fırlatır."),
    TRIPLESTAB("Üçlü Saplama", "İleri doğru art arda üç hızlı saplama."),
    SCYTHE("Tırpan (Sinematik)", "Geniş yatay biçme; eşya ters yönde tam tur döner."),
    BOOMERANG("Bumerang (Sinematik)", "Eşya dönerek ileri fırlar, iki tur atıp ele geri döner."),
    WINDMILL("Yel Değirmeni (Sinematik)", "Eşya öne doğru iki tam takla atarak iner."),
    VORTEX("Girdap Kesiği (Sinematik)", "Bıçak ekseninde tam tur dönerken çapraz keser."),
    CLEAVE("Yarma", "Çok hızlı dik yarma: tepeden aşağı tek, sert darbe."),
    SWEEP("Süpürme", "Bel hizasında sağdan sola alçak yatay süpürme."),
    RISINGDRAGON("Yükselen Ejder (Sinematik)", "Aşağıdan yukarı keser, tepede tam tur döner ve iner."),
    FLOURISH("Kılıç Gösterisi (Sinematik)", "Bıçak ekseninde tam tur gösteri, sonra ileri doğrultup durur."),
    GUARD("Savunma Kesiği", "Kılıcı gövdenin önünde yatay korumaya alır, sonra dışarı keser.");

    public final String label;
    public final String description;

    /** Esya elde tam tur donen sinematik stil mi (secim ekraninda isaretlenir). */
    public boolean cinematic() {
        return switch (this) {
            case SPIN, FLIP, TWIRL, REVERSE, KARAMBIT, DRAGON, WHIRLWIND, TORNADO, MOONLIGHT, SCYTHE, BOOMERANG, WINDMILL, VORTEX, RISINGDRAGON,
                 FLOURISH -> true;
            default -> false;
        };
    }

    SwingStyle(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public SwingStyle next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SwingStyle previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static SwingStyle parse(String s) {
        if (s == null) return SWIPE;
        String n = s.trim().toUpperCase(Locale.ROOT);
        for (SwingStyle st : values()) if (st.name().equals(n)) return st;
        return SWIPE;
    }
}
