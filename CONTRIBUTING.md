# Mitmachen

Änderungen werden gegen `main` eingereicht. Vor großen Änderungen oder Tests an bestehendem Code zuerst einen Backup-Branch vom bisherigen Stand anlegen.

Vor einem Pull Request `./gradlew clean testDebugUnitTest lintDebug assembleDebug` ausführen. Änderungen an der nativen Anbindung zusätzlich auf einem echten Gerät testen. Modell, Android-Version, HyperOS-Version, Region und reproduzierbare Schritte nennen. Keine Overlay-Ersatzlösung hinzufügen.

Keine Signierschlüssel, Kennwörter oder lokale SDK-Pfade committen. Neue Funktionen sollen lokal arbeiten und nur unbedingt notwendige Berechtigungen benötigen.
