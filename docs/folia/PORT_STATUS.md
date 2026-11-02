# Folia-Fork Port-Status (M1-Scaffold)

Stand: 2026-11-02. Repo: `MCCreativeLab-Folia-Fork`, Upstream PaperMC/Folia `ver/26.3.x`
(GitUpstream PaperMC/Paper `804dd0c3f08b666dc51a0250257915c002c59755`, mcVersion 26.3,
paperweight 2.0.0-beta.24, Java 25, Group `de.verdox.mccreativelab`,
Version `26.3-R0.1-SNAPSHOT-folia`).

## Architektur-Entscheidung

Folia-Repo = Patches-only (54 Blobs). Ein Fork-of-Folia kann Folia nicht als
Quellen-Upstream referenzieren. Lösung: Folia's Patch-Serien als Basisschicht (Nummern
0001+) ins eigene Repo vendored; MCC-Patches als obere Schicht (2001+, alte Nummer mit
Praefix 2). applyPatches faehrt Paper@paperRef aus, legt Folia-Serien, dann MCC-Serien
darueber. Rebuild-Tasks persistieren Konflikte zurueck in die Serien.

## Patch-Matrix

Legende: ✅ aktiv (Stage A), ⏳ Stage B (in `docs/folia/pending-patches/`, noch nicht portiert),
Status nach erstem applyPatch-Durchlauf wird nachgezogen.

### mccreativelab-api/paper-patches/features

| Nr | Patch | Status |
|---|---|---|
| 0001-0004 | Folia-Basis (timings, Region-Scheduler-API, folia-supported-Gating, TPS-from-Region) | ✅ vendored |
| 2001 | Adding-behaviours | ⏳ Stage A |
| 2002 | GeneratedStructure in AsyncStructureGenerateEvent | ⏳ Stage A |
| 2003 | PersistentDataObjects (API) | ⏳ Stage A |
| 2004 | Server-Resourcepack wechselbar | ⏳ Stage A |
| 2005 | Nullability MCC-API | ⏳ Stage A |

### mccreativelab-server/paper-patches

| Nr | Patch | Status |
|---|---|---|
| 0001-0006 | Folia-Basis (Region-Threading-Base, Build, Tests, Profiler, Watchdog, TPS) | ✅ vendored |
| 2001 | Add-PersistentDataObjects | ⏳ Stage B (PDC/Kontext-Thread) |
| 2002 | Add-BasicItemFormat | ⏳ Stage A |
| 2003/2006 | Add-BlockBehavior | ⏳ Stage A |
| 2004 | Add-ItemBehavior | ⏳ Stage A |
| 2005 | Add-EntityBehaviour | ⏳ Stage B |
| 2007 | CraftEntity-Handles | ⏳ Stage B |
| 2008 | MaterialRerouting legacy | ⏳ Stage A |
| files | CraftResourcePack, CraftStructureTransformer | ⏳ Stage A |
| files | CraftServer.java.patch | ⏳ Stage B (Folia patcht CraftServer massiv) |

### mccreativelab-server/minecraft-patches

| Nr | Patch | Status |
|---|---|---|
| 0001 | Folia Region-Threading-Base (~1MB) | ✅ vendored |
| 0002-0011 | uebrige Folia-Minecraft-Serien | ✅ vendored |
| 2001 | Add-MCCWorldHook | ⏳ Stage B (ServerLevel/Region) |
| 2002 | Add-PersistentDataObjects | ⏳ Stage B (MinecraftServer-PDC) |
| 2003 | Add-BasicItemFormat | ⏳ Stage B |
| 2004 | Add-WorldGenerationBehaviour | ⏳ Stage B |
| 2005 | Add-RepairItemBehavior | ⏳ Stage A |
| 2006 | SerializationBehavior-Draft | ⏳ Stage B |
| 2007 | Add-ItemBehavior | ⏳ Stage A |
| 2008 | Add-EntityBehaviour | ⏳ Stage B |
| 2009 | Add-BlockBehavior | ⏳ Stage B (blockTicks/Region) |
| 2010 | Adapt-behavior-hooks | ⏳ Stage B |
| 2011 | Pass-through-pack-resources | ⏳ Stage A |
| 2012 | CreativeLab-vor-Plugin-Bootstrap | ⏳ Stage B (MinecraftServer-Bootstrap) |
| 2013 | Restore-block-callbacks | ⏳ Stage A |
| 2014 | Per-Stack-Item-Callbacks/Water | ⏳ Stage A |
| 2015 | Section-Snapshots | ⏳ Stage B |
| 2016 | Bone-meal custom growth | ⏳ Stage A |
| 2017 | Preserve-vanilla-entity-super-calls | ⏳ Stage B |
| sources | BuiltInRegistries, PathPackResources | ⏳ Stage A |
| sources | NewChunkHolder, SerializableChunkData, DedicatedServer(Properties), AbstractFurnaceBlockEntity, StructureStart | ⏳ Stage B |

## Serie neu bauen

`siehe README` — applyPatches, Konflikt im generierten Arbeitsverzeichnis loesen
(git add + git am --continue), dann rebuildPaperPatches /
:mccreativelab-server:rebuildPaperServerPatches / :mccreativelab-server:rebuildMinecraftPatches.
