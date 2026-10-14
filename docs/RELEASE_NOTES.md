# Nafba V1.0.0 — erste Vorabversion

Eine konfigurierbare Android-Island mit gezeichneter Glas-Optik, sanften Übergängen und Akku-Momenten.

## Enthalten

- Low Battery Bar bei weniger als 20 %; Grenze konfigurierbar.
- Akkustand, Ladeanzeige, Vorschau und abschaltbare Animationen.
- Breite und Position passend zur Frontkamera einstellen.
- Lokale Verarbeitung, kein Konto, keine Werbung und keine Internetberechtigung.

## Installation

Die `.apk` herunterladen und installieren. In Nafba die Overlay-Erlaubnis erteilen und anschließend die Island aktivieren. Android 8.0 oder neuer ist erforderlich.

## Status

V1 ist eine Vorabversion. Automatische Akku-Tests, Android Lint, APK-Build und Signaturprüfung werden vor Bereitstellung geprüft; tatsächliche Resultate stehen in `docs/VALIDATION.md`. Kein physischer POCO-/Redmi-Test wurde durchgeführt. Allgemeine Android-Unterstützung ist keine Garantie für ein bestimmtes unbekanntes Gerätemodell.

## Bekannte Grenzen

Die App ersetzt nicht die gesamte Systemoberfläche. Glas ist gezeichnet und verwendet kein Live-Blur anderer Apps. Das Overlay kann am oberen Rand Bedienelemente verdecken. Geschützte Apps oder Systemfenster können es ausblenden. HyperOS/MIUI kann den Dienst beenden. Nach Neustart ist erneutes manuelles Aktivieren nötig.
