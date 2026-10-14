# Releases

## Ersteinrichtung

Das Projekt wird auf `main` entwickelt. Repo-Beschreibung:

> Native Android Dynamic Island mit iOS-inspirierter Glas-Optik, Akkuwarnungen und anpassbarer Position für POCO/Redmi.

Topics: `android`, `poco`, `redmi`, `dynamic-island`, `battery`, `java`, `glass-ui`.

Die V1-APK verwendet einen privaten Release-Schlüssel. Der separat bereitgestellte Signierschlüssel-Backup gehört ausschließlich dem Projektinhaber und darf niemals ins Repo oder in öffentliche Release-Downloads gelangen. Zum Weiterbauen denselben Schlüssel verwenden.

## GitHub Actions Secrets

- `NAFBA_KEYSTORE_BASE64`: Base64-Inhalt von `nafba.jks`.
- `NAFBA_STORE_PASSWORD`: Kennwort aus dem privaten Backup.
- `NAFBA_KEY_PASSWORD`: dasselbe Kennwort.
- Alias: `nafba`.

Nach Einrichten der Secrets die Repository-Variable `NAFBA_RELEASE_SIGNING_READY` auf `true` setzen. Bis dahin laufen nur Tests und Debug-Builds; der automatische signierte Release-Job wird übersprungen. Die erste geprüfte V1-APK kann manuell als Release-Asset veröffentlicht werden.

Normale Builds und Pull Requests brauchen keine Secrets. Nur der Release-Job auf Versionstags erhält sie. Fehlen sie, bricht er ab, statt eine anders signierte APK zu veröffentlichen.

## Release erstellen

1. `versionCode` erhöhen und `versionName` in `app/build.gradle` aktualisieren.
2. Changelog und `docs/RELEASE_NOTES.md` aktualisieren. Gerätetests dokumentieren.
3. Tests, Lint und Build prüfen; Änderungen auf `main` committen und pushen.
4. `git tag -a v1.0.0 -m 'Nafba V1.0.0'` und `git push origin v1.0.0`.
5. Actions prüft den Code, baut die signierte APK und erstellt eine Vorabversion mit APK und SHA-256-Prüfsummen.
6. Erst nach echten Gerätetests den Pre-release-Status aufheben.

GitHub Actions muss im Repository erlaubt sein. Tags und Releases nicht nachträglich auf anderen Code umbiegen. Stattdessen eine neue Versionsnummer verwenden.

## Lokal signieren

```sh
export NAFBA_KEYSTORE=/absoluter/pfad/nafba.jks
read -rs NAFBA_STORE_PASSWORD
export NAFBA_STORE_PASSWORD
export NAFBA_KEY_PASSWORD="$NAFBA_STORE_PASSWORD"
./gradlew testDebugUnitTest lintDebug assembleRelease
```

## Erstes Repo per GitHub CLI veröffentlichen

Falls die verwendete Integration keine Repo-Erstellung unterstützt, kann der Eigentümer mit installiertem und angemeldetem `gh` das vorbereitete Projekt veröffentlichen:

```sh
gh auth login
bash scripts/publish-v1.sh /pfad/zum/Nafba-v1.0.0.apk /pfad/zum/privaten/backup
```

Das Skript erstellt ein **öffentliches** Repo unter dem angemeldeten Account `Fufi1925`, setzt Beschreibung und Topics, lädt Code und Signatur-Secrets hoch, pusht `v1.0.0` und erstellt eine Vorabversion mit der bereitgestellten APK. Die öffentliche Sichtbarkeit ist eine bewusste Vorgabe des Skripts; alternativ `--public` im Skript vor der ersten Ausführung auf `--private` ändern. Private Dateien bleiben außerhalb von Git.
