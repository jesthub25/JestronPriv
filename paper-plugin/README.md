# JestronPriv Sunucu — Paper / Spigot eklentisi (Jestron)

JestronPriv modunun **aura** ve **elytra izi** efektlerinin sunucu tarafı. Parçacıkları sunucu oluşturup yakındaki
oyunculara gönderir: **mod kurmamış (vanilla) oyuncular da görür.** Modun istemci kodundan ayrı, kendi kodudur;
mod ile birlikte ya da tek başına kullanılabilir.

- **Sunucu:** Paper, Purpur, Spigot (ve çatalları) **1.20.1 – 1.21.4** — tek jar hepsinde çalışır.
- **Java:** 17 veya üstü.
- **Kurulum:** `JestronPriv-Sunucu-<sürüm>.jar` dosyasını sunucunun `plugins` klasörüne at, sunucuyu yeniden başlat.

**1.0.0 — ilk sürüm:**
- **50 aura** (modun 50 aurasının aynı desenleri): Ateş Halkası, Ruh Alevi, Kalp Yağmuru, Yıldız Tozu, Galaksi Sarmalı, Kar Fırtınası, Bahar Bahçesi ... Durunca etrafında döner, yürürken iz bırakır.
- **50 elytra izi** (modun 50 elytra görünümünün renklerinde): elytra ile süzülürken iki kanat ucundan renkli iz + desenin kendi parçacığı (Anka'da alev, Buz Kanat'ta kar tanesi, Nebula'da portal ...).
- **Menü:** `/jestron` (kısaca `/jp`) sandık menüsü — Auralar, Elytra İzleri, Ayarlar, Hepsini Kapat. Seçili olan parlar; tekrar tıkla kapanır.
- **Oyuncu ayarları:** başkalarının efektlerini gizle, yüz filtresi (kendi auranın kafa hizasındaki parçacıkları sana göstermez, birinci şahısta görüş kapanmaz), kendi efektini kendinden gizle, animasyon hızı 0.5x – 2x.
- **Sunucu ayarları (`config.yml`):** görüş mesafesi, oyuncu başına tick başına parçacık sınırı (kalabalıkta ağ ve FPS korunur), yoğunluk, yayılım, aura / iz aç-kapa, kapalı dünyalar, her öğe için ayrı izin.
- **Akıllı gösterim:** görünmez, izleyici modunda, ölü, uyuyan ve vanish (SuperVanish, PremiumVanish, EssentialsX) oyuncuların efekti gösterilmez; seni göremeyen oyuncuya (hidePlayer) gönderilmez; ışınlanma hareket sayılmaz.
- **Sürüm uyumu:** 1.20.5'te değişen parçacık adları (REDSTONE → DUST, VILLAGER_HAPPY → HAPPY_VILLAGER ...) çalışırken çözülür; sunucuda olmayan parçacık sessizce atlanır, hiçbir sürümde hata vermez. Menü 1.21'deki InventoryView değişikliğinden etkilenmez.
- **Kayıt:** seçimler `plugins/JestronPriv-Sunucu/oyuncular.yml` dosyasında; arka planda, önce geçici dosyaya yazılıp yerine taşınır (sunucu takılmaz, dosya yarım kalmaz).

## Komutlar

| Komut | Açıklama |
|---|---|
| `/jestron` | Menüyü açar |
| `/jestron aura <ad\|kapat>` | Aura seçer (adsız: aura listesi) |
| `/jestron elytra <ad\|kapat>` | Elytra izi seçer (adsız: iz listesi) |
| `/jestron gizle` | Başkalarının efektlerini gizler / gösterir |
| `/jestron ayar` | Ayarlar menüsü |
| `/jestron kapat` | Aurayı ve izi kapatır |
| `/jestron liste [aura\|elytra]` | Kimlikleri listeler |
| `/jestron ver <oyuncu> <aura\|elytra> <ad\|kapat>` | (yönetici) başkasına ayarlar, konsoldan da |
| `/jestron yenile` | (yönetici) `config.yml`'i yeniden okur |

## İzinler

| İzin | Varsayılan | Açıklama |
|---|---|---|
| `jestronpriv.use` | herkes | Menü ve kendi efektlerin |
| `jestronpriv.admin` | op | `ver`, `yenile` |
| `jestronpriv.aura.<ad>` / `jestronpriv.aura.*` | op | Sadece `her-oge-icin-izin: true` iken |
| `jestronpriv.elytra.<ad>` / `jestronpriv.elytra.*` | op | Sadece `her-oge-icin-izin: true` iken |

## Derleme

```
gradlew build
```
Jar: `build/libs/JestronPriv-Sunucu-<sürüm>.jar`. Paper API 1.20.1'e göre derlenir (Java 17 hedefi).
