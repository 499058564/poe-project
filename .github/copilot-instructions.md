# Copilot Instructions

## Current repository state

- This repository is currently **doc-first**: `Readme.md` and `doc/tasks/**` define the intended Java/JavaFX application, but the Gradle multi-module scaffold described there is not checked in yet.
- Treat the task docs as the source of truth for module boundaries, package naming, and planned workflows. Do not invent extra modules or tooling that are not described there.

## Build, test, and packaging commands

- Before any POB- or translation-source-related work, initialize submodules:
  - `git submodule update --init --recursive`
- The documented Gradle commands are:
  - `.\gradlew build`
  - `.\gradlew :common:build`
  - `.\gradlew :app-ui:run`
  - `.\gradlew test`
  - single test: `.\gradlew test --tests "com.poe.SomeTest"` or `.\gradlew :module:test --tests "com.poe.SomeTest"`
  - packaging: `.\gradlew jpackage`
- JUnit 5 is the planned test platform (`useJUnitPlatform()` in the Gradle skeleton docs).
- No lint command or lint tool is documented in the current repository. Do not add one unless the repo is updated to include it.
- Because the wrapper and Gradle files are not committed yet, verify whether the current task is expected to add the scaffold before attempting these commands.

## High-level architecture

- The planned application is a **Java 17 + JavaFX desktop tool** for Path of Exile, with a layered multi-module Gradle design:
  - `common`: shared utilities, constants, and exceptions
  - `app-ui`: JavaFX UI, FXML/controllers, reusable components, theme resources
  - `app-core`: orchestration layer for config, services, and event-driven coordination
  - `data-provider`: upstream data fetchers/importers for PoE Wiki / poedb / PoeCharm2 / poe.ninja
  - `data-cache`: local SQLite cache, migrations, DAO layer, FTS5 search indexes
  - `pob-runtime`: Path of Building Community submodule/runtime
  - `pob-adapter`: Java models and XML mapping for POB data
  - `pob-ipc`: LuaJIT process management and JSON request/response protocol
- Planned data flow:
  - **content sync**: PoE Wiki/API data -> `data-provider` -> SQLite in `data-cache` -> services in `app-core` -> JavaFX views in `app-ui`
  - **build calculation**: UI action -> `app-core` -> `pob-adapter` serializes/parses POB structures -> `pob-ipc` talks to headless Lua/POB -> structured results return to UI
- Keep module dependencies directional. `app-ui` should depend on orchestration/services rather than reaching directly into low-level persistence or POB runtime details.

## Key conventions

- Package names follow module ownership. The docs establish roots such as `com.poe.common`, `com.poe.ui`, and `com.poe.core`; keep new code in the package tree that matches its layer instead of mixing responsibilities.
- Persist application state under `~/.poe-tool/`:
  - `config.json`
  - `data/poe.db`
  - `logs/`
- Configuration should prefer explicit user-managed paths. For POB, resolve in this order: configured `pobPath`, common Windows install locations, then the checked-out submodule.
- UI communication is intended to be **event-driven** via `AppEventBus`, with both sync and async posting:
  - use async events for background work like search and data sync
  - use sync events for immediate UI/state reactions such as selected-node updates
- Search is designed around **SQLite FTS5**, not a database-agnostic abstraction. Keep search logic close to `data-cache`/DAO concerns and preserve the documented `items_fts` + `base_items` join pattern.
- Name matching should use the shared normalization helper (`StringUtils.normalizeName`) instead of ad hoc lowercasing or whitespace stripping.
- Translation flow is intentionally layered: user custom translations -> SQLite translation table -> original source text fallback.
- PoeCharm2 is the planned Git submodule source for baseline Chinese translation data; prefer importing and normalizing it instead of hand-maintaining a parallel built-in dictionary.
- Wiki ingestion has explicit throughput rules from the task docs:
  - rate limit to **5 req/s**
  - retry up to **3 times** with exponential backoff
  - sync in **500-row batches**
  - publish progress/completion events during long-running syncs
- For persistence tests, prefer **in-memory SQLite** when validating DAO or FTS behavior, even though `Readme.md` mentions H2 at a high level. The task docs for `data-cache` are more specific and align with FTS5 usage.
- The UI theme is not generic JavaFX styling: use AtlantaFX dark styling plus the repo’s custom `/theme/dark-theme.css`, including the documented PoE rarity colors.
- POB integration must respect the repo’s packaging constraints: the submodule lives at `pob-runtime/src/main/pob`, and packaged artifacts must exclude POB `assets/` resources.
