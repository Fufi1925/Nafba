# Nafba 2.0.0-beta.1 · Native Edition

Die überarbeitete App mit vier schwebenden Glas-Tabs und schwarzen Island-Vorlagen.

- Übersicht, Island, Aktivitäten und Einstellungen.
- Helles Apple-inspiriertes Design, rosa Akzente, eigene Glas-/Blur-Flächen und sanfte Übergänge.
- Der frühere Overlay-Code und die Overlay-Berechtigung sind vollständig entfernt.
- Dokumentierte Xiaomi-Systembenachrichtigungen mit Freigabeprüfung; kein gezeichneter Ersatz.
- Kurze native Timer über Xiaomi oder öffentliche Android Live Updates ab API 36.1, wenn erlaubt.
- Akkuwarnungen nur nach bestätigter Xiaomi-Anbindung.

## Grenzen dieser Beta

Die Beta enthält noch keine Xiaomi-App-ID und keine Xiaomi-App-/Signatur-/Szenariofreigabe. Ohne diese ist die native Akkuwarnung deaktiviert. Ein vorhandener Kameraausschnitt oder eine vorhandene HyperIsland bestätigt keine Freigabe für Nafba.

Musik, fremde Anrufe, Navigation, Fokus, Aufnahme und Laden sind interne Design-Vorschauen. Deren tatsächliche System-/Drittanbieter-Ereignisse werden nicht übernommen. Die System-Island kann nicht beliebig in ein Apple-Layout umgestaltet werden.

Kein physischer POCO-/Redmi-Test wurde durchgeführt. Die konkrete Modell-/HyperOS-/Regionskombination ist noch unbekannt.

## Installation

Die V2-APK herunterladen und installieren. Derselbe Signierschlüssel ermöglicht ein Update über V1. In Einstellungen die native Verbindung prüfen. Es wird keine Overlay-Erlaubnis benötigt. V1 entspricht nicht dem korrigierten Native-only-Ziel.

Prüfresultate und Screenshots der tatsächlichen Android-Views stehen in `docs/VALIDATION.md` und `docs/screenshots`.
