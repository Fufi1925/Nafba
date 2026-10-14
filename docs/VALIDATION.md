# V1-Validierung

Stand: 14. Oktober 2026. Geprüft im Build-Container, ohne Emulator oder physisches Telefon.

| Prüfung | Ergebnis |
| --- | --- |
| Sauberer Android-Release-Build | bestanden |
| BatteryState Unit-Tests | 4 bestanden, 0 Fehler |
| Android Lint Debug | 0 Fehler, 4 Warnungen |
| Release Lint Vital | bestanden |
| APK-Signatur mit Android apksigner | gültig, APK Signature Scheme v2 |
| Paket-ID / Version | `de.fufi.nafba` / `1.0.0` (Code 1) |
| Mindest- / Ziel-API | 26 / 35 |
| XML und Veröffentlichungsskript | Syntax geprüft |
| POCO-/Redmi-Gerätetest | ausstehend |
| GitHub-Repo und V1-Release | veröffentlicht: `Fufi1925/Nafba`, Tag `v1.0.0`, signierte APK und SHA-256-Prüfsumme |

Die laufenden GitHub-Prüfungen und ihre Ergebnisse sind unter [Actions](https://github.com/Fufi1925/Nafba/actions) sichtbar. Signierte automatische Releases benötigen die in `RELEASING.md` beschriebenen privaten Secrets; bis zur Ersteinrichtung ist dieser Job deaktiviert.

Build-Aufruf: `gradle clean testDebugUnitTest lintDebug assembleRelease` mit Gradle 8.13 und Java 17. Die Gradle-Warnung über die kommende Version 9 betrifft nicht diesen gepinnten Build.

APK SHA-256:
`e37b20ffa9feb0d62edcf20b15b01921f397e152d179b9d0fb93d9637d6efde8`

Öffentlicher Signierzertifikat-Fingerabdruck SHA-256:
`a36c504862bd5b09125edf60bec0d18dfef20a6383664eefc6f2eea524846350`

Die automatischen Prüfungen bestätigen den Build und die Akku-Logik. Sie beweisen weder die visuelle Qualität auf einem bestimmten Display noch die Laufzeit-Kompatibilität mit einem unbekannten POCO-/Redmi-Modell. Dafür die Checkliste in `DEVICE_TESTS.md` verwenden.
