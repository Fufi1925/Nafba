# Mitmachen

Änderungen werden gegen `main` eingereicht. Vor großen Änderungen oder Tests an bestehendem Code zuerst einen Backup-Branch vom bisherigen Stand anlegen.

Vor einem Pull Request `./gradlew testDebugUnitTest lintDebug assembleDebug` ausführen. Für Änderungen am Overlay zusätzlich einen echten Gerätetest dokumentieren. Modell, Android-Version, HyperOS/MIUI-Version und reproduzierbare Schritte nennen.

Keine Signierschlüssel, Kennwörter oder lokale SDK-Pfade committen. Neue Funktionen sollen lokal arbeiten und nur unbedingt notwendige Berechtigungen benötigen.
