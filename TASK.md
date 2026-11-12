# Görev: deprem-takip-android — Jetpack Compose + Clean Architecture Android uygulaması

Portföy projesi: işverene modern Android yetkinliğini (Compose, MVVM, Clean Architecture, Hilt, Room, Retrofit, Coroutines/Flow, WorkManager, test) gösterecek, DERLENEN ve TESTLERİ GEÇEN bir uygulama.

## Ortam
- ANDROID_HOME=/opt/homebrew/share/android-commandlinetools (platforms;android-35, build-tools;35.0.0 kurulu). `local.properties` içine `sdk.dir=/opt/homebrew/share/android-commandlinetools` yaz (gitignore'da olsun).
- Sistem gradle 9.9.1 var; projeye wrapper üret: `gradle wrapper --gradle-version 8.11.1` ve sonra hep `./gradlew` kullan.
- JDK 17. AGP 8.7.x, Kotlin 2.0.x (Compose compiler plugin `org.jetbrains.kotlin.plugin.compose`), KSP (Room ve Hilt için), version catalog (gradle/libs.versions.toml).
- compileSdk 35, minSdk 26, targetSdk 35. Tek modül `app` yeterli ama paketler katmanlı olsun.
- `rm -rf`, `sudo`, `git push` kullanma. Sadece bu klasörde çalış. Emülatör yok; doğrulama `./gradlew testDebugUnitTest assembleDebug lintDebug` ile.

## Uygulama
AFAD açık deprem verisi: `GET https://deprem.afad.gov.tr/apiv2/event/filter?start=YYYY-MM-DDTHH:mm:ss&end=...&minmag=2&orderby=timedesc&limit=200` (JSON dizi; alanlar: eventID, location, latitude, longitude, depth, type, magnitude, province, district, date, isEventUpdate...). API anahtarı yok. Alan adlarını tolerant parse et (eksik alan -> null).

Özellikler:
1. Son depremler listesi (son 24 saat / 7 gün seçimi), büyüklüğe göre renkli rozet, göreli zaman ("12 dk önce"), il/ilçe.
2. Filtre: minimum büyüklük (slider), il araması (metin), sıralama (zaman/büyüklük).
3. Detay ekranı: tüm alanlar, derinlik, koordinat, "Haritada aç" (geo: intent, harita SDK'sı YOK).
4. Çevrimdışı: Room önbellek, ağ yoksa önbellekten göster + "çevrimdışı" banner'ı; pull-to-refresh.
5. Arka plan: WorkManager periyodik (15 dk) kontrol; kullanıcının seçtiği ilde, eşik büyüklüğün üstündeki YENİ deprem için bildirim (Android 13+ POST_NOTIFICATIONS izni isteği). Ayarlar DataStore'da.
6. Ayarlar ekranı: il seçimi, bildirim eşiği, bildirim aç/kapa.
7. Material 3, açık/koyu tema, Türkçe string resource'ları.

## Mimari
- `data/remote` (Retrofit + kotlinx.serialization veya Moshi, DTO), `data/local` (Room entity/dao), `data/repository` (offline-first: önce cache Flow, sonra network ile refresh, hata -> Result/sealed tip)
- `domain/model`, `domain/usecase` (GetEarthquakesUseCase, FilterEarthquakesUseCase, CheckNewQuakesForAlertUseCase), `domain/repository` arayüzü
- `ui/list`, `ui/detail`, `ui/settings` — ViewModel + immutable UiState (StateFlow), Compose ekranları, Navigation Compose
- `di/` Hilt modülleri, `worker/` HiltWorker
- Saat/zaman bağımlılığı için `Clock` soyutlaması (testlenebilir).

## Testler (src/test, JVM)
En az 20 anlamlı unit test: DTO->domain mapping (eksik alanlar), filtre/sıralama use case'leri, alert use case (yeni vs görülmüş deprem, eşik, il eşleşmesi Türkçe büyük/küçük harf: "İzmir" vs "izmir"), repository (sahte API + sahte DAO ile offline fallback), ViewModel (kotlinx-coroutines-test, Turbine opsiyonel) state geçişleri: loading->success, error->cache'li gösterim. MockWebServer ile Retrofit parse testi (gerçek ağ yok).

## README.md (İngilizce + kısa Türkçe özet)
Özellikler, mimari diyagram (ASCII katmanlar), teknoloji listesi, tasarım kararları (offline-first neden, WorkManager kısıtları, harita SDK'sı neden yok), derleme/test komutları, gerçek test sayısı, ekran görüntüsü yok notu, veri kaynağı ve atıf (AFAD), sınırlamalar.

## Bitirme kriteri
`./gradlew testDebugUnitTest assembleDebug` BAŞARILI (lintDebug uyarı verebilir ama hata vermesin). Sonra `git add -A && git commit` (push YOK). build/ ve .gradle/ gitignore'da.
