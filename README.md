# JestronPriv

Jestron'un Minecraft projesi. Her parça kendi klasöründe, kendi sürümü ve kendi release'i ile:

| Klasör | Ne | Minecraft | Release etiketi | Dosyalar |
|---|---|---|---|---|
| [`fabric-1.21.4`](fabric-1.21.4) | İstemci modu (Fabric) | 1.21.4 | `mod-1.21.4-v<sürüm>` | `JestronPriv-Mod-<sürüm>-mc1.21.4.jar`, `...-kaynak.zip` |
| [`paper-plugin`](paper-plugin) | Sunucu eklentisi (Paper / Spigot) | 1.20.1 – 1.21.4 | `eklenti-v<sürüm>` | `JestronPriv-Sunucu-<sürüm>.jar`, `...-kaynak.zip` |
| `fabric-1.20.1` | İstemci modu (Fabric) | 1.20.1 | `mod-1.20.1-v<sürüm>` | hazırlanıyor |

- **Mod** (`mods` klasörüne): menüler, kozmetikler, HUD, PvP araçları — sadece kuran oyuncuda çalışır.
- **Eklenti** (sunucunun `plugins` klasörüne): aura ve elytra izi efektleri sunucuda oluşur, **modsuz oyuncular da görür**.

Her klasöre yapılan değişiklikte GitHub Actions o parçayı derler ve kendi release'ini oluşturur / günceller
(sürüm aynıysa jar ve kaynak zip yenilenir). Releases sayfasında her parçanın en son sürümü ayrı durur.
