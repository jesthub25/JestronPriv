# JestronPriv — Fabric 1.21.4 istemci modu (Jestron)

**1.20.0** — Doku Kütüphanesi (her paketten istediğin doku, mod klasöründe hazır dokular) + eşya editöründe kalıp (rehber doku) + kare kare animasyonlu çizim + yeni araçlar; eşya görünümü editörü (kendin çiz / PNG / kaynak paketi) + 10 şeffaf menü teması + her şey profil koduyla paylaşılır + 50 animasyonlu pelerin + büyü parlaması rengi + özel gökyüzü (kutup ışığı, kayan yıldız) + sis ayarı + kategorili menü + reach / ping / öldürme akışı / totem bildirimi / hitbox + ayar profilleri (kaydet / yükle / kodla paylaş) + 50 animasyonlu şapka + özel isim etiketi (50 font, 3944 emoji, kendi PNG/JPG/GIF görselin) + 50 öldürme efekti + 51 kanat + 50 aura + 50 vuruş partikülü + 50 vuruş stili + 50 yoldaş + 50 nişangah + 50 renk filtresi + PvP hissi + özel nişangah + totem (cihazından **özel totem sesi**) + saat / hava + sürükle-bırak HUD (iksir, eşya sayacı, bilgi, envanter, alınan / atılan, kombo, seri duyurusu) + yerdeki eşya etiketleri + özel seçim kutusu + FOV ayarı + varlık ayıklama + serbest 3. şahıs kamera

**1.21.0 — 50 elytra görünümü, şeffaf kalıp, Buzlu Cam menü:**
- **50 animasyonlu elytra görünümü** (Kozmetik › Elytra): elytra giyince vanilla dokunun yerine geçer; vanilla model ve animasyon (açılma, süzülme, eğilme) aynen kalır. Anka Kuşu, Melek, Ejder Zarı, Gece Yarasası, Monark Kelebeği, Tavus Kuşu, Buz Kanat, Nebula, Kutup Perdesi, Siber Neon, Magma, Yıldırım, Okyanus Dalgası, Şelale, Prizma, Vitray, Altın Zırh, Şövalye, Obsidyen, Boşluk Yırtığı, Ruh Alevi, Zehirli Sarmaşık, Kiraz Bahçesi, Sonbahar Yaprağı, Yusufçuk, Bal Peteği, Kara Melek, Cehennem, Kar Fırtınası, Güneş Patlaması, Tutulma, Takımyıldız, Hologram, Matris, Retro Gün Batımı, Kan Damlası, Mantar, Ametist Jeodu, Zümrüt Rün, Antik Taş, Kum Fırtınası, Şeker Kamışı, Ateşböceği, Plazma, Altın Mermer, Dijital Kamuflaj, Kartal, Kar Baykuşu, Ejder Ateşi, Kozmik Kraliyet.
  - Vanilla elytranın 4 katı çözünürlük (256×128), 41'i animasyonlu, hepsi karanlıkta ışıldayan kısımlı; 23'ünün alt kenarı tüy / zar ucu gibi tırtıklı (kesik).
  - Ayarlar: animasyon hızı, ışıltı ve gücü, renk tonu, akan gökkuşağı, kenar şeridi, iç yüz desenli / koyu astar, kanat uçlarından parçacık (süzülürken daha çok).
  - Izgarada küçük resimler, sağda oyuncu arkadan; önizlemede elytra giyili olmasa da görünür ve kanatlar açılıp kapanır. Profil koduyla paylaşılır.
- **Şeffaf kalıp:** eşya çiziminde kalıp açıkken dama deseni sadece kalıbın doldurduğu piksellerde; dışı tamamen şeffaf, kalıbın biçimi net görünür.
- **Varsayılan menü teması Buzlu Cam:** eski varsayılan (Mor Cam) olan ayarlar bir kez Buzlu Cam'a geçer; sonradan başka tema seçersen korunur.

**1.20.1 — düzeltme ve hız:**
- **Ayar kaydı arka planda:** ekran kapanırken diske yazma oyunu dondurmaz (büyük eşya çizimleriyle bile). Ayar değişmediyse hiç yazılmaz; oyun kapanırken bekleyen kayıt mutlaka tamamlanır; yarım dosya ve yedek (.bak) koruması aynen sürer.
- **Geçici hatalara dayanıklılık:** pelerin ya da eşya görünümü dokusu bir kez hata verirse (ör. F3+T sırasında) artık oturum boyunca kapanmaz; 5 sn sonra yeniden denenir, beklerken eşya vanilla görünür (görünmez eşya olmaz).
- **Çökme koruması:** renk düzenleme ve vurulma rengi kancaları hiçbir durumda kareyi bozamaz.
- **Daha az iş:** menü teması her widget'ta yazı birleştirmeden önbellekten gelir; ayar her değiştiğinde eşya görünümü listesi baştan kurulmaz; çizim editöründe oynatma ve kare şeridi kopyasız / birleşik çizilir; Doku Kütüphanesi küçük resimleri her karede anahtar üretmeden bulunur.

**1.20.0 yenilikleri — Doku Kütüphanesi, eşya editörü: kalıp, animasyon, yeni araçlar:**
- **Kalıp (rehber):** çizim tahtasının altında bir eşyanın dokusu soluk ya da sadece dış çizgisiyle durur, üstünden rahatça çizersin (kaydedilmez, sadece rehber). Kaynak: bu eşya ya da kılıç, kazma, balta, kürek, çapa, topuz, yay, mızrak, olta, totem, altın elma, ender incisi, iksir, ok, çubuk, elytra, nether yıldızı, elmas (◀ ▶ ile seç; aktif kaynak paketindeki hali kullanılır). Görünürlük %20 / 35 / 50 / 70, `T` ile aç-kapa. Boş tahtada kendiliğinden açılır.
- **Animasyonlu çizim:** en çok 32 kare, her kareyi ayrı çizersin. Kare ekle / çoğalt (bir sonraki adımı çizmenin en kolay yolu) / sil / sola-sağa taşı, karelerin küçük resimleri (tıkla seç), kare süresi (50 ms – 1 sn, Shift ile hepsine), **soğan kabuğu** (önceki kare soluk görünür), ▶ Oynat. Oyunda eşya da aynı hızla oynar; geri al kareleri de kapsar. Animasyonlu GIF ve kaynak paketlerindeki animasyonlu dokular (.mcmeta süresiyle) kare kare içe aktarılır.
- **Yeni araçlar:** renk değiştir (bir rengin tüm piksellerini çevirir), gölgelendir (sol koyulaştır / sağ aç, bir sürüklemede bir kez), yatay / dikey çevir, 90° döndür, dış hat (çiziminin çevresine 1 piksel çerçeve), ok tuşlarıyla kaydır, fare altındaki piksel koordinatı ve fırça izi, kısayollar (B E G I L R C D M T, Boşluk, `,` `.`, Shift+`,` `.`).
- **Doku Kütüphanesi (Eşya Görünümleri › ❖ Doku Kütüphanesi, editörde ❖ Kütüphane):** istediğin kaynak paketinden istediğin dokuyu al.
  - Kaynaklar: **mod klasörü** (`config/jestronpriv/items`), `resourcepacks` klasöründeki her paket (.zip / klasör), sürükle-bıraktığın .zip ve **oyunun aktif dokuları**. Paketin tüm eşya / blok dokuları küçük resimli ızgarada, arama ve Eşya / Blok süzgeci.
  - Seçtiğin dokuyu: **istediğin eşyaya uygula** (ör. bir paketin kılıcını çubuğa), editörde aç, **tahtaya yükle** ya da **kalıp yap** (her paketten kalıp), **mod klasörüne kopyala**. Animasyonlu dokular kareleri ve süreleriyle gelir.
  - **Mod klasörü = hazır dokular:** oraya attığın (ya da ekrana sürüklediğin) her PNG / JPG / GIF hazır doku olarak kütüphanede durur. Adı bir eşyanın adıysa (`diamond_sword.png`, `totem_of_undying.gif`, başka mod: `modadi__esya.png`) **★ İsme göre uygula** ile tek tuşta o eşyalara geçer. Kopyalarken kendi dosyaların asla ezilmez.
- **PNG kaydet:** çizimini `config/jestronpriv/items` klasörüne PNG olarak (animasyonda şerit + `.mcmeta`, kaynak paketine hazır) dışa aktarır.
- **Küçük ekran düzeni:** varsayılan pencere ve GUI ölçeği 4'te (427×240 / 480×270) araçlar simgeye döner, hiçbir şey taşmaz.
- **Düzeltmeler / hız:** editör nereden kapanırsa kapansın canlı önizleme temizlenir; tahta satır satır birleşik çizilir (64×64'te bile az çizim), oyunda sadece değişen kare yeniden yüklenir, geri al hafızası değişmeyen kareleri kopyalamaz; kaynak paketi klasörü her tıklamada değil bir kez taranır; eşya listesindeki durum yazısı her karede yeniden çözülmez; çok büyük animasyon kaydedilmeden önce uyarır, toplam görünüm boyutu sınırlı (ayar dosyası şişmez). Eski (1.19) çizimler ve profil kodları aynen çalışır.

**1.19.0 yenilikleri — eşya görünümleri, menü temaları, her şey paylaşılır:**
- **Eşya Görünümleri (Kozmetik › Eşya Görünümleri):** oyundaki tüm eşyalar aramalı bir ızgarada (Türkçe / İngilizce ad, kimlik). Birini seç:
  - **Kendin çiz:** piksel editörü — kalem, silgi, kova, damlalık, çizgi, dikdörtgen, ayna (simetri), fırça 1-4, ızgara, geri al / ileri al (Ctrl+Z / Ctrl+Y, 64 adım), 16 / 32 / 64 çözünürlük, 32 renkli palet + son renkler + #RRGGBB + ton / doygunluk / parlaklık / saydamlık. Çizdikçe eşya canlı değişir; Ctrl+S kaydeder.
  - **Yükle:** PNG / JPG / GIF / BMP ya da bir **kaynak paketi (.zip / klasör)** sürükle-bırak; paketteki o eşyanın dokusu kendiliğinden bulunur. `resourcepacks` klasöründeki paketler editörde tek tıkla seçilir. Animasyonlu dokuda ilk kare, büyük görselde 64'e küçültme, kare olmayanda ortalama.
  - "Şu anki dokudan başla": vanilla / aktif paket dokusunun üstüne çiz. "Vanilla'ya döndür", hepsini aç / kapat.
  - Elde, envanterde, yerde, çerçevede görünür; 3B (1 piksel kalınlık, kenarlar), büyülü eşyada parlama korunur. Blok eşyalarına çizilen düz görünüm de doğru durur. Sadece senin ekranında.
- **Menü Teması (Ayarlar › Menü Teması):** arkası şeffaf düğmelerle 10 tasarım — Mor Cam, Buzlu Cam, Neon Gece, Okyanus, Zümrüt, Gün Batımı, Altın Lüks, Kızıl, Siber, Minimal — + Klasik (vanilla). Saydamlık ve özel vurgu rengi. Düğmeler, kaydırıcılar (değere kadar dolar), yazı kutuları, paneller, ızgara hücreleri ve kartlar temaya uyar. Oyunun kendi menülerine dokunulmaz.
- **Her şey profil koduyla paylaşılır:** kodu / profil dosyasını alan kişide ayarlar birebir aynı olur — artık çizdiğin eşya görünümleri, menü teması, **özel totem sesi ve isim etiketi görselleri (dosyalarıyla birlikte)**, son emojiler dahil. Gelen dosyalar arkadaşının bilgisayarına yazılır; aynı adda kendi dosyası varsa ezilmez (yanına kopya). Çok büyük ses dosyası (uzun video) oyunun çaldığı kadarıyla WAV'a çevrilerek eklenir. Ses / görsel varsa kod uzar: o zaman profil dosyasını gönder.
- Yeni profil bölümü: **Eşya Görünümleri**.

**1.18.0 yenilikleri — pelerin, büyü parlaması, gökyüzü, sis:**
- **50 animasyonlu pelerin (Kozmetik › Pelerin):** Gece Yarısı, Galaksi, Alev, Buz Kristali, Lav Akıntısı, Okyanus, Orman, Gün Batımı, Kutup Işığı, Şimşek, Ejderha Pulu, Zümrüt Pul, Kraliyet, Savaşçı, Satranç, Zebra, Kaplan, Leopar, Neon Izgara, Devre Kartı, Kod Yağmuru, Gökkuşağı, Pastel Bulut, Kalpler, Yıldız Tozu, Ay Işığı, Güneş, Kiraz Çiçeği, Sonbahar, Kar Tanesi, Zehir, Kızıl Ay, Altın, Gümüş, Elmas, Ametist, Obsidyen, Boşluk, Kor, Ruh Ateşi, Kamuflaj, Piksel Sanatı, Mozaik, Kilim, Çini, Dalga Ritmi, Hologram, Prizma, Mermer, Bambu.
  - Dokular vanilla pelerinin 4 katı çözünürlükte (40×64) oyun içinde üretilir; 35'i canlı (alev yükselir, yıldızlar göz kırpar, kod yağar, şimşek çakar...). Parlayan kısımlar karanlıkta ışıldar.
  - **Kumaş fiziği:** 8×14 bükülen yüzey: koşunca açılır, zıplayınca savrulur, aşağı ilerleyen rüzgâr dalgası, enine dalgalanma, kenar kıvrılması; 1 piksel kalınlık, iç yüz ve kenarlar ayrı. Sırtın içine asla girmez (test edildi).
  - Ayarlar: uzunluk, genişlik, rüzgâr, fizik gücü, animasyon hızı, ışıltı ve gücü, renk tonu kaydırma, gökkuşağı (+ hız), kenar şeridi, iç yüz rengi (otomatik / özel), stile uygun parçacık (+ miktar), kanatla gizle. Elytra giyince gizlenir (vanilla gibi). Izgarada küçük resimler + sağda kendi karakterin arkadan.
- **Büyü parlaması (Görünüm › Büyü Parlaması):** tek renk, iki renk, akan gökkuşağı, renk döngüsü ya da vanilla mor; eşya ve zırh / varlık ayrı ayarlanabilir. Hız, parlaklık, desen boyutu, eğim, döngü hızı, nabız. Üstte canlı eşya önizlemesi. Kaynak paketinin parlama dokusu korunur (sadece yeniden boyanır).
- **Özel gökyüzü (Görünüm › Gökyüzü):** gece kuzey göğünde dalgalanan **kutup ışığı** (Klasik / Buzul / Ateş / Pembe / Gökkuşağı / özel renk; parlaklık, akış hızı, yükseklik, gündüz de), **kayan yıldızlar** (sıklık, kuyruk boyu, renk, "şimdi göster"), güneş ve ay boyutu, ay evresi (sadece görünüş), yıldız parlaklığı, gündüz yıldızları, gün batımı rengi (doğal / özel / gökkuşağı / kapalı + güç), gök rengi tonu (gece karanlık kalır). Nether / End ve su altında çizilmez; en düşük görüş mesafesinde bile kırpılmaz.
- **Sis (Görünüm › Sis):** renk (doğal / özel / gökkuşağı, karışım), mesafe, yumuşaklık, arazi sisini kaldırma, gök sisine uygulama, Nether / boss kalın sisi (doğal / azalt / kaldır), boyut bazında aç-kapa. **Su, lav, toz kar, körlük ve karanlık sisi asla değişmez.**
- Hepsi ayar profillerine dahil (pelerin → Kozmetik, diğerleri → Görünüm & Kamera).

**1.17.0 yenilikleri — kategorili menü ve PvP araçları:**
- **Kategorili menü:** K menüsü artık 6 kategori: Kozmetik · PvP · Öldürme & Totem · HUD & Eşyalar · Görünüm & Kamera · Ayarlar. Kategoriye girince sadece o bölümün kartları; Esc / Geri bir üst seviyeye döner. Ekran kalabalığı bitti.
- **Reach göstergesi:** vuruşunun gözünden hedefin çarpışma kutusuna uzaklığı (sunucunun ölçtüğü gibi). Sadece sunucunun onayladığı vuruşlar sayılır. Renk kademeleri, son 10 vuruş grafiği, ortalama / en uzak / vuruş sayısı, 1-3 ondalık, kaybolma süresi, moblar aç/kapa.
- **Ping:** anlık ölçüm (vanilla'nın F3 ağ grafiğinde kullandığı ping isteğiyle, saniyede bir; sekme listesi 30 sn'de bir güncellenir). Sinyal çubukları, son 60 ölçümün grafiği, en düşük / ortalama / en yüksek / titreme, rakibin pingi, ani sıçrama uyarısı, ölçüm aralığı. Bilgi satırındaki ping de artık anlık.
- **Öldürme akışı:** "Öldüren [silah] Ölen" satırları; silah mesajdaki eşyadan, yoksa ölüm nedeni simgesi (ok, patlama, düşme, lav...). Senin öldürmelerin yeşil, ölümün kırmızı; öldürenin serisi ×N. Filtre (hepsi / oyuncu öldürmeleri / benimkiler), satır sayısı, süre, sıralama, "Sen" yazımı, ölüm mesajlarını sohbetten gizleme (öldürme efekti etkilenmez). Özel eklenti mesajlı sunucularda doğrulanmış öldürmelerin de eklenir.
- **Totem bildirimleri:** yakındaki oyuncu totem patlatınca "[yüz] Oyuncu ✦ TOTEM ×3 · 2 sn"; yeni totemde satır nabız atar, ölürse "☠ öldü". Renk totem sayısına göre, mesafe sınırı, kendi totemin / moblar, ses, satır sayısı, süre.
- **Hitbox:** F3+B'nin ayarlanabilir hali: oyuncular / moblar / kendin (F5), renk, kalınlık, opaklık, göz hizası, bakış yönü, nişan aldığın ve hasar alan için ayrı renk, saydam dolgu, mesafe. Duvar arkasındakiler ve görünmez / izleyici oyuncular asla gösterilmez.
- Hepsi HUD düzenleyicide sürükle-bırak, tekerlekle boyut; ayar profillerine dahil.

**1.16.0 yenilikleri — ayar profilleri, her şey 50+, kalite:**
- **Ayar Profilleri (yeni menü kartı):** ayarlarını adla kaydet, tek tıkla yükle, üzerine yaz, sil.
  - **Paylaşım kodu:** seçtiğin bölümler kısa bir koda (`JPRIV1:...`) dönüşür ve panoya kopyalanır; arkadaşın "Panodaki kodu içe aktar" ile alır. Ya da profil dosyasını (`config/jestronpriv/profiles`) gönder, ekrana sürükle-bırak.
  - **Birebir kopya:** tüm bölümler seçiliyken kodu yükleyen kişide ayarlar seninkinin aynısı olur (HUD yerleri / boyutları, sesler, el görünümü, vuruş stili ve ayarları, kozmetikler, kamera, performans...; 300+ ayar, test edildi). Kod sadece varsayılandan farklı ayarları taşır, bu yüzden kısadır.
  - **Bölüm seçimi:** Kozmetik · Vuruş & PvP · Öldürme Efekti · Görünüm & Kamera · HUD · Performans. Kaydetme, paylaşma ve yükleme sadece seçili bölümlere uygulanır (ör. sadece arkadaşının kozmetiklerini al, kendi HUD'un kalsın).
  - **Güvenli:** her yüklemeden önce otomatik yedek ("Son yüklemeyi geri al"); gelen her değer sınırlarla doğrulanır, bozuk alanlar atlanır, sıkıştırma bombasına karşı korumalı. Kişisel dosyalar (özel totem sesi yolu, etiket görselleri) asla paylaşılmaz.
  - **Ayar dosyası kurtarma:** `jestronpriv.json` bozulursa artık silinmez (`.bozuk` olarak saklanır) ve son sağlam kayıttan (`.bak`) devam edilir.
- **Her katalog en az 50:**
  - **Vuruş partikülü 50:** + Mürekkep, Havai Fişek, Piksel, Okyanus, Kum Fırtınası, Plazma, Gölge, Şeker, Ateş Böcekleri (her biri kendi vuruş ve kritik sesiyle).
  - **Vuruş stili 50:** + Zikzak, Balyoz, Eskrim Riposte, Orak, Balta, Yumruk, Üçlü Saplama, Yarma, Süpürme, Savunma Kesiği ve sinematik Tırpan, Bumerang, Yel Değirmeni, Girdap Kesiği, Yükselen Ejder, Kılıç Gösterisi.
  - **Yoldaş 50:** + kedi, köpek, tavşan, penguen, baykuş, arı, kelebek, japon balığı, ahtapot, denizanası, yarasa, kuş, tilki, panda, balçık, hayalet, kafatası, balkabağı, kardan adam, robot, UFO, roket, gezegen, güneş, hilal, yağmur / fırtına bulutu, ateş topu, kar tanesi, elmas, altın para, enerji küpü, prizma, kum saati, büyü kitabı, uçan kılıç, fener, mantarcık, çiçek, örümcek, ejder yumurtası, kuzu. Hepsi animasyonlu (göz kırpar, kanat çırpar, kuyruk sallar...). Izgaradan seçim ekranı eklendi.
  - **Nişangah 50:** + halka / kare / elmas / üçgen / altıgen / yıldız / kalp / radar / keskin nişancı / gülümseme / parantez / kum saati ve hareketli "Dönen Çarpı", "Nabız Halkası"... Hepsi piksel simetrik (test edildi).
  - **Renk filtresi 50:** + altın saat, mavi saat, gün batımı, okyanus, orman, mevsimler, pastel, soluk film, polaroid, teknikolor, çapraz işlem, teal & turuncu, Matrix, cyberpunk, vaporwave, synthwave, Nether, End, ters renk, röntgen, termal, noir, HDR, retro konsol...
  - **İsim etiketi fontu 50:** + Canavar, Su Birikintisi, Plak, Labirent (Türkçe karakterli).
- **Kalite / performans:** parlayan vuruş partiküllerinde yumuşak ışıma (bloom) ve sıcak çekirdek; nişangah halkası satır satır çizilir (piksel piksel yerine, çok daha az çizim çağrısı); bulunamayan parçacık türleri her tick yeniden aranmaz; ana menü profil sayısını her karede diskten okumaz.

**1.15.0 yenilikleri — şapkalar ve özel isim etiketi (2 yeni menü kartı):**
- **50 animasyonlu şapka (Şapkalar):** Kral Tacı, Kraliçe Tacı, Melek Halesi, Şeytan Boynuzları, Silindir, Sihirbaz, Cadı, Kovboy, Korsan, Aşçı, Viking Miğferi, Pervaneli Şapka, Kedi / Tavşan / Panda / Tilki Kulakları, Çiçek Tacı, Uzaylı Antenleri, Noel, Beyzbol, Kışlık Bere, Mezuniyet Kepi, Fes, Samuray Kabutosu, Firavun Başlığı, Neon Hale, Kristal / Ateş / Buz Tacı, Yıldız Yörüngesi, Satürn, Yağmur Bulutu, Gökkuşağı Bulutu, Kurbağa, Mantar, Lastik Ördek, Ejderha Boynuzları, Geyik Boynuzu, Unicorn Boynuzu, Şövalye Sorgucu, Polis, Denizci, Fötr, Melon, Hasır, DJ Kulaklığı, Glitch Halesi, Kara Delik, Kalp Halesi, Ruh Alevi. Her biri kendi 3B modeli ve hareketiyle (dönen, sallanan, nabız atan, parlayan); yürürken / koşarken canlanır.
  - Izgara + sağda kendi karakterinle canlı önizleme. Ayarlar: boyut, yükseklik, eğim, animasyon hızı (0 = sabit), parıltı (karanlıkta ışıldayan taş / hale / alev), gökkuşağı, özel renk (#RRGGBB), stile uygun parçacık, kask takınca gizle ya da kaskın üstüne çık.
  - Oyuncu modelinin kafasına bağlıdır: kafa dönünce, eğilince şapka da döner. Hepsi süreklilik / NaN / sınır testinden geçer, kurulumu ortalama ~45 µs.
- **Özel isim etiketi (İsim Etiketi):** F5'te ve serbest kamerada kendi başının üstünde görünür (ikisi ayrı açılıp kapanır).
  - İstediğin metin (boşsa kendi adın), **46 font** (Minecraft, Unifont, Büyü dili, İllager dili + Türkçe karakterli 42 yazı tipi: Piksel, Retro Oyun, Terminal, Fütüristik, Siber, Gotik, El Yazısı, Dans, Kraliyet, Korku, Neon Çizgi, Glitch, Balon...).
  - **16 renk modu** (tek renk, 2 / 3 renk gradyan, gökkuşağı, akan gökkuşağı, nabız, parıltı süpürmesi, ateş, buz, neon, parti, altın, okyanus, zehir, gün batımı, galaksi), **8 harf hareketi** (dalga, zıplama, süzülme, titreme, nefes, daktilo, salıncak), kalın / italik / altı çizili / üstü çizili / gölge / kontur (renk seçilir) / parlak, arka plan opaklığı ve rengi, boyut, yükseklik.
  - **Başına ve sonuna emoji:** telefondaki emoji klavyesinin tamamı (**3944 emoji**, sadece yüzler değil: insanlar, hayvanlar & doğa, yiyecek, seyahat & yerler, etkinlik, nesneler, semboller, bayraklar). Kategoriler, son kullanılanlar, Türkçe / İngilizce arama, sağ tıkla ten rengi. Emoji hareketi (zıplama, dönme, nabız, sallanma) ve boyutu.
  - **Kendi görselin:** `config/jestronpriv/icons` klasörüne (ya da menüye sürükle-bırak) **PNG, JPG, GIF (animasyonlu), BMP** at; başa / sona koy. GIF'ler oynar. Görseller arka planda çözülür, oyunu dondurmaz.

**1.14.2 — kanat formu (melek kemeri):**
- Tüm kanatlar artık düz bir yelpaze gibi açılmıyor; dış tüyler **yukarı doğru kemerlenip** melek kanadı hilali oluşturuyor (dihedral kavis). Yüzeyde ayrıca dışa kıvrım (ustten C) ve alt kenar kubbesi var. Sadece **şekil** değişti; renk / doku / tür aynı.
- Çırpmada uçlar daha çok eğilir (düz çırpma yerine 3B dalga).
- Kemer kanat türüne göre ölçekli: tüy / enerji kanatlarda belirgin melek kavisi, kristal / zar kanatlarda daha zarif, kelebekte hafif. Katlanınca kavis düzelir (sırta yaslanır).
- Büyük kanatlarda kavis sınırlanır, oyuncuyu/kamerayı aşmaz. Sınır kutusu, süreklilik, ayna simetrisi ve performans testleri korunur (51 kanat sorunsuz).

**1.14.1 — optimizasyon ve düzeltmeler:**
- Öldürme efektleri çizilirken her parça için karede ~17 geçici nesne üretiliyordu; artık 5 (ara karede) ya da hiç (sabit karede). Sonuç birebir aynı (200.000 rastgele dönüşümde doğrulandı), efekt sırasında takılma ve çöp toplayıcı yükü azaldı.
- Yerdeki eşya etiketleri ve hitbox kutuları ekran dışındaysa (arkanda, yanlarda) hiç kurulmaz; yazı genişliği tick'te bir kez ölçülür.
- Blok varlığı ayıklama kapalıyken her sandık / tabela için yapılan gereksiz sorgu kaldırıldı.
- Bellek sızıntısı düzeltmeleri: sunucudan çıkınca eski dünya (yerdeki eşya havuzu, varlık ayıklama, biyom bilgisi) artık bellekte tutulmuyor.
- Bilgi satırı: Nether / Dünya geçişinde aynı koordinatta eski biyom adı kalabiliyordu, düzeltildi.
- Özel totem sesi: yükleme sürerken başka dosya seçilince yenisi 5 sn beklemeden hemen yükleniyor.
- Paket ve çizim kancalarının hepsi hatadan yalıtıldı (hasar paketi, el görünümü, kritik yayıcısı, vuruş süresi): beklenmedik bir durumda bağlantı kopmaz, oyun çökmez.

**1.14.0 yenilikleri — vuruş düzeltmesi, 10 yeni vuruş stili, 20 yeni partikül, özel totem sesi:**
- **Düzeltme — "önce klasik vuruş, sonra seçili stil":** vanilla, vuruşun ilk karesinde ilerlemeyi 0'da tutuyor ve saldırı bekleme göstergesi eli aşağı daldırıyordu; bu yüzden her vuruş bir an klasik görünüyordu. Artık vuruş ilerlemesi ilk kareden itibaren karede araya girilerek kendi zamanlamamızla hesaplanır, seçili stil tıkladığın anda başlar. Saldırı bekleme dalışı varsayılan kapalı (istersen açılır), eşya iniş animasyonu sadece eşya değiştirince oynar.
- **10 yeni vuruş stili:** Hilal Kesik, Yükselen Kesik, Haç Kesiği, Çifte Saplama, İnfaz, Ronin Çekişi, Hayalet Adım ve sinematik **Kasırga** (ekseninde dönüş), **Hortum** (bıçak ekseninde 1080°), **Ay Işığı** (360° takla). Toplam 34 stil; hepsi sarsıntı / ekran dışı / süreklilik testlerinden geçer.
- **20 yeni vuruş + kritik partikülü** (her biri kendi dokusu, hareketi ve 2 + 3 katmanlı sesiyle): Tüy, Hilal, Papatya, Buz Kristali, Girdap, Taç, Rün Halkası, Yarasa, Pati, Yonca, Kılıç İzi, Volkan, Zehir, Galaksi, Kara Delik (içe çeker), Balon, Zümrüt, Ejder Ateşi, Büyü, Patlama. Toplam 41 stil; atlas 11 yeni çizimle genişledi.
- **Özel totem sesi (Totem & Zaman → Özel Totem Sesi):** cihazındaki bir **video (MP4, MOV, 3GP) ya da ses dosyasını (M4A, MP3, WAV, OGG)** seç, totem patlayınca o ses çalsın.
  - **Klasörü aç** butonu `config/jestronpriv/sounds` klasörünü açar; dosyayı içine at ya da doğrudan **menü penceresine sürükle-bırak** (otomatik kopyalanır ve seçilir). **Dosya** butonu klasördeki dosyalar arasında geçer.
  - Ayarlar: **ses seviyesi** (%0–200, yumuşak sınırlayıcıyla cızırtısız), **başlangıç saniyesi** (0–180), **süre** (0 = sonuna kadar, en çok 60 sn), **hız / perde** (0.5x–2x), **giriş / çıkış yumuşatma**, **kimde çalsın** (herkes / sadece ben / sadece başkaları — başkasınınki onun yerinden 3D olarak, 32 blok), **vanilla totem sesini sustur**, **üst üste çal**. **Sesi dene** / **Durdur** ile anında dinlenir.
  - Ses arka planda bir kez çözülür (oyunu dondurmaz), oyunun kendi ses motorundan çalar ve "Oyuncular" ses ayarına uyar. Durum satırı (Hazır · 12.4 sn · stereo / Hata: …) menünün üstünde görünür. Dosya değişirse kendiliğinden yeniden yüklenir; özellik kapalıyken bellek boşaltılır.
  - Çözücü saf Java'dır (ek kurulum yok): MP4 / M4A / MOV / 3GP içindeki AAC ses, MP3, WAV (8 / 16 / 24 / 32 bit, float) ve OGG Vorbis. Videonun görüntüsü okunmaz, sadece ses izi. Bkz. `THIRD_PARTY_NOTICES.txt`.

**1.13.0 yenilikleri — HUD, envanter & eşyalar, evcil yoldaş, seçim kutusu, FOV, varlık ayıklama (4 yeni menü kartı):**
- **Sürükle-bırak HUD düzenleyici:** tüm HUD öğeleri (bilgi satırı, envanter, iksir paneli, eşya sayacı, alınan / atılan, **kombo sayacı**, **seri duyurusu**) fareyle taşınır, tekerlekle %50–200 boyutlanır, sağ tıkla açılıp kapanır, ok tuşlarıyla 1 piksel (Shift ile 10) kaydırılır. Ekran ortasına ve kenarlara mıknatıs gibi hizalanır, kılavuz çizgisi çıkar. Konum ekranın oranı olarak saklanır: her çözünürlükte ve arayüz ölçeğinde doğru yerde kalır; sağ yarıdaki öğeler içeriğini sağa yaslar. Boş öğeler düzenleyicide örnekle gösterilir.
- **İksir paneli:** ikon, ad + seviye (II, III…), kalan süre ve süre çubuğu; bitmeye yakın hızlanarak yanıp söner (eşik ayarlı). Kompakt mod, renk (etki rengi / iyi-kötü / beyaz), sıralama (süre / tür / ad), fener etkileri, arka plan; vanilla sağ üst ikonları gizlenebilir.
- **Eşya sayacı:** 13 eşya seçilebilir (totem, inci, ok — tüm türler, altın / büyülü elma, rüzgâr küresi, fırlatılabilir iksir, ağ, havai fişek, tecrübe şişesi, End kristali, koro meyvesi, obsidyen). Zırh ve sol el dahil toplam; inci / rüzgâr küresi bekleme süresi ikonda karartı + saniye; son totem uyarısı; alt alta ya da yan yana.
- **Bilgi satırı:** FPS ve ping (iyi / orta / kötü renkli), koordinat (0–2 ondalık), yön (8 yön + eksen), Nether ↔ Dünya koordinatı, biyom, hız (blok/sn), gerçek saat, oyun günü / saati; tek satır ya da liste, etiket / değer rengi ayarlı. Yazılar tick'te bir kez üretilir.
- **Envanter HUD:** ana envanter 3×9, isteğe bağlı hızlı çubuk (seçili yuva vurgulu) ve zırh + sol el sütunu; adet ve dayanıklılık görünür; boşken / ekran açıkken gizlenebilir.
- **Alınan / atılan eşyalar:** "+12 Elmas" (yeşil), "−3 Kırık Taş" (kırmızı) ikonla, nadirlik renginde ad. Alınanlar sunucunun "eşyayı aldı" paketinden tam adetle; atılanlar Q, Ctrl+Q, envanterden atma ve pencere dışına bırakmadan. Aynı eşya süre dolmadan gelirse satır birleşir ve sayı atar; satır yandan kayarak gelir, süresi bitince söner. Süre, satır sayısı, yön ayarlı.
- **Yerdeki eşyalar:** üstünde ikon + ad + toplam adet (nadirlik rengi), yan yana aynı eşyalar tek etikette birleşir, isteğe bağlı hitbox kutusu, "sadece baktığım" modu, menzil (4–48), etiket sınırı (en yakınlar), boyut ve arka plan. Derinlik testlidir: duvar arkasındaki eşyayı göstermez (ESP değildir).
- **Evcil yoldaş (sadece sende):** Ruh Işığı, Kristal, Mini Ejderha, Peri, Göz, Drone, Yıldız, Kalp — tamamen kodla çizilen animasyonlu modeller (kanat çırpma, kuyruk sallama, göz kırpma, pervane, iki vuruşlu kalp atışı, ışık haleleri). Omzunda / yanında / arkada / etrafında; yaylı takip (koşarken / uçarken geride kalmaz, ışınlanınca yanında belirir), hıza göre eğilme, baktığın yöne bakma. Vuruşta zıplar, öldürmede dönerek sevinir (ses + parçacık). Renk / gökkuşağı, boyut, takip hızı, süzülme, parlaklık, parçacık izi, 1. şahısta göster ayarlı.
- **Blok seçim kutusu:** renk / #RRGGBB / kenarlar boyunca akan gökkuşağı, nabız, gerçek piksel kalınlığı (1–10, yüksek çözünürlükte orantılı), opaklık; dolgu yok / bakılan yüz / tüm blok; **kırma ilerlemesi** (dolgu ortadan büyür, renk kırmızıdan yeşile).
- **FOV etkileri:** koşma, hız / yavaşlık, uçma, yay germe, su / lav ayrı ayrı %0–150 (100% = birebir vanilla); geçiş anında / normal / yumuşak / çok yumuşak; "PvP" ve "Sabit FOV" ön ayarları. Dürbün aynen kalır.
- **Varlık ayıklama (Performans):** duvar arkasındaki varlıklar ve blok varlıkları (sandık, tabela, sancak, kafa…) çizilmez; kalabalık yerlerde FPS artar. Karede ışın bütçeli, gizlenenler her karede yeniden denenir (geç belirme olmaz). Parlayanlar, binilenler, sen, çok büyük varlıklar, fener ışını gibi uzaktan görünenler, istenirse oyuncular ve isimli moblar asla ayıklanmaz; kamera blok içindeyse o kare kapalı. Hassasiyet ayarlı, son karede ayıklanan sayısı ekranda.
- Sağlamlık: her tick modülü ve her dünya çizimi ayrı ayrı hatadan yalıtıldı (bir modülde hata olursa o modül atlanır, oyun çökmez, günlüğe en fazla 5 kez yazar).

**1.12.0 yenilikleri — PvP hissi, nişangah, totem, saat / hava (3 yeni menü kartı):**
- **Hasar sayıları:** vurduğun hedeften uçan sayılar, sunucunun onayladığı gerçek can farkından hesaplanır (tahmin değil). Kalp (-3.5 ❤) ya da can (-7) biçimi; hasara göre renk (beyaz → sarı → turuncu → kırmızı) ya da sabit renk; kritikte altın ✦ ve büyük, öldürmede kızıl ☠ ve daha büyük. Sayı kameraya döner, konturlu (her zeminde okunur), hafif taşarak açılır, yana savrulup düşer ve söner. Sunucu canı gizliyorsa yine de ✕ / ✦ işareti çıkar (kapatılabilir). Ok / mızrak isabetleri de sayılır.
- **Kombo sayacı:** hasar almadan art arda isabetler. Her isabette sayı atar ve **nota yarım ton yükselir**; 5 / 10 / 15 / 20'de renk ve katmanlı ödül sesi; altta kalan süre çubuğu. Hasar alınca "Kombo kırıldı" ile biter. Konum (nişangah altı / üst / sağ), boyut, süre (1 – 6 sn), ses ve ses seviyesi ayarlanır. "Komboyu dene" otomatik 10'lu dizi oynatır.
- **Seri öldürme duyurusu:** Çifte / Üçlü / Dörtlü / Beşli Öldürme, Ölüm Makinesi ×N; ölmeden seri: Seri Başladı (3), Durdurulamaz (5), Katliam (7), Efsanevi (10), Tanrısal (15), Ölümsüz (20+); İlk Kan. Büyüyerek gelen parlayan başlık, harflerde akan ışık (en üst seviyede gökkuşağı), kenarlarda seviye renginde parlama, kurban adı + seri, seviyeye göre katmanlı ses (boru, çan, havai fişek, ejderha). Ölünce seri sıfırlanır. Türkçe / English, çoklu öldürme süresi ayarlanır.
- **Vurulma rengi:** vurduğun hedef vanilla kırmızı yerine seçtiğin renkte (ya da gökkuşağı) yanıp söner; yoğunluk ayarlı, istenirse herkese. Vanilla kırmızının kendisine dokunulmaz: renk, oyunun overlay dokusunun boş satırlarına yazılır ve sadece seçilen varlıklar o satırı kullanır.
- **Özel nişangah:** 9 şekil (Artı, Artı + Nokta, Nokta, Daire, Halka + Nokta, Çarpı, T, Köşeli, Ok), uzunluk, kalınlık, boşluk, opaklık, renk / #RRGGBB / gökkuşağı, siyah kontur, orta nokta; **dinamik açılma** (yürürken, havadayken, saldırı dolarken) ve **isabet işareti** (beyaz vuruş, altın kritik, kızıl öldürme). Vanilla nişangahla piksel piksel aynı merkez; saldırı göstergesi aynen kalır. Ayar ekranında açık ve koyu zeminde 1x + 3x canlı önizleme.
- **Totem animasyonu:** biri totem patlatınca altın-yeşil ışık halkaları, gövde etrafında yükselen sarmal, ışıltılar ve katmanlı kutsal ses (üst üste totemlerde perde yükselir); başının üstünde **"✦ TOTEM ×N ✦" sayacı** (ölünce sıfırlanır). İsteğe bağlı: sohbete yaz, vanilla totem partikülünü gizle, ekrandaki büyük totemi gizle. Boyut ayarlı, "Totemi dene" ile önizleme.
- **Saat ve hava (sadece sende):** Sunucu / Sabit / Hızlı döngü (1x – 200x) saat modu, 6 hazır saat (gün doğumu … gece yarısı) ve dakikalı saat kaydırıcısı; hava Sunucu / Açık / Yağmur / Fırtına, birkaç saniyede yumuşak geçiş. Sunucuya hiçbir şey gitmez, sunucunun saati arkada doğru işlemeye devam eder; kapatınca anında gerçeğe döner. Nether / End'de hava etkisizdir.
- Ana menüye 3 kart eklendi (PvP Hissi, Nişangah, Totem & Zaman); geniş ekranda 3 sütun, sığmazsa kart alanı kaydırılır. Her sayfada "Varsayılana Sıfırla".
- Her PvP olayı ayrı ayrı hatadan yalıtılmıştır: bir özellikte sorun olsa bile diğerleri ve oyun etkilenmez. Hiçbir yeni ağ paketi, kayıt ya da sunucu özelliği yoktur; her sunucuda çalışır.

**1.11.0 yenilikleri — Vuruş ve kritik sesleri:**
- 20 vuruş partikülü stilinin her birine kendine özel **vuruş tonu** (kısa, net, 2 katman) ve daha dolgun **kritik tonu** (3-4 katman, tick gecikmeli kuyruk) eklendi: köpük patlaması, kelebek kanat sesi + ametist çınlaması, kalp zili, yıldız ışıltısı, alev püskürmesi, ruh fısıltısı, buz kırılması, elmas çatırtısı, şimşek çakması, yaprak hışırtısı, nota bloğu arpeji (Notalar: her vuruşta pentatonik bir nota, kritikte akor), ender ışınlanması, para şıkırtısı, kemik kırılması, rüzgâr patlaması, neon vızıltısı, havai fişek.
- Hepsi oyunun kendi sesleri (1.21.4 ses listesinde otomatik doğrulanır); her vuruşta hafif doğal perde farkı. Aynı anda çok hedefe vurunca (süpürme) ses patlaması olmaz.
- Vuruş Partikülleri ekranında **Ses** aç/kapat ve **Ses seviyesi** (0x – 2x); ses, Genel Ayarlar'daki efekt ses kanalından çalar. Önizleme de sesli.

**1.10.0 yenilikleri — Gerçek kıvrımlı kanatlar:**
- **Yeni kanat: Kutsal Melek** — "Melek Kanatları" öldürme efektindeki kanadın birebir aynısı: oyunun kendi 3D tüyleri, aynı 10 tüylük yelpaze dizilimi (açılar, uzunluklar, boylar), kökten geriye süpürülüp öne-arkaya çırpma, kutsal ışık (karanlıkta da parlak) ve süzülen ışıltılar. Dururken, yürürken, koşarken, uçarken ve eğilirken ayrı animasyon; tüyler kökten uca dalga gibi gecikmeli çırpar.
- **Bütün kanatlar artık düz değil:** kanat yüzeyi gerçek kanat gibi kıvrımlı — dışa doğru geriye kavis yapar (üstten bakınca C), arka/alt kenar kubbelenir, çırparken uçlar öne-arkaya esner. Tüy, kristal, enerji, zar (ejderha) ve kelebek kanatlarının hepsinde.
- **Gerçek kanat duruşu:** dururken ve yürürken kanatlar yarı açık, yukarı kalkık ve geriye süpürülü (V şekli); yürüdükçe öne-arkaya salınır, koşarken geriye yatar, uçarken geniş açılır. Eğilince / yüzerken / uyurken yine sırta katlanır. Büyük boyutta da yere girmez.

**1.9.0 yenilikleri — Vuruş Partikülleri:**
- Karşındakine vurunca ve kritik atınca çıkan partiküller artık seçilebilir; vuruş ve kritik için **ayrı ayrı** stil seçilir. 21 stil: Yeşil Köpük, Kelebekler (kanat çırparak uçup gider), Kalpler, Yıldız Işıltısı, Yıldızlar, Alev, Ruh Ateşi, Kar Taneleri, Elmas Parçaları, Kızıl Damla, Şimşek, Sakura, Yapraklar, Notalar, Gökkuşağı, Ender (sarmal), Altın Paralar (dönerek yere düşer), Kafatası, Şok Dalgası, Neon, Konfeti — ve Vanilla (değişiklik yok).
- **Sadece senin vuruşların:** normal vuruş, sunucunun hasar paketindeki vuran kimliğiyle doğrulanır (ıskalanan, kalkanla engellenen vuruşta çıkmaz); kritik, sunucunun kritik animasyonu son vurduğun hedefe gelince çıkar. Başkalarının vuruşları değişmez.
- Ayarlar: Miktar (0.25x – 2.5x), Boyut (0.5x – 2x), "Vanilla gizle" (özel stil seçiliyken oyunun kendi kritik / keskinlik yıldızlarını gizler), "Moblarda" (kapalıysa sadece oyuncular). Performans sayfasındaki partikül yoğunluğu ve vanilla partikül ayarı da uygulanır.
- Seçim ekranında tek tıkla seç; seçtiğin stil hemen önünde patlar, "Oto önizle" ile sürekli tekrarlar.
- Yeni partikül türü kaydedilmez: kendi dokusuyla kendisi çizer, her sunucuda (vanilla / Paper / Fabric) güvenle çalışır. Ağır partiküller (para, damla, elmas) yerin içine girmez, sekip durur. Aynı anda en fazla 1024 partikül; karede hiç nesne oluşturmaz.
- **İyileştirme:** renk düzenleme efekti bir kez yüklenemezse artık 30 sn sonra yeniden denenir (önceden oturum boyunca kapalı kalıyordu).

**1.8.2 düzeltmesi — kanatlarda bir tarafın siyah / görünmez olması:**
- Sol kanat sağın ayna kopyası olarak üretilir; aynalama yüzey yönünü ters çevirdiği halde ışık hesabı için kullanılan normal düzeltilmiyordu. Bir kanat ışığı tersten alıp kararıyordu. Artık iki kanat birebir simetrik aydınlanır (50 kanatta otomatik ayna testiyle doğrulandı).
- Kanat ışıltısı arka yüzü atan bir çizim türüyle çiziliyordu; bakış açısına göre bir kanadın ışıltısı tamamen kayboluyor, ışıltısı ana renk olan kanatlarda (Plazma, Ruh, Boşluk, Galaksi vb.) o taraf siyah ya da görünmez kalıyordu. Işıltı artık çift yüzlü: iki kanat da her açıdan aynı parlaklıkta görünür.

**1.8.1 düzeltmeleri:**
- **Takılma / yürüyememe düzeltildi:** F5 serbest kamerada hareket kameraya göre çevrilirken sunucuya giden tuş bilgisi (W/A/S/D) eski haliyle kalıyordu; hareket simülasyonu yapan anti-cheat'ler bunu uyuşmazlık sayıp oyuncuyu geri çekiyordu (blok köşesinde ya da düz zeminde takılma). Artık hareket vanilla klavye gibi 8 yönden birine yuvarlanır ve sunucuya giden tuşlar bununla birebir aynıdır; sunucu normal bir oyuncu görür. Karakter yine kameranın gösterdiği yöne döner ve gider.
- **Kanatlar dururken küçülmüyor:** dururken kanatlar katlanmak yerine yürürkenki açıklığında kalır ve yavaşça sallanır; arada bir iyice açılıp silkelenir. Eğilince, yüzerken ve uyurken yine katlanır.

**1.8.0 yenilikleri:**
- **10 yeni vuruş animasyonu (toplam 24):** Şövalye (omuzdan ağır çapraz kesik), Katana (Iaido: yatay hızlı kesik + kan silkeleme), Çift Kesik (tek vuruşta X), Eskrim Hamlesi (geri çekip burgulu ileri hamle), Yer Sarsan (tepeden darbe, çarpınca el sarsılır) ve **sinematik** olanlar — eşya elde döner: Takla (bilekten öne tam tur), Pervane (ekranın ortasında iki tur), Ters Tutuş (ters tutuşa çevirip saplar, geri döndürür), Bıçak Gösterisi (kendi ekseninde iki tur, CS bıçak gösterisi gibi), Ejder Kombosu (kurulur, tam tur savrulur, tepeden bitirir). Hepsi anahtar karelerle tasarlandı: tepe noktalarda doğal yavaşlama, aradaki geçişler kırılmasız; vuruş başlarken ve biterken el hiç sıçramaz (dinlenme pozu vanilla ile aynı). Sinematik stilleri net görmek için Süre 1.5x – 2x önerilir.
- **Kolay stil seçimi:** Vuruş ekranında "Stil ▸" tek ekranda tüm 24 stili açar; tek tıkla seçilir, sağ tarafta elin seçili stille sürekli sallanır (otomatik önizleme), Güç / Süre aynı ekranda. Sinematik stiller ★ ile işaretli.
- **El görünümü (viewmodel):** kol sağ/sol, yukarı/aşağı, ileri/geri (uzat / geri çek); eşya sağ/sol, yukarı/aşağı, ileri/geri; eşya boyutu (0.3x – 2x); eşya döndürme X/Y/Z (±180°); **El FOV** (oyun FOV'unu değiştirmeden elin çizim açısı); sol ele aynalanarak uygulama; **sol eli gizle** (kalkan / totem / meşale; kullanırken görünür); dururken hafif **nefes salınımı**; 7 hazır ayar (Varsayılan, Küçük PvP, Alçak, Uzak, Merkez, Büyük, Sinematik). Varsayılan değerlerde vanilla ile birebir aynıdır.
- **Ekran & Görüntü (yeni menü kartı):**
  - **Renk düzenleme:** doygunluk, parlaklık, kontrast, sıcaklık ve 10 hazır filtre (Canlı, Sinematik, Sıcak, Soğuk, Nostalji, Sepya, Siyah-Beyaz, Gece Görüşü, Neon). Dünya ve el etkilenir, arayüz değişmez. Vanilla'nın kendi renk programını kullanır (ek shader yok); kapalıyken maliyeti sıfır.
  - **Ekran oranı (stretched):** 4:3, 5:4, 3:2, 16:10, 1:1 veya özel oran; görüntü yatayda gerilir, arayüz / nişangah / vuruş algılama değişmez.
  - **Yakınlaştırma (C basılı tut):** yumuşak geçiş, basılıyken fare tekerleğiyle yakınlık (hotbar kaymaz), yakınlaştıkça fare hassasiyeti otomatik düşer. Tuş Kontroller'den değiştirilebilir.
  - **Düşük ateş:** yanarken ekrandaki ateşi aşağı indir ve saydamlaştır.
- **Düzeltmeler / iyileştirmeler:** vuruş ve el görünümü ekranı bölüm başlıklarıyla düzenlendi; dar ekranda stil seçim ızgarası taşmaz.

**1.7.0 yenilikleri:**
- **Bas-bak (varsayılan Sol Alt):** tuşu basılı tuttuğun sürece 3. şahıs kamerayla etrafına / arkana bakarsın; karakter hiç dönmeden gittiği yöne gitmeye devam eder. Bırakınca kamera yumuşakça (0.18 sn) eski yerine kayar ve önceki görünüme (1. şahıs vb.) döner; karakter kameranın baktığı yöne **çevrilmez**. Serbest kamera modundayken de çalışır (bırakınca kamera kaldığı açıdan devam eder). Elytra ile uçarken etrafa bakmak için de kullanılır. Tuş Kontroller'den değiştirilebilir, Serbest Kamera sayfasından kapatılabilir.
- **9 yeni öldürme efekti (toplam 50):** Samuray Kesiği, Buz Mızrakları, Kara Güneş, Ölüm Tırpanı, Kuantum Parçalanma, Yıldız Yağmuru, Toksik Patlama, Dev Dalga, Rün Mührü — hepsi 3D, kendi ses tasarımıyla.
- **10 yeni kanat (toplam 50):** Serafim (altı kanatlı, üç çift), Düşmüş Melek, Tavus Kuşu, Buz Ankası, Ruh Kanadı, Plazma, Mekanik, Kan Ejderi, Kozmik Ejder, Ay Kelebeği.
- **10 yeni aura (toplam 50):** Büyü Çemberi, Gezegen Halkası, Ruh Fırtınası, Yıldırım Kafesi, Kan Ayı, Kar Yıldızı, Volkan, Atom, Su Kalkanı, Bahar Bahçesi.
- **Daha keskin kanatlar:** tüy dokularına ince koyu kontur (üst üste binen tüyler net ayrılır), daha keskin kenar, parlama şeridi ve belirgin sap; boyut aynı kaldı (50 doku toplam ~2.6 MB).
- **Düzeltmeler:** serbest kameradan elytra / binek / ölüm yüzünden çıkınca karakter artık kameranın yönüne zıplamıyor (sadece F5 ile 1. şahısa dönünce görüş kesintisiz kalsın diye döner); bas-bak sırasında serbest kamera duraklar ve kaldığı yerden devam eder.

**1.6.0 yenilikleri — Serbest 3. Şahıs Kamera:**
- **F5 arka görünümde** fare kamerayı döndürür, oyuncuyu değil: dururken kamera etrafında serbestçe döner, karakterini her açıdan görürsün.
- **Hareket kameraya göre:** W kameranın baktığı yöne, A/S/D ona göre; karakter yumuşakça gittiği yöne döner ve o yöne koşar (S'ye basınca dönüp kameraya doğru koşar).
- **Etkileşimde nişan al:** vurunca, blok kırınca, ok atınca, yiyip içince karakter önce kameranın nişan aldığı yere döner, sonra etkileşir; ekranın ortasındaki nişangah tam olarak vurduğun yerdir. Kısa bir süre (ayarlanabilir) o yöne bakmaya devam eder; yay gerirken nişan sürekli kameraya göre güncellenir.
- **Elytra:** istersen uçarken de kamera serbest kalır; karakter yumuşakça kameranın baktığı yöne döner (yumuşak dümen). Kapalıysa uçuşta vanilla kamera.
- **Ön görünümü atla:** F5 döngüsü birinci şahıs ↔ arka görünüm olur.
- Birinci şahısa dönünce karakter kameranın baktığı yöne bakar (geçiş kesintisiz). Binekteyken, uyurken, izleyici modunda devre dışı; kamera duvarların içine girmez (vanilla çarpışma).
- Ana menüde **Serbest Kamera** kartı (sağ tık: aç/kapat) ve **Kontroller → JestronPriv → Serbest 3. Şahıs Kamera Aç/Kapat** tuşu (varsayılan atanmamış).
- Bu özellik JestronPriv için sıfırdan yazıldı; başka bir modun kodu kullanılmadı.

**1.5.0 yenilikleri — Animasyonlu Kanatlar:**
- Eski elytra doku sistemi kaldırıldı; yerine sırtında gerçek 3D, **animasyonlu 40 kanat** geldi. Kanatlar katman katman tüylerden (birincil, ikincil, örtü tüyleri), kemik parmaklar arasına gerili zardan, süzülen kristal parçalarından, akan ışık desenlerinden veya kelebek kanatlarından oluşur. Dokular yüksek çözünürlüklüdür (256x256).
- **Duruma göre animasyon:** dururken sırtında katlı, yavaşça nefes alır ve ara sıra kanatlarını açıp silkeler · yürürken yavaş çırpar · koşarken geriye süpürülüp daha hızlı çırpar · zıplayınca güçlü bir çırpma, düşerken paraşüt gibi açılıp titrer · elytra ile süzülürken tamamen açılır, yükselirken / roketle güçlü çırpar, arada kendiliğinden kanat vurur · yaratıcı uçuşta havada asılı kalma çırpması · eğilince / yüzerken / uyurken katlanır. Geçişler yay fiziğiyle yumuşaktır, kanat asla zıplamaz; uçlar çırpma yönünün tersine esner.
- Işıltı (alev titremesi, şimşek çakması, nabız gibi atan lav), akan desenler (galaksi, aurora, matrix, su) ve kanat uçlarından stile uygun parçacıklar (uçarken artar).
- Elytra giyiliyken elytranın yerine, giyili değilken de kozmetik olarak görünür; kanat görünürken pelerin gizlenir. Sadece görüntüdür, oynanışı değiştirmez.
- **Kanatlar ekranı:** 40 kanat, sağda oyuncunun kendisi kanatlarıyla canlı döner. **Önizleme** butonu ile dururken / yürürken / koşarken / uçarken animasyonu denenir. **Boyut** (0.6x – 1.4x; büyük boyutta katlı tüyler yere girmesin diye kısalır) ve **Parçacık** ayarı.
- 1.4.x'te seçtiğin elytra görünümü aynı isimli kanada otomatik taşınır.
- Performans: kanat tek karede ~0.05 ms'de üretilir, kare içinde hiç nesne oluşturmaz.

**1.4.1 yenilikleri:**
- **Aura Dağılma ayarı (0.5x – 2.5x):** parçacıkların etrafına ne kadar geniş yayılacağı. Düşük = vücuduna sarılı ince aura, yüksek = geniş halka / büyük kanat. Yükseklik değişmez (hale, taç, boynuz yerinde kalır); birinci şahıs yüz filtresi ayardan sonra uygulanır.
- **Aura Hız ayarı (0.25x – 3x):** dönme, sarmal, akış ve atış hızı. 0.25x ağır çekim, 3x çok hızlı; kalp atışı ve sonik dalga da bu hızda atar. Nota renkleri ve büyü rünleri bozulmaz.
- **Düzeltme:** Sonik Dalga ve Kalp Atışı halkaları artık gerçekten dışa yayılıyor, Matrix kodu aşağı akıyor, Kan İzi damlıyor. Vanilla toz parçacığı verilen hızı neredeyse yok saydığı için bu desenler önceden yerinde duruyordu; hız artık birebir uygulanır.
- Aura ekranındaki ayarlar kaydırıcıdır; sürükledikçe aura arkada canlı değişir. Dar ekranda alt butonlar iki satıra geçer (Elytra ekranında da).

**1.4.0 yenilikleri:**
- **Özel Yazı efekti:** öldürünce kurbanın üstünde kendi yazdığın yazı çıkar. Yazı tipi (Normal, Unicode, Büyü Dili, İllager), renk modu (tek renk / gradyan / hareketli gökkuşağı), 16 hazır renk + özel #RRGGBB, arka plan kutusu (renk + opaklık), kalın / italik / altı çizili / üstü çizili, gölge, kontur (renkli), karanlıkta parlama, boyut, yükseklik, 6 animasyon (Pop, Yükselerek, Daktilo, Nabız, Titreyerek, Zoom), 6 ses ve renkli parçacık patlaması. `{kurban}` ve `{ben}` yer tutucuları, `&` renk kodları, `|` ile alt satır. Düzenleyicide canlı önizleme.
- **Elytra Görünümü:** 40 yüksek çözünürlüklü (vanilla'nın 4 katı) kanat görünümü: Melek, Su, Ateş, Buz, Şeytan, Ejderha, Anka, Galaksi, Kuzey Işıkları, Gökkuşağı, Altın, Elmas, Zümrüt, Lav, Boşluk, Mavi Kelebek, Monark Kelebek, Sakura, Neon, Matrix, Kamuflaj, Zebra, Leopar, Bal Peteği, Vitray, Sonbahar, Ender, Nether, Balçık, Ametist, Kızıl Gül, Yıldızlı Gece, Gün Batımı, Okyanus, Toksik, Şimşek, Kar Tanesi, Kraliyet, Kalpler, Kutup Tilkisi. İstersen elytra giymeden de kozmetik kanat olarak görünür. (1.5.0'da animasyonlu 3D kanatlara dönüştü.)
- **Aura:** 40 parçacık aurası; dururken ve yürürken farklı desenler (alev halkası, melek halesi, DNA sarmalı, ışık kanatları, kelebekler, matrix, kara delik, sonik dalga, kraliyet tacı ...). Yoğunluk ayarı; birinci şahısta görüşünü kapatan parçacıklar otomatik gizlenir.
- Ana menüye **Özel Yazı**, **Elytra Görünümü** (1.5.0'da **Kanatlar**) ve **Aura** kartları eklendi; kartlar küçük ekranlara göre otomatik küçülür.
- Efekt sonu güvenlik ağı iyileştirildi (zaten kaybolmakta olan parçalara dokunmaz).

**1.3.0** — 40 öldürme efekti + 14 kılıç vuruş stili + kesin öldürme doğrulaması.

**1.3.0 yenilikleri:**
- **Sahte efekt düzeltmesi (sıkı doğrulama):** efekt sadece öldürme kesinse çıkar. Son 5 sn içinde son vuran sen olmalısın (sunucunun öldürme kredisi kuralı); senden sonra yakından başkası vurduysa efekt çıkmaz; ışınlanma veya kimin öldürdüğünü söylemeyen tek bir mesaj tek başına yetmez; sahte can paketleri (1 tick 0 can) yok sayılır; killstreak / bounty / combat mesajları öldürme sayılmaz.
- **15 yeni 3D efekt:** Yıldırım Çekici, Kan Gülü, End Portalı, Ruh Zincirleri, Demir Yumruk, Altın Piramit, TNT Zinciri, Galaksi, Yarasa Sürüsü, Volkan, Kum Saati, Yörünge Lazeri, Dev Kar Tanesi, Anka Kuşu, Lanetli Sarmaşık.
- **6 yeni kılıç vuruş stili:** Çapraz Kesik, Ters Savurma, Ağır Darbe, Saplama, Çevirme, 1.7 Blok-Vuruş. "Klasik" stil yüksek güçte artık sıçramıyor.
- **Daha akıcı efekt sonu:** tüm efektlerde son parçalar 48. tick'e kadar yumuşakça küçülür; hiçbir parça "pat" diye kaybolmaz.
- Efekt menüsü fare tekerleğiyle kaydırılır (küçük ekranlarda 40 efekt sığar).

**1.2.2 düzeltmesi:** Öldürme algılama baştan yazıldı; can gizleyen, ölüm mesajını değiştiren ve anında respawn eden (BoxPvP / practice) sunucularda da çalışır.

**1.2.1 düzeltmesi:** Efekt sesleri artık varsayılan olarak **Ana Ses** kanalından çalar (önceden "Oyuncular" kanalındaydı; o kaydırıcı %0 iken hiç ses gelmiyordu). Yeni **Gerçekçi ses** katmanı ve **Ses testi** eklendi.

- **Öldürme Efektleri:** 2.5 saniye süren 40 detaylı 3D öldürme efekti (konum / ekran / ikisi).
- **Vuruş Animasyonu:** ana eldeki eşya için 24 vuruş stili (Savurma, Aşağı Darbe, Yumuşak, Yumuşak 2, Güç, Ziyafet, Burgu, Klasik, Çapraz Kesik, Ters Savurma, Ağır Darbe, Saplama, Çevirme, 1.7 Blok-Vuruş, Şövalye, Katana, Çift Kesik, Eskrim Hamlesi, Yer Sarsan, Takla, Pervane, Ters Tutuş, Bıçak Gösterisi, Ejder Kombosu). Güç, süre, "sadece vururken", eşya iniş animasyonu ve tam el görünümü (viewmodel) ayarlanabilir.
- **Performans:** parçacık yoğunluğu, vanilla parçacık ayarına uyma, çizim mesafesi, görünmeyeni çizmeme, aynı anda efekt sınırı.

**Sadece istemci modu:** her şeyi sen görürsün; sunucuda hiçbir şey kurulu olmasına gerek yok, her sunucuda çalışır.
Tüm animasyonlar sadece görüntüdür; saldırı hızını, hasarı veya oynanışı değiştirmez.

## Gereksinimler
- Minecraft **1.21.4**
- Fabric Loader **0.16.9+**
- **Fabric API** (zorunlu bağımlılık)
- Java 21

## Kurulum
1. `JestronPriv-1.17.0+1.21.4.jar` ve Fabric API jar'ını `.minecraft/mods` klasörüne at.
2. Oyunu Fabric profiliyle aç.
3. **K** tuşu ile ana menüyü aç ya da `/ke` yaz.

Ayrı SwingAnimation modunu bu modla **birlikte kurma**; aynı el animasyonunu iki mod birden değiştirmeye çalışır.

## Ana menü (K)
| Kart | İçerik |
|---|---|
| Öldürme Efektleri | 50 efekt; sol tık seç, sağ tık önünde önizle, tekerlekle kaydır; "✎ Özel Yazı" butonu |
| Özel Yazı | Yazı, yazı tipi, renkler, arka plan, kontur, stil, boyut, yükseklik, animasyon, ses; canlı önizleme |
| Kanatlar | 51 animasyonlu kanat (gerçek kıvrımlı), canlı 3D önizleme, önizleme modu, boyut, parçacık |
| Aura | 50 aura, yoğunluk, dağılma, hız, 1. şahısta göster/gizle |
| Serbest Kamera | Bas-bak (Alt), aç/kapat, etkileşimde nişan al, nişangah, nişan süresi, dönüş hızı, elytrada serbest, ön görünümü atla |
| Vuruş Animasyonu | 34 stil (9 sinematik), güç, süre, sadece vururken, eşya değiştirme inişi, saldırı bekleme dalışı, önizle |
| Vuruş Partikülleri | 41 vuruş + kritik stili (ayrı ayrı), miktar, boyut, vanilla gizle, moblarda, ses ve ses seviyesi; anında önizleme |
| Ekran & Görüntü | Renk düzenleme ve 10 filtre, ekran oranı (stretched), yakınlaştırma, düşük ateş |
| HUD | İksir paneli, eşya sayacı (13 eşya), bilgi satırı (9 bilgi); sürükle-bırak düzenleyici |
| Envanter & Eşyalar | Envanter HUD, alınan / atılan bildirimi, yerdeki eşya etiketleri ve hitbox |
| Evcil Yoldaş | 8 yoldaş, yer, taraf, boyut, takip, süzülme, renk / gökkuşağı, parlaklık, parçacık, tepki |
| Seçim Kutusu & FOV | Çizgi rengi / kalınlığı / gökkuşağı / nabız, dolgu, kırma ilerlemesi; FOV etkileri ve geçiş |
| PvP Hissi | Hasar sayıları (biçim, boyut, renk), kombo sayacı (konum, süre, ses), seri öldürme duyurusu (dil, ses, parlama, süre), vurulma rengi (renk, gökkuşağı, yoğunluk, herkese); her bölümde "dene" |
| Nişangah | 9 şekil, uzunluk, kalınlık, boşluk, opaklık, renk / gökkuşağı, kontur, orta nokta, dinamik açılma, isabet işareti; canlı önizleme |
| Totem & Zaman | Totem efekti, boyut, ses, sayaç, sohbete yaz, vanilla partikül / ekran totemini gizle; **özel totem sesi** (dosya, klasörü aç, sürükle-bırak, ses seviyesi, başlangıç, süre, hız, giriş / çıkış, kimde, vanilla sustur, üst üste); saat modu, hazır saat, saat, döngü hızı; hava |
| Genel Ayarlar | Gösterim modu, ekran başlığı ve mesafesi, efekt sesi, ses kanalı, gerçekçi ses, ses testi, sıkı doğrulama, algılama bildirimi, mob öldürünce, seçili efekti önizle |
| Performans | Parçacık yoğunluğu, vanilla ayarına uy, çizim mesafesi, görünmeyeni çizme, aynı anda efekt, tümünü durdur, varlık ayıklama (varlık / blok varlığı / oyuncu / isimli, hassasiyet) |
| Tuş Ayarları | Kontroller ekranını açar; menü tuşunu (K) buradan değiştirebilirsin |

- Kartlarda **sol tık** açar, **sağ tık** özelliği hızlıca açıp kapatır.
- Her ayar sayfasında **Varsayılana Sıfırla** ve **Geri** var; **Esc** bir önceki sayfaya döner.
- Ayar sayfalarında arka plan bulanıklaşmaz: elin ve oyun arkada görünür, değişiklikleri anında görürsün.
- Menü tuşu ayrıca **Ayarlar → Kontroller → Tuş Atamaları → JestronPriv** altından da değiştirilebilir.

## Kozmetikler hakkında
- Kanatlar ve aura **sadece istemci tarafıdır**: sen görürsün (F5 ile kendine bakabilirsin), diğer oyuncular görmez. Sunucuda hiçbir şey gerekmez.
- Kanat seçiliyken elytra giyiliyse elytranın yerine kanat çizilir (uçuş aynen elytrayla olur); elytra giymeden de kanat sırtında görünür ama sadece görüntüdür, uçamazsın. Kanat görünürken pelerin gizlenir.

## Sesler
- **Ses kanalı:** Ana Ses (önerilen, her zaman duyulur) / Oyuncular / Ortam. Oyuncular veya Ortam seçiliyse o kaydırıcı %0 iken efekt sesi duyulmaz.
- **Gerçekçi ses:** patlama ve ağır çarpmalara düşen enkaz, taş kırılması, çınlama ve cızırtı katmanları eklenir; her ses doğal perde farkıyla çalar; 20 bloktan uzak öldürmelerde ses mesafeye göre biraz gecikir. Aynı anda aynı sesten en fazla 3 tane çalar (ok yağmurunda ses patlaması olmaz).
- **Ses testi:** Genel Ayarlar'daki butona bas; test sesi çalar ve Ana Ses / kanal / efekt sesi seviyelerini sohbete yazar. %0 olan varsa kırmızı gösterir.

## Gösterim modları
| Mod | Açıklama |
|---|---|
| Konum | Efekt, öldürdüğün oyuncunun öldüğü noktada oynar |
| Ekran | Efekt, ekranının tam önünde oynar + ekranda başlık |
| İkisi | İkisi birden |

## Öldürme nasıl algılanır? (sıkı doğrulama)
**Güçlü sinyaller** (tek başına yeterli, hepsinde "son 5 sn içinde son vuran sen" kuralı geçerli):
1. **Ölüm olayı:** sunucu, can gizlense ve ölüm mesajı kapatılsa bile ölen her oyuncu için çevredekilere gönderir.
2. **Ölüm pozu / can 0:** vurduğun oyuncu ölme animasyonuna geçer veya canı en az 2 tick 0 kalır (sahte can paketleri elenir).
3. **Vanilla ölüm mesajı:** öldüren olarak senin adın geçer.
4. **Seni öldüren olarak gösteren sunucu mesajı:** "Jestron killed X", "You killed X", "X was slain by Jestron", "X adlı oyuncuyu öldürdün", "X, Jestron tarafından öldürüldü".

**Zayıf sinyaller** (sıkı doğrulamada tek başına yetmez, 1.5 sn içinde farklı ikinci bir zayıf sinyal gerekir):
- Vurduğun oyuncunun vuruştan hemen sonra dünyadan silinmesi (ışınlanma / çıkış da olabilir).
- Kimin öldürdüğünü söylemeyen mesaj ("+1 Kill | X", "[Kill] X").

**Hiçbir zaman öldürme sayılmaz:** senden sonra yakından başkasının vurduğu oyuncu, 5 sn'den eski vuruşlar, "X seni öldürdü", "Y killed X", killstreak / bounty / combat / sıralama mesajları.

Efekt çıkmıyorsa veya neden çıktığını görmek istersen **Genel Ayarlar → Algılama bildirimi**'ni aç. **Sıkı doğrulama** kapatılırsa zayıf sinyaller de tek başına efekt çıkarır.

## Komutlar (istemci komutları)
`/ke` (ana menü) · `/ke sec <efekt>` · `/ke onizle <efekt>` · `/ke kapat` · `/ke rastgele` · `/ke mod <konum|ekran|ikisi>` · `/ke liste`
(`/jestronpriv` da aynı)

## Ayar dosyası
`.minecraft/config/jestronpriv.json` — 1.1.0'dan gelen dosya korunur, yeni ayarlar varsayılan değerleriyle eklenir.

## Kaynaktan derleme
```
gradlew build
```
Çıktı: `build/libs/JestronPriv-1.14.2+1.21.4.jar`

## Teşekkür / lisans notu
Vuruş animasyonu stillerinin matematiği **SwingAnimation** (anomalith, CC0-1.0) modundan uyarlanmıştır.
Emoji görselleri **Twemoji** (CC-BY 4.0, jdecked/twemoji), emoji adları **Unicode CLDR** (Unicode License v3), isim etiketi yazı tipleri Google Fonts (SIL OFL 1.1 / Apache 2.0); ayrıntılar `THIRD_PARTY_NOTICES.txt` içinde.
Özel totem sesi için ses çözme kodu **JCodec** (BSD 2-Clause) ve **JAAD** (kamu malı) projelerinden kaynak olarak eklenmiştir; lisans metinleri `THIRD_PARTY_NOTICES.txt` içindedir.
