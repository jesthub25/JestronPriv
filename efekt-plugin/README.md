# EfektPlugin — Paper / Spigot efekt eklentisi (Jestron)

50 **aura** ve 50 **elytra izi** efekti. Parçacıkları sunucu oluşturup yakındaki oyunculara gönderir:
**mod kurmamış (vanilla) oyuncular da görür.** Efekt Menüsü modunun desenleriyle aynıdır; mod olmadan da çalışır.

- **Sunucu:** Paper, Purpur, Spigot (ve çatalları) **1.20.1 – 1.21.4** — tek jar hepsinde çalışır.
- **Java:** 17 veya üstü.
- **Kurulum:** `EfektPlugin-<sürüm>.jar` dosyasını sunucunun `plugins` klasörüne at, sunucuyu yeniden başlat.

**1.0.0 — ilk sürüm:**
- **50 aura**: Ateş Halkası, Ruh Alevi, Kalp Yağmuru, Yıldız Tozu, Galaksi Sarmalı, Kar Fırtınası, Bahar Bahçesi ... Durunca etrafında döner, yürürken iz bırakır.
- **50 elytra izi**: elytra ile süzülürken iki kanat ucundan renkli iz + desenin kendi parçacığı (Anka'da alev, Buz Kanat'ta kar tanesi, Nebula'da portal ...).
- **Ayrı komutlar:** `/efekt` (menü), `/aura`, `/elytra`, `/efektgizle`, `/efektayar`, `/efektkapat`, `/efektliste`, `/efektver`, `/efektyenile`.
- **Menü:** Auralar, Elytra İzleri, Ayarlar, Hepsini Kapat. Seçili olan parlar; tekrar tıkla kapanır.
- **Oyuncu ayarları:** başkalarının efektlerini gizle, yüz filtresi (kendi auranın kafa hizasındaki parçacıkları sana göstermez), kendi efektini kendinden gizle, animasyon hızı 0.5x – 2x.
- **Sunucu ayarları (`config.yml`):** görüş mesafesi, oyuncu başına tick başına parçacık sınırı, yoğunluk, yayılım, aura / iz aç-kapa, kapalı dünyalar, her öğe için ayrı izin.
- **Akıllı gösterim:** görünmez, izleyici, ölü, uyuyan ve vanish (SuperVanish, PremiumVanish, EssentialsX) oyuncuların efekti gösterilmez; seni göremeyen oyuncuya gönderilmez; ışınlanma hareket sayılmaz.
- **Sürüm uyumu:** 1.20.5'te değişen parçacık adları çalışırken çözülür; sunucuda olmayan parçacık sessizce atlanır.
- **Kayıt:** seçimler `plugins/EfektPlugin/oyuncular.yml` dosyasında; arka planda, yarım kalmayacak şekilde yazılır.

## Komutlar

| Komut | Açıklama |
|---|---|
| `/efekt` | Menüyü açar (`/efekt yardim`: komut listesi) |
| `/aura <ad\|kapat>` | Aura seçer (adsız: aura listesi) |
| `/elytra <ad\|kapat>` | Elytra izi seçer (adsız: iz listesi) |
| `/efektgizle` | Başkalarının efektlerini gizler / gösterir |
| `/efektayar` | Ayarlar menüsü |
| `/efektkapat` | Aurayı ve izi kapatır |
| `/efektliste [aura\|elytra]` | Kimlikleri listeler |
| `/efektver <oyuncu> <aura\|elytra> <ad\|kapat>` | (yönetici) başkasına ayarlar, konsoldan da |
| `/efektyenile` | (yönetici) `config.yml`'i yeniden okur |

## İzinler

| İzin | Varsayılan | Açıklama |
|---|---|---|
| `efektplugin.use` | herkes | Menü ve kendi efektlerin |
| `efektplugin.admin` | op | `/efektver`, `/efektyenile` |
| `efektplugin.aura.<ad>` / `efektplugin.aura.*` | op | Sadece `her-oge-icin-izin: true` iken |
| `efektplugin.elytra.<ad>` / `efektplugin.elytra.*` | op | Sadece `her-oge-icin-izin: true` iken |

## Derleme

```
gradlew build
```
Jar: `build/libs/EfektPlugin-<sürüm>.jar`. Paper API 1.20.1'e göre derlenir (Java 17 hedefi).
