# FM Kids Player

Offline-Kinder-Mediaplayer für Android, vorbereitet für das Samsung Galaxy S20 FE.

## Bereits enthalten
- Große Serien-Kacheln
- Eigenes Cover pro Serie
- Elternbereich per langem Druck auf die Überschrift
- Standard-PIN: `2580`
- Android-Dateibrowser zur Auswahl eines Video-Ordners
- Cover-Auswahl über Android-Dateiauswahl
- Lokale MP4/MKV/WebM/M4V-Dateien
- Automatisches Abspielen Folge für Folge
- Fortschritt wird lokal gespeichert
- Nach Ende einer Serie geht es mit einer anderen Serie weiter
- Wenn alle Serien durch sind, beginnt der Gesamtzyklus erneut
- Vollbild
- App kann auf Android als Home-/Launcher-App gewählt werden
- GitHub Action zum Bauen einer APK

## Empfohlene Ordner
Videos/Mickey Maus/01.mp4
Videos/Mickey Maus/02.mp4
Videos/Mickey Maus/03.mp4

Videos/Masha und der Baer/01.mp4
Videos/Masha und der Baer/02.mp4

Am besten Folgen mit 01, 02, 03 ... beginnen, damit die Reihenfolge eindeutig ist.

## APK komplett im Browser bauen

1. Auf GitHub ein neues leeres Repository anlegen.
2. Den Inhalt dieser ZIP **entpackt** in das Repository hochladen.
3. Commit auf `main`.
4. Oben `Actions` öffnen.
5. Workflow `Build FM Kids Player APK` öffnen.
6. Falls er nicht bereits durch den Upload gestartet wurde: `Run workflow`.
7. Nach erfolgreichem Build den Lauf öffnen.
8. Unter `Artifacts` → `FM-Kids-Player-APK` herunterladen.
9. ZIP des Artifacts öffnen. Darin liegt `app-debug.apk`.
10. APK auf dem S20 FE installieren.

## Ersteinrichtung auf dem Handy

1. FM Kids Player öffnen.
2. Elternbereich öffnet sich beim ersten Start.
3. Serie hinzufügen → Name → Video-Ordner → optional Titelbild.
4. Weitere Serien hinzufügen.
5. Elternbereich später: Überschrift lange gedrückt halten → PIN `2580`.
6. PIN anschließend ändern.

### Als Startbildschirm verwenden
Die App meldet sich zusätzlich als Android-Home-App an. Wenn Android/Samsung fragt, welche Start-App verwendet werden soll, kann `FM Kids Player` als Standard gewählt werden. Vorher sollte mindestens eine Serie eingerichtet sein.

Zum Zurückwechseln muss ein Erwachsener in den Android-Einstellungen die Standard-Start-App wieder auf `One UI Home` setzen.

## Hinweis zur Sperre
Die Launcher-Lösung ist für ein Kindergerät praktisch, aber keine MDM-/Device-Owner-Sicherheitslösung. Android erlaubt einer normalen APK nicht, ohne vorherige Geräte-Provisionierung sämtliche Systemwege dauerhaft zu sperren.
