package com.jestron.jestronpriv.hitfx;

import java.util.Locale;

/** Vurus / kritik partikul stilleri. VANILLA = oyunun kendi partikulu (hicbir sey eklenmez). */
public enum HitStyle {
    VANILLA("Vanilla", "Oyunun kendi partikülü (değişiklik yok)."),
    KOPUK("Yeşil Köpük", "Yeşil köpük baloncukları fışkırır, yükselip patlar."),
    KELEBEK("Kelebekler", "Rengârenk kelebekler kanat çırparak uçup gider."),
    KALP("Kalpler", "Pembe-kırmızı kalpler patlar ve süzülür."),
    ISILTI("Yıldız Işıltısı", "Altın ışıltılar parlayıp göz kırpar."),
    YILDIZ("Yıldızlar", "Dönen sarı yıldızlar her yöne saçılır."),
    ALEV("Alev", "Turuncu alevler ve kıvılcımlar yükselir."),
    RUH("Ruh Ateşi", "Mavi ruh alevleri ve soluk ışık."),
    KAR("Kar Taneleri", "Dönen kar taneleri yavaşça süzülür."),
    ELMAS("Elmas Parçaları", "Parlak elmas kırıkları saçılıp düşer."),
    KAN("Kızıl Damla", "Kırmızı damlalar sıçrar ve düşer."),
    SIMSEK("Şimşek", "Sarı şimşekler çakar, kıvılcımlar fırlar."),
    SAKURA("Sakura", "Pembe kiraz yaprakları dans ederek süzülür."),
    YAPRAK("Yapraklar", "Yeşil yapraklar dönerek savrulur."),
    NOTA("Notalar", "Gökkuşağı renkli notalar yükselir."),
    GOKKUSAGI("Gökkuşağı", "Her renkten parlak küreler patlar."),
    ENDER("Ender", "Mor ender küreleri sarmal çizer."),
    PARA("Altın Paralar", "Dönen altın paralar fırlayıp düşer."),
    KAFATASI("Kafatası", "Kafatası ve kara duman yükselir."),
    SOK("Şok Dalgası", "Genişleyen ışık halkası ve kıvılcımlar."),
    NEON("Neon", "Camgöbeği-pembe neon çizgiler."),
    KONFETI("Konfeti", "Rengârenk konfetiler dönerek yağar."),
    TUY("Melek Tüyleri", "Bembeyaz tüyler sallanarak süzülür, ışıltı bırakır."),
    HILAL("Ay Işığı", "Gümüş hilaller yükselir, yıldız tozu göz kırpar."),
    PAPATYA("Çiçek Bahçesi", "Rengârenk papatyalar açılıp dönerek düşer."),
    BUZ("Buz Kırığı", "Keskin buz kırıkları fırlar, soğuk halka yayılır."),
    GIRDAP("Girdap", "Dönen girdaplar ve sarmal çizen mavi küreler."),
    TAC("Kral Tacı", "Altın taçlar yükselir, paralar ve ışıltılar saçılır."),
    RUN("Rün Büyüsü", "Büyü çemberi dönerek açılır, rün ışıkları yükselir."),
    YARASA("Yarasalar", "Kara yarasalar kanat çırparak dağılır."),
    PATI("Pati İzleri", "Sevimli pati izleri belirip söner."),
    YONCA("Şanslı Yonca", "Dört yapraklı yoncalar ve altın parıltı."),
    KESIK("Kılıç İzi", "Havada parlak kılıç kesikleri ve kıvılcımlar."),
    VOLKAN("Volkan", "Lav damlaları fışkırır, kara duman ve alev."),
    ZEHIR("Zehir", "Zehirli yeşil kabarcıklar ve sis."),
    GALAKSI("Galaksi", "Mor-mavi yıldızlar sarmal çizer, uzay tozu parlar."),
    KARADELIK("Kara Delik", "Her şey merkeze çekilir, mor ışık çöker."),
    BALON("Balonlar", "Renkli balonlar yavaşça yükselip patlar."),
    ZUMRUT("Zümrüt", "Yeşil zümrüt kırıkları dönerek saçılır."),
    EJDERATESI("Ejder Ateşi", "Mor-pembe ejder alevleri ve kıvılcımlar."),
    BUYU("Büyü Tozu", "Pembe-camgöbeği sihir tozu bulutu."),
    PATLAMA("Patlama", "Turuncu şok halkası, duman ve kıvılcım yağmuru."),
    MUREKKEP("Mürekkep", "Koyu mürekkep damlaları sıçrar, lekeli duman bırakır."),
    FISEK("Havai Fişek", "Yukarı fırlayan ışık izleri, renkli patlama ve göz kırpan yıldızlar."),
    PIKSEL("Piksel", "8-bit kare pikseller saçılır ve zıplayarak düşer."),
    OKYANUS("Okyanus", "Su damlaları sıçrar, köpük baloncukları ve dalga halkası."),
    KUM("Kum Fırtınası", "Kum tanecikleri hedefin etrafında döner, toz bulutu."),
    PLAZMA("Plazma", "Camgöbeği-pembe plazma küreleri sarmal çizer, enerji izleri."),
    GOLGE("Gölge", "Kara duman ve mor gölge küreleri yükselir, karanlık halka."),
    SEKER("Şeker", "Pastel yıldızlar, kalpler ve şekerli konfeti."),
    ATES_BOCEGI("Ateş Böcekleri", "Sarı-yeşil ateş böcekleri dağılır, yanıp sönerek süzülür.");

    public final String label;
    public final String description;

    HitStyle(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static HitStyle parse(String s) {
        if (s != null) {
            String n = s.trim().toUpperCase(Locale.ROOT);
            for (HitStyle h : values()) if (h.name().equals(n)) return h;
        }
        return VANILLA;
    }
}
