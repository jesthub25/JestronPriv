package com.jestron.jestronpriv.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jestron.jestronpriv.client.anim.SwingStyle;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * config/jestronpriv.json — tum ayarlar tek dosyada.
 * Eski surumden gelen dosyalarda olmayan alanlar varsayilan degerlerini korur.
 */
public final class ModConfig {

    public static final String NONE = "yok";
    public static final String RANDOM = "rastgele";

    // ------------------------------------------------------------------ oldurme efektleri
    /** Secili efekt id'si, "yok" veya "rastgele". */
    public String effect = "elmas_kilic";
    /** KONUM / EKRAN / IKISI */
    public String mode = "KONUM";
    /** Mob oldurunce de calissin mi. */
    public boolean mobKills = false;
    /** Oldurme algilaninca sohbete hangi sinyalle algilandigini yaz. */
    public boolean killDebug = false;
    /** Sik dogrulama: zayif sinyaller (aninda silinme, kimin oldurdugunu soylemeyen mesaj) tek basina efekt cikarmaz. */
    public boolean killStrict = true;
    /** Ses carpani 0.0 - 2.0 */
    public float volume = 1.0f;
    /** Ekran modunda efektin onunde kac blok ileride oynayacagi. */
    public double screenDistance = 4.5;
    /** Ekran modunda baslik gosterilsin mi. */
    public boolean screenTitle = true;
    /** Ayni anda en fazla kac efekt. */
    public int maxSessions = 12;
    /** Efekt seslerinin calacagi oyun ses kanali: MASTER (Ana Ses), PLAYERS (Oyuncular), AMBIENT (Ortam). */
    public String soundChannel = "MASTER";
    /** Gercekci ses: katmanli carpma/enkaz sesleri, dogal perde farki, uzak mesafede ses gecikmesi. */
    public boolean soundRealism = true;

    // ------------------------------------------------------------------ vurus animasyonu
    public boolean swingEnabled = true;
    public String swingStyle = SwingStyle.SWIPE.name();
    /** Hareket genligi 0.5 - 3.0 */
    public float swingStrength = 1.0f;
    /** Sure carpani 0.5 (hizli) - 4.0 (yavas) */
    public float swingDuration = 1.0f;
    /** Sadece sallarken ozel poz; dururken vanilla poz. */
    public boolean swingOnlyWhileSwinging = false;
    /** Esya degistirme / bekleme suresi inisi. */
    public boolean swingEquipDip = true;
    /** Vurustan sonraki saldiri bekleme dalisi (vanilla 1.9+ hissi). Kapaliyken stil temiz oynar. */
    public boolean swingCooldownDip = false;

    // ------------------------------------------------------------------ el gorunumu (viewmodel)
    /** El gorunumu ayarlari etkin mi (hepsi varsayilanda iken vanilla ile ayni). */
    public boolean vmEnabled = true;
    /** Kol konumu (blok): sag(+)/sol, yukari(+)/asagi, geri(+)/ileri. -0.6 - 0.6 */
    public float vmHandX = 0f, vmHandY = 0f, vmHandZ = 0f;
    /** Esya konumu (blok). -0.6 - 0.6 */
    public float vmItemX = 0f, vmItemY = 0f, vmItemZ = 0f;
    /** Esya boyutu 0.3 - 2.0 */
    public float vmItemScale = 1f;
    /** Esya donusu (derece) -180 - 180 */
    public float vmRotX = 0f, vmRotY = 0f, vmRotZ = 0f;
    /** Sol ele de (aynalanarak) uygula. */
    public boolean vmOffhand = true;
    /** Sol eldeki esyayi gizle (kullanirken gorunur). */
    public boolean vmHideOffhand = false;
    /** El cizim FOV'u 30 - 110 (vanilla 70). */
    public float vmHandFov = 70f;
    /** Dururken hafif nefes salinimi. */
    public boolean vmIdleSway = false;

    // ------------------------------------------------------------------ ekran ve renk
    public boolean cgEnabled = false;
    public String cgPreset = "YOK";
    /** 0 - 2 */
    public float cgSaturation = 1f;
    /** 0.5 - 1.5 */
    public float cgBrightness = 1f;
    /** 0.5 - 1.5 */
    public float cgContrast = 1f;
    /** -1 (soguk) - 1 (sicak) */
    public float cgTemperature = 0f;
    /** KAPALI / R4_3 / R5_4 / R3_2 / R16_10 / R1_1 / OZEL */
    public String aspect = "KAPALI";
    /** Ozel oran 0.8 - 2.4 */
    public float aspectCustom = 1.333f;
    /** Ates katmanini asagi kaydirma 0 - 0.45 */
    public float fireHeight = 0f;
    /** Ates katmani opakligi 0.2 - 1 */
    public float fireOpacity = 1f;
    /** Yakinlastirma (C basili tut). */
    public boolean zoomEnabled = true;
    /** Yakinlik 1.5 - 15 */
    public float zoomFactor = 4f;
    public boolean zoomSmooth = true;
    /** Basiliyken tekerlekle yakinlik ayari. */
    public boolean zoomScroll = true;

    // ------------------------------------------------------------------ vurus partikulleri
    /** Normal vurus stili (HitStyle adi; VANILLA = degisiklik yok). */
    public String hitStyle = "VANILLA";
    /** Kritik vurus stili. */
    public String critStyle = "VANILLA";
    /** Adet carpani 0.25 - 2.5 */
    public float hitAmount = 1f;
    /** Boyut carpani 0.5 - 2 */
    public float hitSize = 1f;
    /** Ozel stil seciliyken vanilla kritik / buyu yildizlarini gizle. */
    public boolean hitHideVanilla = true;
    /** Moblara vurunca da (kapaliysa sadece oyuncular). */
    public boolean hitOnMobs = true;
    /** Stile ozel vurus / kritik sesi. */
    public boolean hitSound = true;
    /** Vurus sesi seviyesi 0 - 2 */
    public float hitSoundVolume = 1f;

    // ------------------------------------------------------------------ pvp hissi
    /** Moblara vurunca da hasar sayisi / kombo (kapaliysa sadece oyuncular). */
    public boolean pvpMobs = true;
    /** Uzayda ucan hasar sayilari. */
    public boolean dmgNumbers = true;
    /** "KALP" (-3.5 ❤) ya da "CAN" (-7). */
    public String dmgFormat = "KALP";
    /** Hasar yazisi boyutu 0.5 - 2 */
    public float dmgScale = 1f;
    /** Hasar miktarina gore renk (beyaz > sari > turuncu > kirmizi). */
    public boolean dmgColorByAmount = true;
    public int dmgColor = 0xFFFFFF;
    public int dmgCritColor = 0xFFC830;
    /** Hasar okunamazsa (zirh / kalkan gizli) "✔" isareti. */
    public boolean dmgUnknownMarker = true;
    /** Kombo sayaci. */
    public boolean combo = true;
    /** "ALT", "UST", "SAG" */
    public String comboPos = "ALT";
    /** 0.5 - 2 */
    public float comboScale = 1f;
    /** Kombo zaman asimi (sn) 1 - 6 */
    public float comboTimeout = 3f;
    public boolean comboSound = true;
    /** 0 - 2 */
    public float comboVolume = 1f;
    /** Seri oldurme duyurusu. */
    public boolean killStreak = true;
    public boolean streakSound = true;
    /** 0 - 2 */
    public float streakVolume = 1f;
    /** Kenar parlamasi. */
    public boolean streakFlash = true;
    /** "TR" ya da "EN" */
    public String streakLang = "TR";
    /** Coklu oldurme penceresi (sn) 2 - 10 */
    public float streakWindow = 5f;
    /** Vurulan hedefin hasar rengi (vanilla kirmizi yerine). */
    public boolean hitColor = false;
    public int hitColorRgb = 0x8A2BE2;
    public boolean hitColorRainbow = false;
    /** Sadece benim vurduklarim degil, hasar alan herkes. */
    public boolean hitColorAll = false;
    /** Renk yogunlugu 0.2 - 0.9 (vanilla kirmizi 0.3) */
    public float hitColorAlpha = 0.5f;

    // ------------------------------------------------------------------ nisangah
    public boolean chEnabled = false;
    public String chShape = "ARTI";
    /** Kol uzunlugu 1 - 16 */
    public float chSize = 5f;
    /** Kalinlik 1 - 4 */
    public float chThickness = 1f;
    /** Bosluk 0 - 10 */
    public float chGap = 2f;
    public int chColor = 0xFFFFFF;
    public boolean chRainbow = false;
    public boolean chOutline = true;
    /** 0.1 - 1 */
    public float chOpacity = 1f;
    public boolean chDot = false;
    public boolean chDynamic = true;
    public boolean chHitMarker = true;

    // ------------------------------------------------------------------ totem
    public boolean totemFx = true;
    public boolean totemHideVanilla = false;
    public boolean totemHideScreen = false;
    public boolean totemCounter = true;
    public boolean totemChat = false;
    public boolean totemSound = true;
    /** 0.5 - 2 */
    public float totemScale = 1f;
    // ozel totem sesi (cihazdan secilen dosya; config/jestronpriv/sounds)
    public boolean totemCustom = false;
    public String totemSoundFile = "";
    /** 0 - 2 (1 = orijinal) */
    public float totemSoundVolume = 1f;
    /** dosyada baslangic (sn) 0 - 180 */
    public float totemSoundStart = 0f;
    /** calma suresi (sn) 0 = sonuna kadar, en cok 60 */
    public float totemSoundLength = 0f;
    /** giris / cikis yumusatma (sn) */
    public float totemSoundFadeIn = 0.02f;
    public float totemSoundFadeOut = 0.3f;
    /** hiz / perde 0.5 - 2 */
    public float totemSoundSpeed = 1f;
    /** "HERKES", "BEN", "DIGER" */
    public String totemSoundWho = "HERKES";
    public boolean totemSoundMuteVanilla = true;
    public boolean totemSoundOverlap = false;

    // ------------------------------------------------------------------ zaman / hava (sadece istemci)
    /** "SUNUCU", "SABIT", "DONGU" */
    public String timeMode = "SUNUCU";
    /** 0 - 24000 */
    public int timeValue = 6000;
    /** Dongu hizi carpani 1 - 200 */
    public float timeSpeed = 10f;
    /** "SUNUCU", "ACIK", "YAGMUR", "FIRTINA" */
    public String weather = "SUNUCU";

    // ------------------------------------------------------------------ HUD
    /** F3 ekrani acikken HUD ogelerini gizle. */
    public boolean hudHideF3 = true;
    public boolean hudShadow = true;
    // iksir paneli
    public boolean potHud = true;
    public float potX = 0f, potY = 0.32f, potScale = 1f;
    /** Arka plan opakligi 0 - 0.9 */
    public float potBg = 0.35f;
    public boolean potBar = true, potNames = true, potCompact = false, potHideVanilla = true, potAmbient = true;
    /** Bu kadar saniye kalinca yanip soner (0 = kapali) 0 - 30 */
    public int potWarn = 10;
    /** "ETKI" (etkinin rengi), "TUR" (iyi yesil / kotu kirmizi), "BEYAZ" */
    public String potColor = "ETKI";
    /** "SURE", "TUR", "AD" */
    public String potSort = "SURE";
    // esya sayaci
    public boolean icHud = true;
    public float icX = 1f, icY = 0.5f, icScale = 1f;
    public float icBg = 0.35f;
    public boolean icVertical = true, icHideZero = false, icWarn = true, icCooldown = true;
    /** Gosterilen esyalar (bit maskesi, ItemHud.ITEMS sirasi). */
    public int icMask = 0b11111;
    // bilgi satiri
    public boolean infoHud = true;
    public float infoX = 0f, infoY = 0f, infoScale = 1f;
    public float infoBg = 0.35f;
    /** true: tek satir, false: alt alta */
    public boolean infoRow = false;
    /** Gosterilen bilgiler (bit maskesi, InfoHud.FIELDS sirasi). */
    public int infoMask = 0b1111;
    public int infoLabelColor = 0xB36BFF, infoValueColor = 0xFFFFFF;
    /** FPS / ping degerini iyi-orta-kotu renklendir. */
    public boolean infoQualityColor = true;
    /** Koordinat ondalik basamagi 0 - 2 */
    public int infoDecimals = 0;

    // kombo / seri duyurusu konumu (HUD duzenleyici)
    public float comboX = 0.5f, comboY = 0.64f;
    public float streakX = 0.5f, streakY = 0.18f, streakScale = 1f;
    // envanter
    public boolean invHud = true;
    public float invX = 1f, invY = 1f, invScale = 0.8f;
    public float invBg = 0.4f;
    public boolean invHotbar = false, invArmor = true, invHideEmpty = true, invHideInScreens = true;
    // alinan / atilan esyalar
    public boolean pkHud = true;
    public float pkX = 1f, pkY = 0.78f, pkScale = 1f;
    public float pkBg = 0.35f;
    public boolean pkPickups = true, pkDrops = true, pkMerge = true, pkNewestBottom = true, pkRarity = true;
    /** Gosterim suresi (sn) 1 - 10 */
    public float pkDuration = 4f;
    /** En fazla satir 1 - 12 */
    public int pkMax = 6;

    // ------------------------------------------------------------------ PvP araclari
    // reach gostergesi
    public boolean reachHud = true;
    public float reachX = 0.5f, reachY = 0.44f, reachScale = 1f;
    public float reachBg = 0f;
    /** Ondalik basamak 1 - 3 */
    public int reachDecimals = 2;
    public boolean reachHistory = true, reachStats = false, reachColor = true, reachMobs = false;
    /** Son vurustan bu kadar sn sonra soner (0 = hep gorunur) 0 - 30 */
    public float reachFade = 5f;
    // ping
    public boolean pingHud = true;
    public float pingX = 1f, pingY = 0f, pingScale = 1f;
    public float pingBg = 0.35f;
    public boolean pingGraph = true, pingStats = true, pingBars = true, pingTarget = true, pingSpike = true;
    /** Olcum araligi (sn) 0.5 - 5 */
    public float pingInterval = 1f;
    // oldurme akisi
    public boolean kfHud = true;
    public float kfX = 1f, kfY = 0.16f, kfScale = 1f;
    public float kfBg = 0.35f;
    /** "HEPSI", "OYUNCU", "BENIM" */
    public String kfFilter = "HEPSI";
    /** En fazla satir 1 - 10 */
    public int kfMax = 5;
    /** Gosterim suresi (sn) 2 - 30 */
    public float kfDuration = 8f;
    public boolean kfIcons = true, kfWeapon = true, kfStreak = true, kfSelfName = true, kfNewestTop = true, kfHideChat = false;
    // totem bildirimleri
    public boolean tfHud = true;
    public float tfX = 0.5f, tfY = 0.27f, tfScale = 1f;
    public float tfBg = 0.35f;
    public boolean tfSelf = false, tfMobs = false, tfSound = true, tfHead = true;
    /** En fazla satir 1 - 8 */
    public int tfMax = 4;
    /** Gosterim suresi (sn) 2 - 30 */
    public float tfDuration = 6f;
    /** Bu mesafeden uzaktakiler bildirilmez (blok, 0 = sinirsiz) 0 - 256 */
    public int tfRange = 64;
    // hitbox
    public boolean hbEnabled = false;
    public boolean hbPlayers = true, hbMobs = false, hbSelf = false, hbEye = true, hbLook = true, hbFill = false, hbTarget = true, hbHurt = true;
    public int hbColor = 0xFFFFFF, hbTargetColor = 0xFF3B3B, hbHurtColor = 0xFF9A2E, hbEyeColor = 0xFF4040, hbLookColor = 0x4D8DFF;
    /** Cizgi kalinligi 0.5 - 4 */
    public float hbWidth = 1.5f;
    /** Cizgi opakligi 0.1 - 1 */
    public float hbAlpha = 0.9f;
    /** Dolgu opakligi 0.02 - 0.6 */
    public float hbFillAlpha = 0.12f;
    /** Gorus mesafesi (blok) 4 - 128 */
    public int hbRange = 48;

    // ------------------------------------------------------------------ yerdeki esyalar
    public boolean giEnabled = true;
    /** Menzil (blok) 4 - 48 */
    public float giRange = 16f;
    /** En fazla etiket 5 - 80 */
    public int giMax = 40;
    public boolean giName = true, giIcon = true, giCount = true, giRarity = true, giMerge = true;
    public boolean giHitbox = false;
    /** Sadece baktigin esya (nisangaha yakin) ya da cok yakindakiler. */
    public boolean giOnlyLooking = false;
    /** 0.5 - 2 */
    public float giScale = 1f;
    /** Etiket arka plani 0 - 0.8 */
    public float giBg = 0.25f;

    // ------------------------------------------------------------------ evcil yoldas
    /** Secili yoldas id'si veya "yok". */
    public String pet = "yok";
    /** 0.5 - 2 */
    public float petScale = 1f;
    /** "OMUZ", "YAN", "ARKA", "YORUNGE" */
    public String petMode = "YAN";
    public boolean petLeft = false;
    public boolean petCustomColor = false;
    public int petColor = 0x3DF0E0;
    public boolean petRainbow = false;
    public boolean petParticles = true;
    public boolean petFirstPerson = true;
    /** Takip hizi 0.3 - 3 */
    public float petFollow = 1f;
    /** Suzulme (yukari-asagi) 0 - 2 */
    public float petBob = 1f;
    /** Vurus / oldurmede sevinc hareketi. */
    public boolean petReact = true;
    /** Parlaklik 0.2 - 1.5 */
    public float petGlow = 1f;

    // ------------------------------------------------------------------ blok secim kutusu
    public boolean boEnabled = true;
    public int boColor = 0xFFFFFF;
    /** 0.1 - 1 */
    public float boAlpha = 0.85f;
    /** Cizgi kalinligi (piksel, 1080p) 1 - 10 */
    public float boWidth = 2.5f;
    public boolean boRainbow = false, boPulse = false;
    /** "YOK", "YUZ" (bakilan yuz), "TUM" */
    public String boFill = "YUZ";
    public boolean boFillSame = true;
    public int boFillColor = 0xFFFFFF;
    /** 0.03 - 0.6 */
    public float boFillAlpha = 0.12f;
    /** Kirarken dolgu ilerlemeyle buyur ve renk kirmizidan yesile doner. */
    public boolean boMining = true;

    // ------------------------------------------------------------------ FOV
    /** Acikken asagidaki olcekler vanilla "FOV etkileri" ayarinin yerine gecer. */
    public boolean fovCustom = false;
    /** 0 - 1.5 (1 = vanilla) */
    public float fovSprint = 1f, fovSpeed = 1f, fovFly = 1f, fovBow = 1f;
    /** 0 - 1 */
    public float fovWater = 1f;
    /** "ANINDA", "NORMAL", "YUMUSAK", "COK_YUMUSAK" */
    public String fovSmooth = "NORMAL";

    // ------------------------------------------------------------------ varlik ayiklama
    public boolean cullEnabled = true;
    public boolean cullEntities = true, cullBlockEntities = true;
    /** Oyunculari da ayikla (kapaliyken isim etiketleri duvar arkasinda gorunmeye devam eder). */
    public boolean cullPlayers = false;
    /** Ismi gorunen varliklari (isim etiketli moblar) ayiklama. */
    public boolean cullKeepNamed = true;
    /** "HIZLI", "DENGELI", "KALITE" */
    public String cullQuality = "DENGELI";

    // ------------------------------------------------------------------ ozel yazi efekti
    /** {kurban}, {ben} yer tutuculari; '&' renk kodlari; '|' satir sonu. */
    public String ctText = "&l{kurban} &fyok edildi!";
    public int ctFont = 0;
    /** 0 tek renk, 1 gradyan, 2 gokkusagi */
    public int ctColorMode = 1;
    public int ctColor1 = 0xFF4040;
    public int ctColor2 = 0xFFD700;
    public boolean ctBgOn = true;
    public int ctBgColor = 0x000000;
    public float ctBgAlpha = 0.45f;
    public boolean ctBold = true;
    public boolean ctItalic = false;
    public boolean ctUnderline = false;
    public boolean ctStrike = false;
    public boolean ctShadow = true;
    public boolean ctOutline = false;
    public int ctOutlineColor = 0x000000;
    public boolean ctGlow = true;
    public float ctSize = 1.4f;
    public float ctHeight = 2.4f;
    public String ctAnim = "POP";
    public String ctSound = "LEVELUP";
    public boolean ctParticles = true;

    // ------------------------------------------------------------------ kanatlar
    /** Secili animasyonlu kanat id'si veya "yok". */
    public String wing = "yok";
    /** Kanat boyutu 0.6 - 1.4 */
    public float wingScale = 1.0f;
    /** Kanat uclarindan parcacik. */
    public boolean wingParticles = true;
    // ------------------------------------------------------------------ serbest 3. sahis kamera
    /** F5 arka gorunumde kamera oyuncudan bagimsiz doner. */
    public boolean freeCam = true;
    /** Vurunca / kullaninca oyuncu kameranin nisan aldigi yere doner. */
    public boolean freeCamAim = true;
    /** Etkilesimden sonra nisanda kalma suresi (sn) 0.25 - 3 */
    public float freeCamAimTime = 1.0f;
    /** Oyuncunun gittigi yone donus hizi 0.1 - 1 */
    public float freeCamTurnSpeed = 0.45f;
    /** F5 dongusunde on gorunumu atla. */
    public boolean freeCamSkipFront = false;
    /** Elytra ile ucarken de serbest kamera (oyuncu yumusakca kameranin yonune doner). */
    public boolean freeCamElytra = true;
    /** Bas-bak: tus (varsayilan Sol Alt) basiliyken 3. sahista etrafa bak, birakinca geri don. */
    public boolean freeLook = true;
    /** Serbest kamerada ekranin ortasinda nisangah. */
    public boolean freeCamCrosshair = true;

    /** Eski (1.4.x) elytra gorunumu: sadece yukleme sirasinda kanada tasinir, sonra kaydedilmez. */
    public String elytraSkin = null;

    // ------------------------------------------------------------------ aura
    /** Secili aura id'si veya "yok". */
    public String aura = "yok";
    /** Aura yogunlugu 0.25 - 2.0 */
    public float auraDensity = 1.0f;
    /** Birinci sahista da goster (yuz hizasindaki parcaciklar her zaman gizlenir). */
    public boolean auraFirstPerson = true;
    /** Aura dagilma (yayilma) carpani 0.5 - 2.5: parcaciklarin oyuncudan yatay uzakligi. */
    public float auraSpread = 1.0f;
    /** Aura hiz carpani 0.25 - 3.0: donme, akis ve atis hizi. */
    public float auraSpeed = 1.0f;

    // ------------------------------------------------------------------ sapka
    /** Secili sapka id'si veya "yok". */
    public String hat = "yok";
    /** Boyut 0.5 - 2 */
    public float hatScale = 1f;
    /** Yukseklik ofseti (blok) -0.3 - 0.6 */
    public float hatHeight = 0f;
    /** One / arkaya egim (derece) -35 - 35 */
    public float hatTilt = 0f;
    /** Animasyon hizi 0 - 3 (0 = sabit) */
    public float hatSpeed = 1f;
    public boolean hatGlow = true;
    public boolean hatCustomColor = false;
    public int hatColor = 0x3DF0E0;
    public boolean hatRainbow = false;
    /** Kask takiliyken sapkayi gizle (kapaliysa kaskin ustune cikar). */
    public boolean hatHideHelmet = false;
    public boolean hatParticles = true;

    // ------------------------------------------------------------------ pelerin
    /** Secili pelerin deseni ya da "yok". */
    public String cape = "yok";
    /** Uzunluk 0.7 - 1.4, genislik 0.8 - 1.2 (vanilla = 1). */
    public float capeLength = 1f, capeWidth = 1f;
    /** Ruzgar dalgasi 0 - 2, fizik (kosu / ziplama tepkisi) 0 - 2, animasyon hizi 0 - 3. */
    public float capeWind = 1f, capePhysics = 1f, capeSpeed = 1f;
    public boolean capeGlow = true;
    /** Isilti gucu 0.2 - 1.5 */
    public float capeGlowStrength = 1f;
    /** Ton kaydirma -180 - 180 derece. */
    public float capeHue = 0f;
    public boolean capeRainbow = false;
    public float capeRainbowSpeed = 1f;
    /** Dis yuzde ince vurgu kenari. */
    public boolean capeTrim = true;
    /** Ic yuz rengi otomatik (kapaliysa capeInnerColor). */
    public boolean capeInnerAuto = true;
    public int capeInnerColor = 0x202020;
    public boolean capeParticles = true;
    /** Parcacik carpani 0 - 3 */
    public float capeParticleRate = 1f;
    /** JestronPriv kanatlari gorunurken pelerini gizle. */
    public boolean capeHideWings = true;

    // ------------------------------------------------------------------ elytra gorunumu
    /** Secili elytra deseni ya da "yok" (elytra giyiliyken vanilla dokusunun yerine). */
    public String ely = "yok";
    /** Animasyon hizi 0 - 3. */
    public float elySpeed = 1f;
    public boolean elyGlow = true;
    /** Isilti gucu 0.2 - 1.5 */
    public float elyGlowStrength = 1f;
    /** Ton kaydirma -180 - 180 derece. */
    public float elyHue = 0f;
    public boolean elyRainbow = false;
    public float elyRainbowSpeed = 1f;
    /** Dis yuzde ince vurgu kenari. */
    public boolean elyTrim = true;
    /** Ic yuz (sirta bakan) de desenli mi; kapaliysa koyu astar. */
    public boolean elyInnerSame = true;
    public boolean elyParticles = true;
    /** Parcacik carpani 0 - 3 */
    public float elyParticleRate = 1f;

    // ------------------------------------------------------------------ buyu parlamasi
    public boolean glintEnabled = false;
    /** TEK, IKI, AKAN (akan gokkusagi), DONEN (renk degistiren), VANILLA. */
    public String glintItemMode = "TEK";
    public int glintItemColor = 0x4FC3FF, glintItemColor2 = 0xFF4FD8;
    /** Zirh / varlik parlamasi esyayla ayni olsun. */
    public boolean glintArmorSame = true;
    public String glintArmorMode = "TEK";
    public int glintArmorColor = 0xFFB040, glintArmorColor2 = 0xFF4060;
    /** Hiz carpani 0 - 4, parlaklik 0.1 - 2, desen boyutu 0.4 - 2.5, egim -45 - 45 derece. */
    public float glintSpeed = 1f, glintStrength = 1f, glintScale = 1f, glintAngle = 10f;
    /** DONEN modunda renk donme hizi 0.2 - 4 */
    public float glintCycleSpeed = 1f;
    /** Parlaklik yavasca nefes alir. */
    public boolean glintPulse = false;

    // ------------------------------------------------------------------ gokyuzu
    public boolean skyEnabled = false;
    public boolean skyAurora = true;
    /** Kutup isigi gucu 0 - 2, hiz 0.2 - 3, yukseklik 0.6 - 1.6 */
    public float skyAuroraStrength = 1f, skyAuroraSpeed = 1f, skyAuroraHeight = 1f;
    /** KLASIK, BUZUL, ATES, PEMBE, GOKKUSAGI, OZEL */
    public String skyAuroraPalette = "KLASIK";
    public int skyAuroraColor1 = 0x2BFF9A, skyAuroraColor2 = 0xB24BFF;
    /** Gunduz de (soluk) gorunsun. */
    public boolean skyAuroraDay = false;
    public boolean skyShooting = true;
    /** Kayan yildiz sikligi 0.2 - 6 (1 = dakikada ~4), uzunluk 0.5 - 2 */
    public float skyShootingRate = 1f, skyShootingLength = 1f;
    public int skyShootingColor = 0xFFFFFF;
    /** Gunes / ay boyutu 0.3 - 3 */
    public float skySunSize = 1f, skyMoonSize = 1f;
    /** -1 = dogal, 0 - 7 sabit ay evresi (sadece gorunus). */
    public int skyMoonPhase = -1;
    /** Yildiz parlakligi carpani 0 - 3 */
    public float skyStars = 1f;
    public boolean skyStarsDay = false;
    /** DOGAL, OZEL, GOKKUSAGI, KAPALI */
    public String skySunsetMode = "DOGAL";
    public int skySunsetColor = 0xFF4F7A;
    /** Gun dogumu / batimi parlakligi 0 - 2 */
    public float skySunsetStrength = 1f;
    public boolean skyTint = false;
    public int skyTintColor = 0x6A5CFF;
    /** Gok rengi karisimi 0 - 1 */
    public float skyTintAmount = 0.3f;

    // ------------------------------------------------------------------ sis
    public boolean fogEnabled = false;
    /** DOGAL, OZEL, GOKKUSAGI */
    public String fogColorMode = "DOGAL";
    public int fogColor = 0xA0B8FF;
    /** Renk karisimi 0 - 1, gokkusagi hizi 0.2 - 4 */
    public float fogColorAmount = 0.6f, fogRainbowSpeed = 1f;
    /** Mesafe carpani 0.1 - 2 */
    public float fogDistance = 1f;
    /** Sisin yumusakligi 0 (dogal) - 1 (yakindan baslar). */
    public float fogSoftness = 0f;
    /** Arazi sisini tamamen kaldir. */
    public boolean fogOff = false;
    /** Mesafe ayari gokyuzu sisine de uygulansin. */
    public boolean fogSky = true;
    /** Nether / boss kalin sisi: DOGAL, AZ, KAPALI */
    public String fogThick = "DOGAL";
    public boolean fogDimOverworld = true, fogDimNether = true, fogDimEnd = true;

    // ------------------------------------------------------------------ esya gorunumleri
    /** Kendi cizdigin / yukledigin esya dokulari acik mi. */
    public boolean itemSkinsEnabled = true;
    /** Esya kimligi -> doku kodu (SkinImage.encode). Profil koduyla birlikte paylasilir. */
    public java.util.Map<String, String> itemSkins = new java.util.LinkedHashMap<>();

    // ------------------------------------------------------------------ menu temasi
    /** GuiTheme kimligi (MOR_CAM, BUZLU_CAM, NEON, ... , VANILLA). Varsayilan: Buzlu Cam. */
    public String menuTheme = "BUZLU_CAM";
    /** Tema ayar surumu: 0 = eski varsayilan (Mor Cam) olan ayar bir kez Buzlu Cam'a gecer. */
    public int menuThemeVer = 0;
    /** Saydamlik carpani 0.4 - 1.6 */
    public float menuOpacity = 1f;
    public boolean menuAccentCustom = false;
    public int menuAccent = 0xC77DFF;

    // ------------------------------------------------------------------ isim etiketi
    /** F5 / serbest kamerada kendi ismini goster. */
    public boolean tagEnabled = true;
    /** Bos = oyuncu adi. */
    public String tagText = "";
    public String tagFont = "mc";
    public int tagColorMode = 0;
    public int tagColor1 = 0xFFFFFF;
    public int tagColor2 = 0x55FFFF;
    public int tagColor3 = 0xFF55FF;
    /** Renk / hareket hizi 0.2 - 3 */
    public float tagAnimSpeed = 1f;
    public boolean tagBold = false;
    public boolean tagItalic = false;
    public boolean tagUnderline = false;
    public boolean tagStrike = false;
    public boolean tagShadow = true;
    public boolean tagOutline = false;
    public int tagOutlineColor = 0x000000;
    /** Karanlikta da parlak. */
    public boolean tagGlow = false;
    /** Arka plan saydamligi 0 - 0.9 */
    public float tagBgAlpha = 0.25f;
    public int tagBgColor = 0x000000;
    /** Boyut 0.5 - 2.5 */
    public float tagScale = 1f;
    /** Yukseklik ofseti (blok) -0.5 - 1.5 */
    public float tagHeight = 0f;
    public int tagAnim = 0;
    /** Emoji onek / sonek (en cok 6'sar emoji). */
    public String tagPrefix = "";
    public String tagSuffix = "";
    public int tagEmojiAnim = 0;
    /** Emoji / gorsel boyutu 0.6 - 2 */
    public float tagEmojiScale = 1f;
    /** config/jestronpriv/icons icindeki gorsel (png / jpg / gif) ya da bos. */
    public String tagPrefixImage = "";
    public String tagSuffixImage = "";
    public boolean tagShowF5 = true;
    public boolean tagShowFreecam = true;
    /** Ozel metin varken gercek adi altta kucuk goster. */
    public boolean tagShowRealName = false;
    /** Son kullanilan emojiler (emoji secici). */
    public String tagRecentEmoji = "";

    // ------------------------------------------------------------------ performans
    /** Efekt parcacik yogunlugu 0.1 - 1.0 */
    public float particleDensity = 1.0f;
    /** Vanilla "Parcaciklar" ayarina uy (Azaltilmis / En az). */
    public boolean respectVanillaParticles = true;
    /** Efekt modelleri bu mesafeden uzaktaysa cizilmez (blok). */
    public int effectRenderDistance = 64;
    /** Ekranda gorunmeyen modelleri cizme. */
    public boolean frustumCulling = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestronpriv.json");
    }

    private static ModConfig read(Path f) {
        if (!Files.isRegularFile(f)) return null;
        try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            ModConfig c = GSON.fromJson(r, ModConfig.class);
            if (c != null) c.sanitize();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ayarlari yukler. Dosya bozuksa silinmez: {@code jestronpriv.json.bozuk} olarak saklanir ve son saglam kayit
     * ({@code jestronpriv.json.bak}) kullanilir; o da yoksa varsayilanlarla baslanir.
     */
    public static ModConfig load() {
        Path f = file();
        ModConfig c = read(f);
        if (c != null) {
            c.markSaved(f);
            return c;
        }
        if (Files.exists(f)) {
            try {
                Files.move(f, f.resolveSibling(f.getFileName() + ".bozuk"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
        c = read(f.resolveSibling(f.getFileName() + ".bak"));
        if (c == null) c = new ModConfig();
        c.save();
        return c;
    }

    public void sanitize() {
        if (effect == null) effect = NONE;
        if (mode == null) mode = "KONUM";
        volume = clamp(volume, 0f, 2f);
        screenDistance = Math.max(2.5, Math.min(8.0, screenDistance));
        maxSessions = Math.max(1, Math.min(40, maxSessions));
        soundChannel = soundChannelName(soundChannel);

        swingStyle = SwingStyle.parse(swingStyle).name();
        swingStrength = clamp(swingStrength, 0.5f, 3f);
        swingDuration = clamp(swingDuration, 0.5f, 4f);
        vmHandX = clamp(vmHandX, -0.6f, 0.6f);
        vmHandY = clamp(vmHandY, -0.6f, 0.6f);
        vmHandZ = clamp(vmHandZ, -0.6f, 0.6f);
        vmItemX = clamp(vmItemX, -0.6f, 0.6f);
        vmItemY = clamp(vmItemY, -0.6f, 0.6f);
        vmItemZ = clamp(vmItemZ, -0.6f, 0.6f);
        vmItemScale = clamp(vmItemScale, 0.3f, 2f);
        vmRotX = clamp(vmRotX, -180f, 180f);
        vmRotY = clamp(vmRotY, -180f, 180f);
        vmRotZ = clamp(vmRotZ, -180f, 180f);
        vmHandFov = clamp(vmHandFov, 30f, 110f);
        cgPreset = com.jestron.jestronpriv.client.view.ColorGrade.Preset.parse(cgPreset).name();
        cgSaturation = clamp(cgSaturation, 0f, 2f);
        cgBrightness = clamp(cgBrightness, 0.5f, 1.5f);
        cgContrast = clamp(cgContrast, 0.5f, 1.5f);
        cgTemperature = clamp(cgTemperature, -1f, 1f);
        aspect = com.jestron.jestronpriv.client.view.ScreenTweaks.Aspect.parse(aspect).name();
        aspectCustom = clamp(aspectCustom, 0.8f, 2.4f);
        fireHeight = clamp(fireHeight, 0f, 0.45f);
        fireOpacity = clamp(fireOpacity, 0.2f, 1f);
        zoomFactor = clamp(zoomFactor, 1.5f, 15f);
        hitStyle = com.jestron.jestronpriv.hitfx.HitStyle.parse(hitStyle).name();
        critStyle = com.jestron.jestronpriv.hitfx.HitStyle.parse(critStyle).name();
        hitAmount = clamp(hitAmount, 0.25f, 2.5f);
        hitSize = clamp(hitSize, 0.5f, 2f);
        hitSoundVolume = clamp(hitSoundVolume, 0f, 2f);
        dmgFormat = "CAN".equals(dmgFormat) ? "CAN" : "KALP";
        dmgScale = clamp(dmgScale, 0.5f, 2f);
        dmgColor &= 0xFFFFFF;
        dmgCritColor &= 0xFFFFFF;
        comboPos = "UST".equals(comboPos) || "SAG".equals(comboPos) ? comboPos : "ALT";
        comboScale = clamp(comboScale, 0.5f, 2f);
        comboTimeout = clamp(comboTimeout, 1f, 6f);
        comboVolume = clamp(comboVolume, 0f, 2f);
        streakVolume = clamp(streakVolume, 0f, 2f);
        streakLang = "EN".equals(streakLang) ? "EN" : "TR";
        streakWindow = clamp(streakWindow, 2f, 10f);
        hitColorRgb &= 0xFFFFFF;
        hitColorAlpha = clamp(hitColorAlpha, 0.2f, 0.9f);
        chShape = com.jestron.jestronpriv.client.pvp.Crosshair.Shape.parse(chShape).name();
        chSize = clamp(chSize, 1f, 16f);
        chThickness = clamp(chThickness, 1f, 4f);
        chGap = clamp(chGap, 0f, 10f);
        chColor &= 0xFFFFFF;
        chOpacity = clamp(chOpacity, 0.1f, 1f);
        totemScale = clamp(totemScale, 0.5f, 2f);
        if (totemSoundFile == null || totemSoundFile.contains("/") || totemSoundFile.contains("\\") || totemSoundFile.contains(":")
                || totemSoundFile.trim().equals("..") || totemSoundFile.trim().equals(".") || totemSoundFile.length() > 200)
            totemSoundFile = "";
        if (Float.isNaN(totemSoundVolume)) totemSoundVolume = 1f;
        if (Float.isNaN(totemSoundSpeed)) totemSoundSpeed = 1f;
        totemSoundVolume = clamp(totemSoundVolume, 0f, 2f);
        totemSoundStart = clamp(totemSoundStart, 0f, 180f);
        totemSoundLength = clamp(totemSoundLength, 0f, 60f);
        totemSoundFadeIn = clamp(totemSoundFadeIn, 0f, 2f);
        totemSoundFadeOut = clamp(totemSoundFadeOut, 0f, 3f);
        totemSoundSpeed = clamp(totemSoundSpeed, 0.5f, 2f);
        if (!"BEN".equals(totemSoundWho) && !"DIGER".equals(totemSoundWho)) totemSoundWho = "HERKES";
        timeMode = com.jestron.jestronpriv.client.view.WorldTime.TimeMode.parse(timeMode).name();
        timeValue = Math.max(0, Math.min(24000, timeValue));
        timeSpeed = clamp(timeSpeed, 1f, 200f);
        weather = com.jestron.jestronpriv.client.view.WorldTime.Weather.parse(weather).name();
        comboX = clamp(comboX, 0f, 1f);
        comboY = clamp(comboY, 0f, 1f);
        streakX = clamp(streakX, 0f, 1f);
        streakY = clamp(streakY, 0f, 1f);
        streakScale = clamp(streakScale, 0.5f, 2f);
        invX = clamp(invX, 0f, 1f);
        invY = clamp(invY, 0f, 1f);
        invScale = clamp(invScale, 0.5f, 2f);
        invBg = clamp(invBg, 0f, 0.9f);
        pkX = clamp(pkX, 0f, 1f);
        pkY = clamp(pkY, 0f, 1f);
        pkScale = clamp(pkScale, 0.5f, 2f);
        pkBg = clamp(pkBg, 0f, 0.9f);
        pkDuration = clamp(pkDuration, 1f, 10f);
        pkMax = Math.max(1, Math.min(12, pkMax));
        reachBg = clamp(reachBg, 0f, 0.9f);
        reachDecimals = Math.max(1, Math.min(3, reachDecimals));
        reachFade = clamp(reachFade, 0f, 30f);
        pingBg = clamp(pingBg, 0f, 0.9f);
        pingInterval = clamp(pingInterval, 0.5f, 5f);
        kfBg = clamp(kfBg, 0f, 0.9f);
        if (!"HEPSI".equals(kfFilter) && !"OYUNCU".equals(kfFilter) && !"BENIM".equals(kfFilter)) kfFilter = "HEPSI";
        kfMax = Math.max(1, Math.min(10, kfMax));
        kfDuration = clamp(kfDuration, 2f, 30f);
        tfBg = clamp(tfBg, 0f, 0.9f);
        tfMax = Math.max(1, Math.min(8, tfMax));
        tfDuration = clamp(tfDuration, 2f, 30f);
        tfRange = Math.max(0, Math.min(256, tfRange));
        hbColor &= 0xFFFFFF;
        hbTargetColor &= 0xFFFFFF;
        hbHurtColor &= 0xFFFFFF;
        hbEyeColor &= 0xFFFFFF;
        hbLookColor &= 0xFFFFFF;
        hbWidth = clamp(hbWidth, 0.5f, 4f);
        hbAlpha = clamp(hbAlpha, 0.1f, 1f);
        hbFillAlpha = clamp(hbFillAlpha, 0.02f, 0.6f);
        hbRange = Math.max(4, Math.min(128, hbRange));
        for (float[] xy : new float[][]{{reachX, reachY, reachScale}, {pingX, pingY, pingScale}, {kfX, kfY, kfScale}, {tfX, tfY, tfScale}}) {
            if (!Float.isFinite(xy[0]) || !Float.isFinite(xy[1]) || !Float.isFinite(xy[2])) resetPvpToolsLayout();
        }
        giRange = clamp(giRange, 4f, 48f);
        giMax = Math.max(5, Math.min(80, giMax));
        giScale = clamp(giScale, 0.5f, 2f);
        giBg = clamp(giBg, 0f, 0.8f);
        potX = clamp(potX, 0f, 1f);
        potY = clamp(potY, 0f, 1f);
        potScale = clamp(potScale, 0.5f, 2f);
        potBg = clamp(potBg, 0f, 0.9f);
        potWarn = Math.max(0, Math.min(30, potWarn));
        potColor = "TUR".equals(potColor) || "BEYAZ".equals(potColor) ? potColor : "ETKI";
        potSort = "TUR".equals(potSort) || "AD".equals(potSort) ? potSort : "SURE";
        icX = clamp(icX, 0f, 1f);
        icY = clamp(icY, 0f, 1f);
        icScale = clamp(icScale, 0.5f, 2f);
        icBg = clamp(icBg, 0f, 0.9f);
        icMask &= (1 << com.jestron.jestronpriv.client.hud.ItemHud.ITEM_COUNT) - 1;
        infoX = clamp(infoX, 0f, 1f);
        infoY = clamp(infoY, 0f, 1f);
        infoScale = clamp(infoScale, 0.5f, 2f);
        infoBg = clamp(infoBg, 0f, 0.9f);
        infoMask &= (1 << com.jestron.jestronpriv.client.hud.InfoHud.FIELD_COUNT) - 1;
        infoLabelColor &= 0xFFFFFF;
        infoValueColor &= 0xFFFFFF;
        infoDecimals = Math.max(0, Math.min(2, infoDecimals));
        if (pet == null || (!NONE.equals(pet) && com.jestron.jestronpriv.pet.PetKind.parse(pet) == null)) pet = NONE;
        petScale = clamp(petScale, 0.5f, 2f);
        petMode = com.jestron.jestronpriv.pet.PetMode.parse(petMode).name();
        petColor &= 0xFFFFFF;
        petFollow = clamp(petFollow, 0.3f, 3f);
        petBob = clamp(petBob, 0f, 2f);
        petGlow = clamp(petGlow, 0.2f, 1.5f);
        boColor &= 0xFFFFFF;
        boFillColor &= 0xFFFFFF;
        boAlpha = clamp(boAlpha, 0.1f, 1f);
        boWidth = clamp(boWidth, 1f, 10f);
        boFill = "YOK".equals(boFill) || "TUM".equals(boFill) ? boFill : "YUZ";
        boFillAlpha = clamp(boFillAlpha, 0.03f, 0.6f);
        fovSprint = clamp(fovSprint, 0f, 1.5f);
        fovSpeed = clamp(fovSpeed, 0f, 1.5f);
        fovFly = clamp(fovFly, 0f, 1.5f);
        fovBow = clamp(fovBow, 0f, 1.5f);
        fovWater = clamp(fovWater, 0f, 1f);
        fovSmooth = switch (fovSmooth == null ? "" : fovSmooth) {
            case "ANINDA", "YUMUSAK", "COK_YUMUSAK" -> fovSmooth;
            default -> "NORMAL";
        };
        cullQuality = "HIZLI".equals(cullQuality) || "KALITE".equals(cullQuality) ? cullQuality : "DENGELI";

        particleDensity = clamp(particleDensity, 0.1f, 1f);

        if (ctText == null) ctText = "{kurban}";
        if (ctText.length() > 96) ctText = ctText.substring(0, 96);
        ctFont = Math.max(0, Math.min(3, ctFont));
        ctColorMode = Math.max(0, Math.min(2, ctColorMode));
        ctColor1 &= 0xFFFFFF;
        ctColor2 &= 0xFFFFFF;
        ctBgColor &= 0xFFFFFF;
        ctOutlineColor &= 0xFFFFFF;
        ctBgAlpha = clamp(ctBgAlpha, 0.05f, 1f);
        ctSize = clamp(ctSize, 0.5f, 3f);
        ctHeight = clamp(ctHeight, 1f, 4f);
        ctAnim = enumName(com.jestron.jestronpriv.fx.CustomText.Anim.class, ctAnim, "POP");
        ctSound = enumName(com.jestron.jestronpriv.fx.CustomText.Sfx.class, ctSound, "LEVELUP");
        // 1.4.x elytra gorunumu ayni kimlikle animasyonlu kanada tasinir
        if ((wing == null || NONE.equals(wing)) && elytraSkin != null
                && com.jestron.jestronpriv.wings.WingStyles.get(elytraSkin) != null) wing = elytraSkin;
        elytraSkin = null;
        if (hat == null || (!NONE.equals(hat) && com.jestron.jestronpriv.hats.HatStyles.get(hat) == null)) hat = NONE;
        if (Float.isNaN(hatScale)) hatScale = 1f;
        if (Float.isNaN(hatSpeed)) hatSpeed = 1f;
        hatScale = clamp(hatScale, 0.5f, 2f);
        if (cape == null || (!NONE.equals(cape) && com.jestron.jestronpriv.cape.CapeDesigns.get(cape) == null)) cape = NONE;
        capeLength = clamp(capeLength, 0.7f, 1.4f);
        capeWidth = clamp(capeWidth, 0.8f, 1.2f);
        capeWind = clamp(capeWind, 0f, 2f);
        capePhysics = clamp(capePhysics, 0f, 2f);
        capeSpeed = clamp(capeSpeed, 0f, 3f);
        capeGlowStrength = clamp(capeGlowStrength, 0.2f, 1.5f);
        capeHue = clamp(capeHue, -180f, 180f);
        capeRainbowSpeed = clamp(capeRainbowSpeed, 0.2f, 4f);
        capeInnerColor &= 0xFFFFFF;
        capeParticleRate = clamp(capeParticleRate, 0f, 3f);
        if (ely == null || (!NONE.equals(ely) && com.jestron.jestronpriv.cape.ElytraDesigns.get(ely) == null)) ely = NONE;
        elySpeed = clamp(elySpeed, 0f, 3f);
        elyGlowStrength = clamp(elyGlowStrength, 0.2f, 1.5f);
        elyHue = clamp(elyHue, -180f, 180f);
        elyRainbowSpeed = clamp(elyRainbowSpeed, 0.2f, 4f);
        elyParticleRate = clamp(elyParticleRate, 0f, 3f);
        glintItemMode = glintMode(glintItemMode);
        glintArmorMode = glintMode(glintArmorMode);
        glintItemColor &= 0xFFFFFF;
        glintItemColor2 &= 0xFFFFFF;
        glintArmorColor &= 0xFFFFFF;
        glintArmorColor2 &= 0xFFFFFF;
        glintSpeed = clamp(glintSpeed, 0f, 4f);
        glintStrength = clamp(glintStrength, 0.1f, 2f);
        glintScale = clamp(glintScale, 0.4f, 2.5f);
        glintAngle = clamp(glintAngle, -45f, 45f);
        glintCycleSpeed = clamp(glintCycleSpeed, 0.2f, 4f);
        skyAuroraStrength = clamp(skyAuroraStrength, 0f, 2f);
        skyAuroraSpeed = clamp(skyAuroraSpeed, 0.2f, 3f);
        skyAuroraHeight = clamp(skyAuroraHeight, 0.6f, 1.6f);
        skyAuroraPalette = switch (skyAuroraPalette == null ? "" : skyAuroraPalette) {
            case "BUZUL", "ATES", "PEMBE", "GOKKUSAGI", "OZEL" -> skyAuroraPalette;
            default -> "KLASIK";
        };
        skyAuroraColor1 &= 0xFFFFFF;
        skyAuroraColor2 &= 0xFFFFFF;
        skyShootingRate = clamp(skyShootingRate, 0.2f, 6f);
        skyShootingLength = clamp(skyShootingLength, 0.5f, 2f);
        skyShootingColor &= 0xFFFFFF;
        skySunSize = clamp(skySunSize, 0.3f, 3f);
        skyMoonSize = clamp(skyMoonSize, 0.3f, 3f);
        skyMoonPhase = Math.max(-1, Math.min(7, skyMoonPhase));
        skyStars = clamp(skyStars, 0f, 3f);
        skySunsetMode = switch (skySunsetMode == null ? "" : skySunsetMode) {
            case "OZEL", "GOKKUSAGI", "KAPALI" -> skySunsetMode;
            default -> "DOGAL";
        };
        skySunsetColor &= 0xFFFFFF;
        skySunsetStrength = clamp(skySunsetStrength, 0f, 2f);
        skyTintColor &= 0xFFFFFF;
        skyTintAmount = clamp(skyTintAmount, 0f, 1f);
        fogColorMode = "OZEL".equals(fogColorMode) || "GOKKUSAGI".equals(fogColorMode) ? fogColorMode : "DOGAL";
        fogColor &= 0xFFFFFF;
        fogColorAmount = clamp(fogColorAmount, 0f, 1f);
        fogRainbowSpeed = clamp(fogRainbowSpeed, 0.2f, 4f);
        fogDistance = clamp(fogDistance, 0.1f, 2f);
        fogSoftness = clamp(fogSoftness, 0f, 1f);
        fogThick = "AZ".equals(fogThick) || "KAPALI".equals(fogThick) ? fogThick : "DOGAL";
        sanitizeItemSkins();
        if (menuThemeVer < 1) {
            // 1.21.0: varsayilan tema Buzlu Cam (eski varsayilan Mor Cam'daki ayarlar bir kez gecer; sonra kullanici secimi korunur)
            if ("MOR_CAM".equals(menuTheme)) menuTheme = com.jestron.jestronpriv.ui.GuiTheme.DEFAULT_ID;
            menuThemeVer = 1;
        }
        menuTheme = com.jestron.jestronpriv.ui.GuiTheme.get(menuTheme == null ? "" : menuTheme).id();
        menuOpacity = clamp(menuOpacity, 0.4f, 1.6f);
        menuAccent &= 0xFFFFFF;
        hatHeight = clamp(hatHeight, -0.3f, 0.6f);
        hatTilt = clamp(hatTilt, -35f, 35f);
        hatSpeed = clamp(hatSpeed, 0f, 3f);
        hatColor &= 0xFFFFFF;
        tagText = tagText == null ? "" : com.jestron.jestronpriv.tag.TagSettings.cleanText(tagText, com.jestron.jestronpriv.tag.TagSettings.MAX_TEXT_CP);
        if (tagFont == null || !com.jestron.jestronpriv.tag.TagFonts.get(tagFont).id().equals(tagFont)) tagFont = "mc";
        tagColorMode = Math.max(0, Math.min(com.jestron.jestronpriv.tag.TagStyle.modeCount() - 1, tagColorMode));
        tagAnim = Math.max(0, Math.min(com.jestron.jestronpriv.tag.TagStyle.ANIMS.length - 1, tagAnim));
        tagEmojiAnim = Math.max(0, Math.min(com.jestron.jestronpriv.tag.TagStyle.EMOJI_ANIMS.length - 1, tagEmojiAnim));
        tagColor1 &= 0xFFFFFF;
        tagColor2 &= 0xFFFFFF;
        tagColor3 &= 0xFFFFFF;
        tagOutlineColor &= 0xFFFFFF;
        tagBgColor &= 0xFFFFFF;
        if (Float.isNaN(tagScale)) tagScale = 1f;
        if (Float.isNaN(tagAnimSpeed)) tagAnimSpeed = 1f;
        if (Float.isNaN(tagEmojiScale)) tagEmojiScale = 1f;
        tagAnimSpeed = clamp(tagAnimSpeed, 0.2f, 3f);
        tagBgAlpha = clamp(tagBgAlpha, 0f, 0.9f);
        tagScale = clamp(tagScale, 0.5f, 2.5f);
        tagHeight = clamp(tagHeight, -0.5f, 1.5f);
        tagEmojiScale = clamp(tagEmojiScale, 0.6f, 2f);
        tagPrefix = tagPrefix == null ? "" : com.jestron.jestronpriv.tag.TagSettings.cleanText(tagPrefix, com.jestron.jestronpriv.tag.TagSettings.MAX_EMOJI);
        tagSuffix = tagSuffix == null ? "" : com.jestron.jestronpriv.tag.TagSettings.cleanText(tagSuffix, com.jestron.jestronpriv.tag.TagSettings.MAX_EMOJI);
        tagPrefixImage = safeFileName(tagPrefixImage);
        tagSuffixImage = safeFileName(tagSuffixImage);
        if (tagRecentEmoji == null || tagRecentEmoji.length() > 400) tagRecentEmoji = "";
        if (wing == null || (!NONE.equals(wing) && com.jestron.jestronpriv.wings.WingStyles.get(wing) == null)) wing = NONE;
        wingScale = clamp(wingScale, 0.6f, 1.4f);
        freeCamAimTime = clamp(freeCamAimTime, 0.25f, 3f);
        freeCamTurnSpeed = clamp(freeCamTurnSpeed, 0.1f, 1f);
        if (aura == null) aura = NONE;
        auraDensity = clamp(auraDensity, 0.25f, 2f);
        auraSpread = clamp(auraSpread, 0.5f, 2.5f);
        auraSpeed = clamp(auraSpeed, 0.25f, 3f);
        effectRenderDistance = Math.max(16, Math.min(160, effectRenderDistance));
    }

    private static <E extends Enum<E>> String enumName(Class<E> type, String v, String def) {
        if (v != null) for (E e : type.getEnumConstants()) if (e.name().equalsIgnoreCase(v.trim())) return e.name();
        return def;
    }

    public com.jestron.jestronpriv.fx.CustomText.Anim ctAnim() {
        return com.jestron.jestronpriv.fx.CustomText.Anim.valueOf(enumName(com.jestron.jestronpriv.fx.CustomText.Anim.class, ctAnim, "POP"));
    }

    public com.jestron.jestronpriv.fx.CustomText.Sfx ctSound() {
        return com.jestron.jestronpriv.fx.CustomText.Sfx.valueOf(enumName(com.jestron.jestronpriv.fx.CustomText.Sfx.class, ctSound, "LEVELUP"));
    }

    public com.jestron.jestronpriv.fx.api.TextStyle textStyle() {
        int bg = ctBgOn ? (Math.round(clamp(ctBgAlpha, 0.05f, 1f) * 255) << 24) | (ctBgColor & 0xFFFFFF) : 0;
        return new com.jestron.jestronpriv.fx.api.TextStyle(ctFont, ctColor1 & 0xFFFFFF, ctColor2 & 0xFFFFFF, ctColorMode, bg,
                ctBold, ctItalic, ctUnderline, ctStrike, ctShadow, ctOutline, ctOutlineColor & 0xFFFFFF, ctGlow);
    }

    public void resetCustomText() {
        ModConfig d = new ModConfig();
        ctText = d.ctText; ctFont = d.ctFont; ctColorMode = d.ctColorMode; ctColor1 = d.ctColor1; ctColor2 = d.ctColor2;
        ctBgOn = d.ctBgOn; ctBgColor = d.ctBgColor; ctBgAlpha = d.ctBgAlpha; ctBold = d.ctBold; ctItalic = d.ctItalic;
        ctUnderline = d.ctUnderline; ctStrike = d.ctStrike; ctShadow = d.ctShadow; ctOutline = d.ctOutline;
        ctOutlineColor = d.ctOutlineColor; ctGlow = d.ctGlow; ctSize = d.ctSize; ctHeight = d.ctHeight;
        ctAnim = d.ctAnim; ctSound = d.ctSound; ctParticles = d.ctParticles;
    }

    private static float clamp(float v, float min, float max) {
        if (Float.isNaN(v)) return min;
        return Math.max(min, Math.min(max, v));
    }

    // ------------------------------------------------------------------ kaydetme

    private static final Object SAVE_LOCK = new Object(), WRITE_LOCK = new Object();
    /** Bekleyen kayit (en son hali; araya giren eski kayitlar yazilmaz). */
    private static String pendingJson;
    private static Path pendingPath;
    private static boolean saverQueued;
    /** Son yazilan icerigin ozeti: degismeyen ayar yeniden yazilmaz. */
    private static long lastHash;
    private static int lastLen = -1;
    private static Path lastPath;
    /** true: dosyaya yazma arka planda (oyun icinde). Testlerde / acilista anlik. */
    private static volatile boolean async;
    private static java.util.concurrent.ExecutorService saver;

    /** Istemci acildi: kayitlar artik arka planda; oyun kapanirken bekleyen kayit tamamlanir. */
    public static synchronized void enableAsyncSave() {
        if (async) return;
        saver = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "JestronPriv-Kayit");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(ModConfig::flushPending, "JestronPriv-Kayit-Son"));
        } catch (Throwable ignored) {
        }
        async = true;
    }

    private static long hash(String s) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0, n = s.length(); i < n; i++) {
            h ^= s.charAt(i);
            h *= 0x100000001b3L;
        }
        return h;
    }

    /**
     * Ayarlari kaydeder. JSON burada (ayni anda degisemesin diye oyun is parcaciginda) uretilir; icerik degismediyse
     * hicbir sey yazilmaz. Oyun icinde diske yazma arka planda yapilir: buyuk ayarlarda (esya cizimleri) ekran
     * kapanirken takilma olmaz.
     */
    public void save() {
        String json;
        Path f;
        try {
            json = GSON.toJson(this);
            f = file();
        } catch (Throwable t) {
            return;
        }
        long h = hash(json);
        synchronized (SAVE_LOCK) {
            if (pendingJson == null && h == lastHash && json.length() == lastLen && f.equals(lastPath) && Files.isRegularFile(f)) return;
            pendingJson = json;
            pendingPath = f;
            if (!async) {
                // anlik
            } else {
                if (!saverQueued) {
                    saverQueued = true;
                    try {
                        saver.execute(ModConfig::flushPending);
                        return;
                    } catch (Throwable t) {
                        saverQueued = false;
                    }
                } else return;
            }
        }
        flushPending();
    }

    /** Diskteki dosya bu ayarla ayni sayilsin (yuklendikten hemen sonra: degismeden kaydetme yazmasin). */
    private void markSaved(Path f) {
        try {
            String json = GSON.toJson(this);
            synchronized (SAVE_LOCK) {
                lastHash = hash(json);
                lastLen = json.length();
                lastPath = f;
            }
        } catch (Throwable ignored) {
        }
    }

    /** Bekleyen kaydi simdi yazar (arka plan, kapanis ya da anlik kayit). */
    public static void flushPending() {
        synchronized (WRITE_LOCK) {
            String json;
            Path f;
            synchronized (SAVE_LOCK) {
                json = pendingJson;
                f = pendingPath;
                pendingJson = null;
                pendingPath = null;
                saverQueued = false;
            }
            if (json == null || f == null) return;
            boolean known;
            synchronized (SAVE_LOCK) {
                known = f.equals(lastPath) && lastLen >= 0;
            }
            if (write(f, json, known)) {
                synchronized (SAVE_LOCK) {
                    lastHash = hash(json);
                    lastLen = json.length();
                    lastPath = f;
                }
            }
        }
    }

    /** Dosyaya once gecici olarak yazar, sonra yer degistirir: oyun kapanirken yarim dosya kalmaz. */
    private static boolean write(Path f, String json, boolean knownGood) {
        try {
            Files.createDirectories(f.getParent());
            Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
            try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                w.write(json);
            }
            // son saglam kaydin yedegi (dosya bozulursa buradan donulur); bu oturumda biz yazdiysak yeniden okumaya gerek yok
            if (Files.isRegularFile(f) && (knownGood || read(f) != null)) {
                try {
                    Files.copy(f, f.resolveSibling(f.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                }
            }
            try {
                Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public ViewMode viewMode() {
        return ViewMode.parse(mode);
    }

    private static String soundChannelName(String s) {
        if ("PLAYERS".equals(s) || "AMBIENT".equals(s)) return s;
        return "MASTER";
    }

    public net.minecraft.sounds.SoundSource soundSource() {
        return switch (soundChannelName(soundChannel)) {
            case "PLAYERS" -> net.minecraft.sounds.SoundSource.PLAYERS;
            case "AMBIENT" -> net.minecraft.sounds.SoundSource.AMBIENT;
            default -> net.minecraft.sounds.SoundSource.MASTER;
        };
    }

    public String soundChannelLabel() {
        return switch (soundChannelName(soundChannel)) {
            case "PLAYERS" -> "Oyuncular";
            case "AMBIENT" -> "Ortam";
            default -> "Ana Ses";
        };
    }

    public void nextSoundChannel() {
        soundChannel = switch (soundChannelName(soundChannel)) {
            case "MASTER" -> "PLAYERS";
            case "PLAYERS" -> "AMBIENT";
            default -> "MASTER";
        };
    }

    public SwingStyle swingStyle() {
        return SwingStyle.parse(swingStyle);
    }

    // ------------------------------------------------------------------ bolum sifirlama

    public void resetCamera() {
        ModConfig d = new ModConfig();
        freeCam = d.freeCam;
        freeCamAim = d.freeCamAim;
        freeCamAimTime = d.freeCamAimTime;
        freeCamTurnSpeed = d.freeCamTurnSpeed;
        freeCamSkipFront = d.freeCamSkipFront;
        freeCamElytra = d.freeCamElytra;
        freeCamCrosshair = d.freeCamCrosshair;
        freeLook = d.freeLook;
    }

    public void resetSwing() {
        ModConfig d = new ModConfig();
        swingEnabled = d.swingEnabled;
        swingStyle = d.swingStyle;
        swingStrength = d.swingStrength;
        swingDuration = d.swingDuration;
        swingOnlyWhileSwinging = d.swingOnlyWhileSwinging;
        swingEquipDip = d.swingEquipDip;
        swingCooldownDip = d.swingCooldownDip;
    }

    public void resetViewModel() {
        ModConfig d = new ModConfig();
        vmEnabled = d.vmEnabled;
        vmHandX = d.vmHandX;
        vmHandY = d.vmHandY;
        vmHandZ = d.vmHandZ;
        vmItemX = d.vmItemX;
        vmItemY = d.vmItemY;
        vmItemZ = d.vmItemZ;
        vmItemScale = d.vmItemScale;
        vmRotX = d.vmRotX;
        vmRotY = d.vmRotY;
        vmRotZ = d.vmRotZ;
        vmOffhand = d.vmOffhand;
        vmHideOffhand = d.vmHideOffhand;
        vmHandFov = d.vmHandFov;
        vmIdleSway = d.vmIdleSway;
    }

    public void resetDisplay() {
        ModConfig d = new ModConfig();
        cgEnabled = d.cgEnabled;
        cgPreset = d.cgPreset;
        cgSaturation = d.cgSaturation;
        cgBrightness = d.cgBrightness;
        cgContrast = d.cgContrast;
        cgTemperature = d.cgTemperature;
        aspect = d.aspect;
        aspectCustom = d.aspectCustom;
        fireHeight = d.fireHeight;
        fireOpacity = d.fireOpacity;
        zoomEnabled = d.zoomEnabled;
        zoomFactor = d.zoomFactor;
        zoomSmooth = d.zoomSmooth;
        zoomScroll = d.zoomScroll;
    }

    public void resetHitFx() {
        ModConfig d = new ModConfig();
        hitStyle = d.hitStyle;
        critStyle = d.critStyle;
        hitAmount = d.hitAmount;
        hitSize = d.hitSize;
        hitHideVanilla = d.hitHideVanilla;
        hitOnMobs = d.hitOnMobs;
        hitSound = d.hitSound;
        hitSoundVolume = d.hitSoundVolume;
    }

    public void resetPvp() {
        ModConfig d = new ModConfig();
        pvpMobs = d.pvpMobs;
        dmgNumbers = d.dmgNumbers;
        dmgFormat = d.dmgFormat;
        dmgScale = d.dmgScale;
        dmgColorByAmount = d.dmgColorByAmount;
        dmgColor = d.dmgColor;
        dmgCritColor = d.dmgCritColor;
        dmgUnknownMarker = d.dmgUnknownMarker;
        combo = d.combo;
        comboPos = d.comboPos;
        comboScale = d.comboScale;
        comboTimeout = d.comboTimeout;
        comboSound = d.comboSound;
        comboVolume = d.comboVolume;
        killStreak = d.killStreak;
        streakSound = d.streakSound;
        streakVolume = d.streakVolume;
        streakFlash = d.streakFlash;
        streakLang = d.streakLang;
        streakWindow = d.streakWindow;
        hitColor = d.hitColor;
        hitColorRgb = d.hitColorRgb;
        hitColorRainbow = d.hitColorRainbow;
        hitColorAll = d.hitColorAll;
        hitColorAlpha = d.hitColorAlpha;
    }

    public void resetCrosshair() {
        ModConfig d = new ModConfig();
        chEnabled = d.chEnabled;
        chShape = d.chShape;
        chSize = d.chSize;
        chThickness = d.chThickness;
        chGap = d.chGap;
        chColor = d.chColor;
        chRainbow = d.chRainbow;
        chOutline = d.chOutline;
        chOpacity = d.chOpacity;
        chDot = d.chDot;
        chDynamic = d.chDynamic;
        chHitMarker = d.chHitMarker;
    }

    public void resetTotemTime() {
        ModConfig d = new ModConfig();
        totemFx = d.totemFx;
        totemHideVanilla = d.totemHideVanilla;
        totemHideScreen = d.totemHideScreen;
        totemCounter = d.totemCounter;
        totemChat = d.totemChat;
        totemSound = d.totemSound;
        totemScale = d.totemScale;
        totemCustom = d.totemCustom;
        totemSoundVolume = d.totemSoundVolume;
        totemSoundStart = d.totemSoundStart;
        totemSoundLength = d.totemSoundLength;
        totemSoundFadeIn = d.totemSoundFadeIn;
        totemSoundFadeOut = d.totemSoundFadeOut;
        totemSoundSpeed = d.totemSoundSpeed;
        totemSoundWho = d.totemSoundWho;
        totemSoundMuteVanilla = d.totemSoundMuteVanilla;
        totemSoundOverlap = d.totemSoundOverlap;
        timeMode = d.timeMode;
        timeValue = d.timeValue;
        timeSpeed = d.timeSpeed;
        weather = d.weather;
    }

    public void resetHud() {
        ModConfig d = new ModConfig();
        hudHideF3 = d.hudHideF3;
        hudShadow = d.hudShadow;
        potHud = d.potHud;
        potX = d.potX;
        potY = d.potY;
        potScale = d.potScale;
        potBg = d.potBg;
        potBar = d.potBar;
        potNames = d.potNames;
        potCompact = d.potCompact;
        potHideVanilla = d.potHideVanilla;
        potAmbient = d.potAmbient;
        potWarn = d.potWarn;
        potColor = d.potColor;
        potSort = d.potSort;
        icHud = d.icHud;
        icX = d.icX;
        icY = d.icY;
        icScale = d.icScale;
        icBg = d.icBg;
        icVertical = d.icVertical;
        icHideZero = d.icHideZero;
        icWarn = d.icWarn;
        icCooldown = d.icCooldown;
        icMask = d.icMask;
        infoHud = d.infoHud;
        infoX = d.infoX;
        infoY = d.infoY;
        infoScale = d.infoScale;
        infoBg = d.infoBg;
        infoRow = d.infoRow;
        infoMask = d.infoMask;
        infoLabelColor = d.infoLabelColor;
        infoValueColor = d.infoValueColor;
        infoQualityColor = d.infoQualityColor;
        infoDecimals = d.infoDecimals;
    }

    public void resetInventoryItems() {
        ModConfig d = new ModConfig();
        invHud = d.invHud;
        invBg = d.invBg;
        invHotbar = d.invHotbar;
        invArmor = d.invArmor;
        invHideEmpty = d.invHideEmpty;
        invHideInScreens = d.invHideInScreens;
        pkHud = d.pkHud;
        pkBg = d.pkBg;
        pkPickups = d.pkPickups;
        pkDrops = d.pkDrops;
        pkMerge = d.pkMerge;
        pkNewestBottom = d.pkNewestBottom;
        pkRarity = d.pkRarity;
        pkDuration = d.pkDuration;
        pkMax = d.pkMax;
        giEnabled = d.giEnabled;
        giRange = d.giRange;
        giMax = d.giMax;
        giName = d.giName;
        giIcon = d.giIcon;
        giCount = d.giCount;
        giRarity = d.giRarity;
        giMerge = d.giMerge;
        giHitbox = d.giHitbox;
        giOnlyLooking = d.giOnlyLooking;
        giScale = d.giScale;
        giBg = d.giBg;
    }

    /** Sadece HUD konum ve boyutlari. */
    public void resetHudLayout() {
        ModConfig d = new ModConfig();
        comboX = d.comboX;
        comboY = d.comboY;
        comboScale = d.comboScale;
        streakX = d.streakX;
        streakY = d.streakY;
        streakScale = d.streakScale;
        invX = d.invX;
        invY = d.invY;
        invScale = d.invScale;
        pkX = d.pkX;
        pkY = d.pkY;
        pkScale = d.pkScale;
        potX = d.potX;
        potY = d.potY;
        potScale = d.potScale;
        icX = d.icX;
        icY = d.icY;
        icScale = d.icScale;
        infoX = d.infoX;
        infoY = d.infoY;
        infoScale = d.infoScale;
        resetPvpToolsLayout();
    }

    /** PvP araclari HUD konumlari. */
    public void resetPvpToolsLayout() {
        ModConfig d = new ModConfig();
        reachX = d.reachX;
        reachY = d.reachY;
        reachScale = d.reachScale;
        pingX = d.pingX;
        pingY = d.pingY;
        pingScale = d.pingScale;
        kfX = d.kfX;
        kfY = d.kfY;
        kfScale = d.kfScale;
        tfX = d.tfX;
        tfY = d.tfY;
        tfScale = d.tfScale;
    }

    /** PvP araclari (reach, ping, oldurme akisi, totem bildirimi, hitbox): tum ayarlar varsayilana. */
    public void resetPvpTools() {
        ModConfig d = new ModConfig();
        for (java.lang.reflect.Field f : ModConfig.class.getDeclaredFields()) {
            String n = f.getName();
            int mod = f.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isFinal(mod)) continue;
            if (!(n.startsWith("reach") || n.startsWith("ping") || n.startsWith("kf") || n.startsWith("tf") || n.startsWith("hb"))) continue;
            try {
                f.set(this, f.get(d));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    /** Dosya adi: klasor ayiraci / ust dizin icermeyen duz ad (degilse bos). */
    static String safeFileName(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty() || s.length() > 200 || s.contains("/") || s.contains("\\") || s.contains(":") || s.equals("..") || s.equals(".")) return "";
        return s;
    }

    private static String glintMode(String m) {
        return switch (m == null ? "" : m) {
            case "IKI", "AKAN", "DONEN", "VANILLA" -> m;
            default -> "TEK";
        };
    }

    /** Adlari bu oneklerle baslayan alanlari varsayilana dondurur (onekten sonra buyuk harf / rakam gelmeli). */
    public void resetPrefixes(String... prefixes) {
        ModConfig d = new ModConfig();
        for (java.lang.reflect.Field f : ModConfig.class.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isFinal(mod)) continue;
            String n = f.getName();
            boolean hit = false;
            for (String p : prefixes) {
                if (n.startsWith(p) && (n.length() == p.length() || Character.isUpperCase(n.charAt(p.length())) || Character.isDigit(n.charAt(p.length())))) {
                    hit = true;
                    break;
                }
            }
            if (!hit) continue;
            try {
                f.set(this, f.get(d));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    /** Elytra ayarlari (secili desen korunur). */
    public void resetEly() {
        String keep = ely;
        resetPrefixes("ely");
        ely = keep;
    }

    /** Pelerin ayarlari (secili desen korunur). */
    public void resetCape() {
        String keep = cape;
        resetPrefixes("cape");
        cape = keep;
    }

    public void resetGlint() {
        boolean on = glintEnabled;
        resetPrefixes("glint");
        glintEnabled = on;
    }

    public void resetSky() {
        boolean on = skyEnabled;
        resetPrefixes("sky");
        skyEnabled = on;
    }

    /** En cok bu kadar esya gorunumu; tek kare en cok SkinImage.MAX_CODE, animasyon SkinAnim.MAX_CODE karakter. */
    public static final int MAX_ITEM_SKINS = 512;
    /** Tum esya gorunumlerinin toplam karakter siniri (ayar dosyasi / profil kodu sismesin). */
    public static final long MAX_ITEM_SKINS_TOTAL = 24L * 1024 * 1024;

    private static boolean validSkin(String k, String v) {
        if (k == null || v == null || !com.jestron.jestronpriv.itemskin.SkinImage.validId(k)) return false;
        return v.startsWith("v1;") ? v.length() <= com.jestron.jestronpriv.itemskin.SkinImage.MAX_CODE
                : v.startsWith("v2;") && v.length() <= com.jestron.jestronpriv.itemskin.SkinAnim.MAX_CODE;
    }

    private void sanitizeItemSkins() {
        if (itemSkins == null) {
            itemSkins = new java.util.LinkedHashMap<>();
            return;
        }
        // hizli yol: her sey gecerliyse yeni harita kurulmaz (ayar her degistiginde cagrilir)
        if (itemSkins instanceof java.util.LinkedHashMap && itemSkins.size() <= MAX_ITEM_SKINS) {
            long t = 0;
            boolean allOk = true;
            for (java.util.Map.Entry<String, String> e : itemSkins.entrySet()) {
                String k = e.getKey(), v = e.getValue();
                if (!validSkin(k, v) || (t += v.length()) > MAX_ITEM_SKINS_TOTAL) {
                    allOk = false;
                    break;
                }
            }
            if (allOk) return;
        }
        java.util.Map<String, String> clean = new java.util.LinkedHashMap<>();
        long total = 0;
        for (java.util.Map.Entry<String, String> e : itemSkins.entrySet()) {
            if (clean.size() >= MAX_ITEM_SKINS) break;
            String k = e.getKey(), v = e.getValue();
            if (!validSkin(k, v) || total + v.length() > MAX_ITEM_SKINS_TOTAL) continue;
            total += v.length();
            clean.put(k, v);
        }
        if (clean.size() != itemSkins.size() || !(itemSkins instanceof java.util.LinkedHashMap)) itemSkins = clean;
    }

    public void resetTheme() {
        resetPrefixes("menu");
    }

    public void resetFog() {
        boolean on = fogEnabled;
        resetPrefixes("fog");
        fogEnabled = on;
    }

    public void resetHat() {
        ModConfig d = new ModConfig();
        hatScale = d.hatScale;
        hatHeight = d.hatHeight;
        hatTilt = d.hatTilt;
        hatSpeed = d.hatSpeed;
        hatGlow = d.hatGlow;
        hatCustomColor = d.hatCustomColor;
        hatColor = d.hatColor;
        hatRainbow = d.hatRainbow;
        hatHideHelmet = d.hatHideHelmet;
        hatParticles = d.hatParticles;
    }

    public void resetTag() {
        ModConfig d = new ModConfig();
        tagEnabled = d.tagEnabled;
        tagText = d.tagText;
        tagFont = d.tagFont;
        tagColorMode = d.tagColorMode;
        tagColor1 = d.tagColor1;
        tagColor2 = d.tagColor2;
        tagColor3 = d.tagColor3;
        tagAnimSpeed = d.tagAnimSpeed;
        tagBold = d.tagBold;
        tagItalic = d.tagItalic;
        tagUnderline = d.tagUnderline;
        tagStrike = d.tagStrike;
        tagShadow = d.tagShadow;
        tagOutline = d.tagOutline;
        tagOutlineColor = d.tagOutlineColor;
        tagGlow = d.tagGlow;
        tagBgAlpha = d.tagBgAlpha;
        tagBgColor = d.tagBgColor;
        tagScale = d.tagScale;
        tagHeight = d.tagHeight;
        tagAnim = d.tagAnim;
        tagPrefix = d.tagPrefix;
        tagSuffix = d.tagSuffix;
        tagEmojiAnim = d.tagEmojiAnim;
        tagEmojiScale = d.tagEmojiScale;
        tagPrefixImage = d.tagPrefixImage;
        tagSuffixImage = d.tagSuffixImage;
        tagShowF5 = d.tagShowF5;
        tagShowFreecam = d.tagShowFreecam;
        tagShowRealName = d.tagShowRealName;
    }

    public void resetPet() {
        ModConfig d = new ModConfig();
        petScale = d.petScale;
        petMode = d.petMode;
        petLeft = d.petLeft;
        petCustomColor = d.petCustomColor;
        petColor = d.petColor;
        petRainbow = d.petRainbow;
        petParticles = d.petParticles;
        petFirstPerson = d.petFirstPerson;
        petFollow = d.petFollow;
        petBob = d.petBob;
        petReact = d.petReact;
        petGlow = d.petGlow;
    }

    public void resetOutlineFov() {
        ModConfig d = new ModConfig();
        boEnabled = d.boEnabled;
        boColor = d.boColor;
        boAlpha = d.boAlpha;
        boWidth = d.boWidth;
        boRainbow = d.boRainbow;
        boPulse = d.boPulse;
        boFill = d.boFill;
        boFillSame = d.boFillSame;
        boFillColor = d.boFillColor;
        boFillAlpha = d.boFillAlpha;
        boMining = d.boMining;
        fovCustom = d.fovCustom;
        fovSprint = d.fovSprint;
        fovSpeed = d.fovSpeed;
        fovFly = d.fovFly;
        fovBow = d.fovBow;
        fovWater = d.fovWater;
        fovSmooth = d.fovSmooth;
    }

    public void resetGeneral() {
        ModConfig d = new ModConfig();
        mode = d.mode;
        mobKills = d.mobKills;
        killDebug = d.killDebug;
        killStrict = d.killStrict;
        volume = d.volume;
        screenDistance = d.screenDistance;
        screenTitle = d.screenTitle;
        soundChannel = d.soundChannel;
        soundRealism = d.soundRealism;
    }

    public void resetPerformance() {
        ModConfig d = new ModConfig();
        maxSessions = d.maxSessions;
        particleDensity = d.particleDensity;
        respectVanillaParticles = d.respectVanillaParticles;
        effectRenderDistance = d.effectRenderDistance;
        frustumCulling = d.frustumCulling;
        cullEnabled = d.cullEnabled;
        cullEntities = d.cullEntities;
        cullBlockEntities = d.cullBlockEntities;
        cullPlayers = d.cullPlayers;
        cullKeepNamed = d.cullKeepNamed;
        cullQuality = d.cullQuality;
    }

    public enum ViewMode {
        KONUM("§bKonum", "öldüğü noktada"),
        EKRAN("§dEkran", "ekranının önünde"),
        IKISI("§6İkisi", "konum + ekran");

        public final String label;
        public final String hint;

        ViewMode(String label, String hint) {
            this.label = label;
            this.hint = hint;
        }

        public ViewMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static ViewMode parse(String s) {
            if (s == null) return KONUM;
            String n = s.trim().toUpperCase(java.util.Locale.ROOT).replace('İ', 'I');
            for (ViewMode m : values()) if (m.name().equals(n)) return m;
            return KONUM;
        }
    }
}
