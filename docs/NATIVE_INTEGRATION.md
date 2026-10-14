# Native Island: Schnittstellen und Grenzen

Recherche: 14. Oktober 2026. Ausschließlich systemeigene Darstellung; niemals Overlay.

## Xiaomi

Der offizielle Client-Weg verwendet Android-Benachrichtigungen mit `miui.focus.param`. Die JSON-Daten enthalten `param_v2`, `param_island`, `bigIslandArea`, `smallIslandArea` und Template-Inhalte. Bilder liegen als `Icon` im Bundle `miui.focus.pics`. SystemUI übernimmt die Darstellung.

Nafba prüft `notification_focus_protocol` (mindestens 3) und den lokalen Provider `content://miui.statusbar.notification.public` mit `canShowFocus` für den eigenen Paketnamen. Die dokumentierte langsame Abfrage läuft außerhalb des UI-Threads. Fehlgeschlagene Abfragen werden nicht als Unterstützung ausgegeben.

Xiaomis Prozess verlangt App-Registrierung, Signaturfingerabdruck, Szenario-/Designprüfung und Freigabe. Die chinesische Entwicklerplattform weist auf die Trennung zur internationalen Plattform hin. Eine globale Freigabe darf daraus nicht abgeleitet werden. Modell, HyperOS-Build und Region müssen für den Hardware-Test bekannt sein.

Die Beta enthält keine Xiaomi-App-ID. Eine genehmigte ID kann beim Build über `-PxiaomiAppId=...` gesetzt werden. Die Release-/Debug-Signaturen müssen passend registriert sein; das Manifest kennzeichnet Debug-Builds entsprechend. Die ID allein beweist keine Freigabe.

## Android

Der öffentliche Promotion-Request wird erst ab API **36.1** aktiviert (`SDK_INT_FULL >= 3600001`). Dazu wird die dokumentierte Extra-Property `android.requestPromotedOngoing` verwendet. Keine versteckten Methoden werden reflektiert.

Ein vom Nutzer gestarteter kurzer Timer hat eine eindeutige Laufzeit und einen Standardstil mit Countdown. Sein Vordergrunddienst endet nach maximal zwei Minuten. Die Promotion-Erlaubnis garantiert nicht die Darstellung auf einem konkreten Gerät.

Akku-Alarme werden nicht als Android Live Updates ausgegeben, da die offiziellen Kriterien allgemeine Alarme und ambienten Status ausschließen. Akkuwarnungen bleiben an Xiaomis Freigabe gebunden.

## Vorlagen

Musik, Anruf, Navigation, Fokus, Aufnahme und Laden sind interne Design-Vorschauen. Nafba übernimmt keine fremden App-Ereignisse, keine Apple-Dienste und keine beliebige Änderung der SystemUI. Native Templates lassen sich mit Daten befüllen; ihre Darstellung bestimmt das System.

## Primärquellen

- [Xiaomi: Client, Parameter und Abfragen](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2131)
- [Xiaomi: App-/Signatur-/Szenariofreigabe](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2132)
- [Xiaomi: Versionsbereich](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2141)
- [Xiaomi: globale Verfügbarkeit](https://www.mi.com/de/hyperos/)
- [Android Live Updates](https://developer.android.com/develop/ui/views/notifications/live-update)
- [Öffentliche Promotion-Property ab 36.1](https://developer.android.com/reference/android/app/Notification#EXTRA_REQUEST_PROMOTED_ONGOING)
- [Android Full SDK Version Codes](https://developer.android.com/reference/android/os/Build.VERSION_CODES_FULL)

Die vollständige Xiaomi-Template-PDF bleibt beim offiziellen Anbieter und wird nicht im Repo erneut verteilt.
