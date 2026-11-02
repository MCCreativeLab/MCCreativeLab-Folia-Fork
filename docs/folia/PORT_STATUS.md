# Folia-Fork Port-Status

Stand: 2026-11-02. Repo: `MCCreativeLab-Folia-Fork`.
Upstream: **PaperMC/Folia `ver/26.3.x`** (GitUpstream PaperMC/Paper `804dd0c3f08b666dc51a0250257915c002c59755`
= Folia's `paperRef`-Pin, mcVersion 26.3, paperweight 2.0.0-beta.24, Java 25).
Group `de.verdox.mccreativelab`, Version `26.3-R0.1-SNAPSHOT-folia`.

## Architektur-Entscheidung (wichtigste Scaffold-Entscheidung)

Das Folia-Repo enthält ausschließlich Patch-Serien (54 Blobs, keine Quellen). Ein
paperweight-GitUpstream auf PaperMC/Folia könnte keine patchDir-Quellen liefern.
Daher: GitUpstream bleibt **PaperMC/Paper auf exakt den von Folia gepinnten Commit**
(wie bei Folia selbst), und Folia's komplette Serien werden als Basisschicht (0001+)
vendored. MCC-Patches laufen darüber; paperweight's `rebuildPatches` hat die finale
Nummerierung festgeschrieben. Ergebnis pro Ebene (apply-Korrektur = Folia zuerst, dann MCC):

| Ebene | Folia | MCC | Gesamt |
|---|---|---|---|
| `mccreativelab-api/paper-patches/features` | 0001–0004 | 0005–0009 | 9 |
| `mccreativelab-server/paper-patches/features` | 0001–0006 | 0007–0014 | 14 |
| `mccreativelab-server/minecraft-patches/features` | 0001–0011 | 0012–0028 | 28 |
| `mccreativelab-server/minecraft-patches/sources` (Datei-Patches) | — | 8 (NewChunkHolder, SerializableChunkData, BuiltInRegistries, PathPackResources, DedicatedServer, DedicatedServerProperties, AbstractFurnaceBlockEntity, StructureStart) | 8 |
| `mccreativelab-server/paper-patches/files` (Datei-Patches) | — | 3 (CraftServer, CraftResourcePack, CraftStructureTransformer) | 3 |

**Alle** MCC-Patches sind portiert — die 🔴-Liste (NewChunkHolder, SerializableChunkData,
0009-BlockBehavior, 0008-EntityBehaviour, 0017, 0002-PDC, 0012-Bootstrap, CraftServer,
0004, 0015) ist vollständig angewendet; 0010-Adapt war der Konflikt-Hotspot
(ServerLevel-Tick, siehe unten).

## Verifikation (2026-11-02)

- `./gradlew applyAllPatches` — **BUILD SUCCESSFUL**, alle Feature-/File-Patch-Tasks
  komplett aus dem Base-Baum wiederholt, Null Konflikte (persistierte Serien).
- `./gradlew :mccreativelab-api:compileJava` — **SUCCESSFUL** (Folia-API + MCC-API-Patches kompilierfähig).
- `./gradlew :mccreativelab-server:compileJava` — **SUCCESSFUL** (1m18s; gesamter
  Folia-Region-Threading-Baum + alle MCC-Server-Patches + de.verdox-Klassen).

Nachtrag: Die erste persistierte 0020-BlockBehavior-Serie enthielt drei versehentlich
übernommene Konfliktmarker-Restzeichen (`r`); bereinigt via git-fixup in den betroffenen
Serie-Commits und erneutem `rebuildMinecraftPatches`. Finale Serie kompiliert konfliktfrei.

## Patch-Matrix (Status nach erstem Port)

Legende: ✅ sauber angewendet | 🔧 mit angepassten Hunks portiert

### api/paper-patches/features
| Patch | Status |
|---|---|
| 0001–0004 Folia (timings, Region-scheduler-API, folia-supported, TPS-from-Region) | ✅ vendored |
| 0005 Adding-behaviours … 0009 Declare-nullability (5 MCC-Patches) | ✅ |

### server/paper-patches
| Patch | Status |
|---|---|
| 0001–0006 Folia (Region-Threading-Base, Build, Tests, Profiler, Watchdog, TPS) | ✅ vendored; Build-Patch (Brand-Id papermc:folia, leafpile-profiler, filterPatches=false) ins `mccreativelab-server/build.gradle.kts.patch` mergiert |
| 0007 BasicItemFormat, 0008/0010 BlockBehavior, 0009 ItemBehavior, 0011 legacy-rerouting | ✅ |
| 0012 PersistentDataObjects, 0013 EntityBehaviour, 0014 CraftEntity-Handles | ✅ |
| files: CraftServer, CraftResourcePack, CraftStructureTransformer | ✅ (CraftServer-Hunk kontextuell neu verankert) |

### server/minecraft-patches
| Patch (frühere MCC-Nr.) | Status |
|---|---|
| 0001–0011 Folia-Serien (Region-Threading-Base ~1 MB) | ✅ vendored |
| 0012 Repair(05), 0013 Item(07), 0014 Pass-through-pack(11), 0017 BoneMeal(16) | ✅ |
| 0015 Restore-block-callbacks(13), 0016 Per-stack/Water(14) | 🔧 LivingEntity `hurtServer`-Kontext (Folia) + `mcc$waterDamage` |
| 0018 PDC(02), 0019 EntityBehaviour(08), 0021 Bootstrap(12), 0022 super-calls(17) | ✅ |
| 0020 BlockBehavior(09) | 🔧 7 Konflikte: ServerLevel/LevelChunk/Block; siehe Design-Notizen |
| 0023 MCCWorldHook(01) | 🔧 Level-Feld nach Folia-Umbau verankert |
| 0024 BasicItemFormat(03), 0025 WorldGen(04), 0026 Serialization(06), 0028 SectionSnapshots(15) | ✅ |
| 0027 Adapt-hooks(10) | 🔧 ServerLevel-Tick-Regionen Folia-übernommen (Details unten) |
| sources: NewChunkHolder, SerializableChunkData, BuiltInRegistries, PathPackResources, DedicatedServer(Properties), Furnace, StructureStart | ✅ NewChunkHolder/SerializableChunkData hunks passen 1:1 auf Folia-Unterlage |

## Design-Notizen zu den Folia-Konfliktstellen (M2-Arbeitsliste)

1. **ServerLevel.tick()-Kern (0020/0027)**: Folia hat den klassischen Tick-Körper
   (blockTicks/fluidTicks/raids/chunkSource/entityTickList) aus `ServerLevel.tick()`
   entfernt und in den Region-Scheduler (`RegionizedWorldData`,
   `TickRegionScheduler.getCurrentRegionizedWorldData()`, moonrise-Voll-Tick) verlagert.
   MCC-Proxys `blockTicks.mcc$tick(…)` etc. können dort nicht als einfache Hunk-Hooks
   bestehen bleiben. → M2: Hooks an die Folia-Tick-Stellen (region-level tick in
   ServerLevel-Folia-Patch / `RegionizedWorldData`) neu ansetzen oder Behaviour-Dispatch
   über die bestehenden Vanilla-Callback-Points (0015 Restore-block-callbacks deckt
   Block/Item-Callbacks ohnehin ab).
2. **WorldBorder-Tick (0020)**: Folia tickt pro-Spieler-WorldBorders
   (`regionizedWorldData.getLocalPlayers()`); der MCC-Hook `getWorldBorder().mcc$tick()`
   ist entfallen. Falls Border-Hooks gebraucht werden: in Folia-Loop einhängen.
3. **scheduledEvents (0020)**: von Folia kommentiert („TODO any way to bring this in?“) —
   Bukkit-Scheduler ist auf Folia global deaktiviert; MCC-Hook entfällt folgerichtig.
4. **NewChunkHolder / SerializableChunkData (ChunkDataSave/LoadEvent)**: Hunks greifen.
   Thread-Kontext prüfen: Save-Runnable läuft im moonrise-Save-Pfad; vor Event-Dispatch
   in M2 Assert `TickThread.isTickThreadFor(chunk)` einbauen bzw. Event auf
   Region-Thread der Chunk-Region mappen (Bukkit.callEvent von Nicht-Region-Thread ist
   auf Folia tabu).
5. **Level-Felder (0021 MCCWorldHook)**: `mccWorldHook`-Feld bleibt auf `Level`;
   Folia hat nur die EAR-Zähler nach `RegionizedWorldData` verschoben.
6. **Brand**: Server-Jar trägt `Brand-Id papermc:folia` / `Brand-Name Folia` —
   `isBrandCompatible(papermc:folia)`-Checks und Plugin-Gating (`folia-supported: true`)
   funktionieren; OneBlock/Extension brauchen das Flag für M3.

## M3-Anker: Runtime Dimension Registration (createWorld)

Folia's CraftServer-Patch wirft in `createWorld(WorldCreator)`/`unloadWorld`
`UnsupportedOperationException` (siehe `paper-patches/features/0001` ≈ Zeile 865/874).
Geplanter eigener Feature-Patch (Stufe > 0014): createWorld auf den
**Global-Region-Thread** verlegen — Prüfsequenz: Aufrufer ist Region-/Async-Thread →
Task via `Bukkit.getGlobalRegionScheduler()` (Folia-Impl:
`io.papermc.paper.threadedregions.scheduler.FoliaGlobalRegionScheduler`) ausführen,
`CompletableFuture` designt übergeben (niemals auf dem globalen Region-Thread joinen —
mcc-util `createWorldAsync`-Muster beibehalten); danach Regionen des neuen Levels über
`ThreadedRegionizer` aktiv. Unload analog. Dies ersetzt OneBlock's
`Bukkit.createWorld`-Blocker (Insel-Erzeugung).

## Serie neu bauen / Konfliktlösung

1. `./gradlew applyAllPatches` (Erzeugt `paper-api/`, `paper-server/`,
   `mccreativelab-server/src/minecraft/` als Wegwerf-Arbeitsbäume).
2. Konflikt im jeweiligen generierten Git-Repo lösen, `git add -A`, `git am --continue`.
3. Persistieren: `./gradlew rebuildPaperPatches`,
   `:mccreativelab-server:rebuildPaperServerPatches`,
   `:mccreativelab-server:rebuildMinecraftPatches` (paperweight schreibt die
   versionierten Serien neu; Nummerierung wird dabei fortlaufend normalisiert —
   das Präfix-Schema 2xxx aus dem Scaffold ist nach erstem Rebuild obsolet).
4. Danach `applyAllPatches` erneut laufen lassen — muss konfliktfrei durchlaufen.

Hinweis Gradle-Wiring: `applyAllPatches` ist ein `RunNestedBuild`-Task; das Root-Build
hat deshalb die `mustRunAfter`-Referenzen auf Root-Tasks gegen existierende Tasks
gelöst (namensbasiert) statt als Pfad-Strings — sonst scheitert der Nested-Run
(Fehler „Task with name 'applyPaperCheckstyleFilePatches' not found“).
