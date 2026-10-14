# V2 Native Edition · Validierung

Stand: 14. Oktober 2026. Automatisch im Build-Container geprüft; kein physischer POCO-/Redmi-Test.

| Prüfung | Ergebnis |
| --- | --- |
| Sauberer signierter Release-Build | bestanden |
| Akku-Grenzwerte | 4 Tests bestanden |
| Native Freigabe-Logik, inklusive API 36.0/36.1 | 3 Tests bestanden |
| Vier Tabs, Wiederherstellung, Berechtigungen und View-Render | 3 Robolectric-Tests bestanden |
| Android Lint | 0 Fehler, 11 Warnungen |
| APK-Signatur | gültig, identischer V1-Release-Schlüssel |
| APK-Overlay-Berechtigung | nicht enthalten |
| Native Xiaomi-Gerätedarstellung | ausstehend; keine App-ID/Freigabe enthalten |
| Native Live-Update-Promotion auf Hardware | ausstehend |

Die Screenshots in `docs/screenshots` zeigen tatsächliche Android-Views, gerendert mit Robolectric/Android 15 bei 393 × 852 dp. Sie sind keine Gerätefotos und bestätigen weder HyperIsland noch die Hardware-Blur-Leistung. Der Renderer bestätigt Layout und Interaktion. Timer, Akkuwarnung und Systemdarstellung benötigen zusätzlich die Hardware-Checkliste.

Build: `gradle clean testDebugUnitTest lintDebug assembleRelease` mit Java 17, Gradle 8.13 und compile/target SDK 36.

APK: `Nafba-v2.0.0-beta.1.apk`.

SHA-256:
`bd426396551b08db6678dc86b01288d5c9fc413ddc14213157179231d3e2f692`

Signierzertifikat SHA-256:
`a36c504862bd5b09125edf60bec0d18dfef20a6383664eefc6f2eea524846350`

Die vorhandenen Lint-Warnungen sind vorwiegend Versions-, Lokalisierungs-, Icon-, Zeichen- und Backup-Hinweise. Sie sind keine bestandenen Hardware-/Freigabetests.
