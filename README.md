# MCCreativeLab-Folia-Fork

Paperweight-Patcher-Fork, dessen Upstream **PaperMC/Folia (Branch `ver/26.3.x`)** ist:
MCCreativeLab-API/Server auf Folia-Region-Threading. Arbeitszustand und Patch-Matrix:
[`docs/folia/PORT_STATUS.md`](docs/folia/PORT_STATUS.md).

## Upstream-Modell (wichtig)

Das Folia-Repo enthält **nur Patch-Serien** (keine Quellen). Der GitUpstream des
paperweight-Patchers ist daher — wie bei Folia selbst — **PaperMC/Paper**, gepinnt auf
den exakten Paper-Commit `paperRef` aus Folia's `gradle.properties`
(`804dd0c3f08b666dc51a0250257915c002c59755`). Folia's komplette Serien sind als
Basisschicht vendored:

| Ebene | Folia-Patches | MCC-Patches |
|---|---|---|
| `mccreativelab-api/paper-patches/features` | 0001–0004 | 2001–2005 |
| `mccreativelab-server/paper-patches/features` | 0001–0006 | 2001–2008 |
| `mccreativelab-server/minecraft-patches/features` | 0001–0011 | 2001–2017 |

MCC-Patches behalten ihre alte Nummer unter Präfix `2` (MCC `0009` → `2009`).
Noch nicht portierte MCC-Patches liegen in `docs/folia/pending-patches/` und werden
einzeln zurückgeholt (siehe PORT_STATUS).

## Build

```
JAVA_HOME=~/.jdks/temurin-25 ./gradlew applyPatches      # Arbeitsbäume erzeugen/patchen
JAVA_HOME=~/.jdks/temurin-25 ./gradlew :mccreativelab-api:compileJava
JAVA_HOME=~/.jdks/temurin-25 ./gradlew createMojmapPaperclipJar
```

Konflikte in den generierten Git-Arbeitsverzeichnissen (`paper-api`, `paper-server`,
`src/minecraft`) sind wegwerfbar; Persistierung zurück in die versionierten Serien:

```
./gradlew rebuildPaperPatches                    # API-Paper-Patches
./gradlew :mccreativelab-server:rebuildPaperServerPatches
./gradlew :mccreativelab-server:rebuildMinecraftPatches
./gradlew :mccreativelab-server:rebuildAllServerPatches
```

Reihenfolge pro Konflikt: Fix im Arbeitsverzeichnis → `git add -A` → `git am --continue`
→ danach der passende `rebuild*`-Task. Siehe `../MCCreativeLab-Fork/docs/update-26.3/UPDATE.md`.

## Lizenz

Code erbt die Lizenz der Upstreams (GPLv3, siehe PATCHES-LICENSE und Paper/Folia-Upstream).
