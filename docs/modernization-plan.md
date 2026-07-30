# Shelf toolchain modernization + GitHub-first CI/publishing + BuildChecks 4.x

Plan for a future session. The library code (storage/serializer decorators + `ShelfList`) is done and
green on JVM/JS; this plan is about the **build, CI, and publishing** around it. The model is
BuildChecks 4.x: GitHub Actions for CI, a **gh-pages Maven repo** for publishing (only
`GITHUB_TOKEN` — no Sonatype, no signing keys), and BuildChecks itself posting a commit status +
sticky PR comment.

> **Scope of "match BuildChecks":** this means adopting BuildChecks' **GitHub-based CI + publishing
> pattern** — not matching its toolchain versions. Shelf's Kotlin/Gradle are chosen on their own merits
> (see "Versioning & toolchain policy" below). BuildChecks 4.x is no longer a Gradle plugin; it's a **CLI**
> resolved as a Maven artifact and run via `JavaExec` (see Phase 3), which is why nothing about its build
> constrains Shelf.

## Current state (2023-era, blocks everything)

- Gradle **6.8.3** (won't run on JDK 17/21 — forced JDK 11 during this work).
- Kotlin **1.5.0**, coroutines 1.4.2, serialization 1.2.0, ktor 1.1.3.
- Dead repositories referenced: `jcenter()`, `dl.bintray.com/kotlin/kotlin-eap`.
- Vestigial **AGP 3.3.2** classpath — there is **no Android target** in the `kotlin {}` block, so it's unused.
- `buildkonfig` + `ktor` exist only for one networking test (`JvmTests`) that currently fails with a
  live `401 Unauthorized` — unrelated to Shelf's core, a maintenance liability.
- Publishing via `com.vanniktech.maven.publish` → Sonatype **s01** (Maven Central), with the ceremony
  that implies (staging, signing keys).
- Old `com.toddway.buildchecks` **v2.13** plugin wired in `gradle/checks.gradle`.
- Groovy `build.gradle` files, no version catalog.

## Phase 1 — Toolchain modernization (do first; checkpoint = green `./gradlew build` on JDK 17)

Work on a branch off `feature/storage-serializer-decorators` (or after it merges).

1. **Gradle wrapper** 6.8.3 → **8.14** (matches BuildChecks). Unlocks JDK 17/21; removes the JDK 11 pin.
2. **Kotlin** 1.5.0 → **2.0.21** (recommended) — code is tiny, migration risk low, and it modernizes the
   MPP/JS/serialization stack. Lower-risk fallback: 1.9.24. Bump in lockstep:
   - coroutines → 1.9.0, serialization → 1.7.3, atomicfu → 0.25.0 (plugin still required — it transforms
     `SynchronizedObject` on JVM; a missing plugin silently no-ops the locks, caught by a failing test).
3. **Repositories:** delete `jcenter()` and the bintray/kotlin-eap URLs everywhere (root `build.gradle`,
   `settings.gradle` pluginManagement). Keep `mavenCentral()` + `google()` (google only if an Android
   target is ever added — currently not needed).
4. **Remove vestigial AGP** (`com.android.tools.build:gradle:3.3.2`) — unused, no Android target.
5. **MPP DSL migration** (1.5 → 2.0):
   - `iosX64('ios')` → declare the real set: `iosX64()`, `iosArm64()`, `iosSimulatorArm64()` (default
     hierarchy template unifies them under `iosMain`). Note `iosMain`'s `DiskStorage`/`Clock` actuals
     currently assume one target — verify they compile for arm64/simulator too.
   - `js()` → `js(IR) { browser(); nodejs() }` (IR is the only backend in 2.0).
   - Source sets to the modern accessor DSL: `kotlin { sourceSets { commonMain.dependencies { … } } }`.
   - Drop `enableFeaturePreview("GRADLE_METADATA")` (default since Gradle 6).
6. **Decision — the networking test:** remove `JvmTests` (the ktor HTTP-cache demo) plus the `ktor` and
   `buildkonfig` machinery, or keep and fix it. Recommendation: **remove** — it isn't testing Shelf core,
   it needs a live endpoint + token, and it fails today. Slims the build considerably.
7. **Convert `build.gradle` → `build.gradle.kts` + `gradle/libs.versions.toml`** (version catalog), matching
   BuildChecks. Optional but recommended for consistency across your repos. Can be deferred to its own step.

Checkpoint: `./gradlew build` green on JDK 17 for jvm + js + ios (metadata at minimum), all 26 new tests pass.

### Phase 1 — execution notes (done)

Completed on branch `feature/phase-1-toolchain` off `multiplatform`. `./gradlew clean build` is green on
JDK 17: `shelf` jvmTest 37 / jsBrowserTest 36 / iosSimulatorArm64Test 36, `shelf-coroutines` jvmTest 5 —
all 0 failures. Deviations from the plan above, and why:

- **atomicfu dropped entirely** (plan step 2 kept it). Once tests actually ran (see the collision note
  below), it was clear Shelf uses locks but no `atomic()` fields, and atomicfu's `synchronized` inline
  only returns the block value where its Gradle plugin transforms it (JVM) — it misbehaves on the JS/Native
  IR backends. Replaced with a dependency-free `expect`/`actual` `Lock` + `withLock` inline
  (`ReentrantLock` on JVM, `NSRecursiveLock` on Apple, no-op on JS). Core is now fully dependency-free. See
  ADR 0001's updated §1.
- **Latent duplicate `MemoryStorage`** removed from `commonTest` (`ShelfTests.kt`). A pre-existing test-only
  `MemoryStorage` collided (same FQN) with the new `commonMain` decorator; under K2 the `commonTest`
  reference bound to the fake, so the decorator tests silently exercised the wrong class (this is what made
  it look like a lock miscompilation). Deleting the fake let the reference bind to the real decorator.
- **JS is browser-only** (`js(IR) { browser() }`; plan step 5 said `browser(); nodejs()`). The JS
  `DiskStorage` is backed by browser `localStorage`, which has no Node equivalent. Add `nodejs()` only
  alongside a Node-compatible storage backend. `kotlin-js-store/yarn.lock` is committed so
  `kotlinStoreYarnLock` is stable.
- **Legacy verification/publishing machinery unwired, not deleted.** The `com.toddway.buildchecks` 2.13
  plugin, `com.vanniktech.maven.publish`, cpd, and detekt don't run on Gradle 8.14 and are slated for
  replacement in Phases 3–4, so their plugin applications / `apply from` lines were removed to reach a green
  `build`. The `gradle/*.gradle` helpers (`buildChecks.gradle`, `checks.gradle`, `cpd.gradle`,
  `jacoco.gradle`, `detekt.gradle`) are left **on disk, dormant** for Phase 3 to modernize.
- **`-Xexpect-actual-classes`** compiler arg added (expect/actual classes are Beta in K2; also enables
  `DiskStorage`/`Clock` actualization via inherited fake-overrides).
- **CI/iOS reality (for Phase 2):** iOS Apple targets only build on a macOS host — a Linux CI runner builds
  jvm + js only. Gating iOS in CI requires a `macos-*` runner. The all-platform green above was verified
  locally on an Apple-Silicon Mac; treat that as the iOS/JS confidence check and decide CI runner OS in
  Phase 2.
- **Not done (deferred):** plan step 7 (`.kts` + version catalog) — left as its own step as the plan allows.

## Phase 2 — GitHub CI (`.github/workflows/ci.yml`) — mirror BuildChecks

Direct port of BuildChecks' `ci.yml`. Triggers on push to `main` + PRs.

```yaml
permissions:
  contents: read
  statuses: write        # post the `buildchecks` commit status
  pull-requests: write   # post/update the summary comment on PRs
```

Steps: `checkout` → `setup-java 17` → `gradle/actions/setup-gradle` → `./gradlew build` (produces JUnit
XML + JaCoCo XML) → **BuildChecks gate** → upload report artifact → job summary from `summary.md` →
commit status → PR comment. Fork PRs get a read-only token, so the status/comment steps self-skip; the
job's own pass/fail still gates them.

## Phase 3 — BuildChecks 4.x integration (matches SW's `buildchecks-4-migration` branch)

**Reference implementation:** SW's `buildchecks-4-migration` branch —
`gradle-plugins/src/main/java/checks/ChecksPlugin.kt`, root `buildchecks.toml`, and
`buildchecks-baseline.txt`. That's the authoritative "how we do v4" model. It resolves the v4 CLI from
BuildChecks' own gh-pages Maven repo, gates via a `JavaExec` task, and posts status + comment from a
CI-agnostic `GitHubPublisher`. Shelf reuses the same v4 setup; only the **GitHub posting** differs (native
GitHub Actions steps instead of Bitrise). Remove the old `com.toddway.buildchecks` v2.13 plugin and retire
`gradle/checks.gradle`'s bespoke task.

1. **Resolve + run the CLI (exactly as SW does).** The CLI is a normal Maven artifact on BuildChecks'
   gh-pages repo — no jar to vendor, and it gives a local `./gradlew buildchecks`:
   ```kotlin
   repositories { maven("https://toddway.github.io/BuildChecks") }
   val buildchecks by configurations.creating
   dependencies { buildchecks("com.toddway:buildchecks:4.0.12") }

   tasks.register<JavaExec>("buildchecks") {
       group = "verification"
       workingDir = rootProject.projectDir
       classpath = configurations["buildchecks"]
       mainClass.set("buildchecks.cli.MainKt")
       args("check")                       // add "--open" for local runs
   }
   tasks.register<JavaExec>("buildchecksBaseline") {
       workingDir = rootProject.projectDir
       classpath = configurations["buildchecks"]
       mainClass.set("buildchecks.cli.MainKt")
       args("baseline")
   }
   ```
   (Simpler CI-only alternative: the composite action `uses: toddway/BuildChecks@v4.0.x` downloads the fat
   jar. The `JavaExec` route matches SW and also runs locally — prefer it.)

2. **`buildchecks.toml` at the repo root** — gates only (SW's shape; tune for a small library):
   ```toml
   [reports]
   output_dir = "build/reports/buildChecks"
   [gates]
   min_coverage_percent = 52.0
   max_new_findings = 0
   min_changed_line_coverage = 80   # skips when no base ref; CI passes --base-ref origin/<base>
   ```
   Plus `buildchecks-baseline.txt`, generated by `./gradlew buildchecksBaseline`.

3. **Reports it auto-discovers**, already emitted by the build: JUnit XML (kotlin.test → JUnit on JVM),
   JaCoCo XML (`jacocoTestReport { reports { xml.required = true } }`), detekt XML (Shelf's existing
   `gradle/detekt.gradle`). **MPP coverage caveat:** JaCoCo only covers `jvmTest`; js/native have no
   standard coverage, so the coverage gate reflects JVM. Kover is the MPP-native alternative if aggregated
   coverage matters — decide (open decision #6).

4. **GitHub posting — the part that "looks different" from SW** (GitHub Actions, not Bitrise). In `ci.yml`,
   after the gate step: upload the report dir as an artifact, then post the `buildchecks` commit status and
   sticky PR comment with `gh`:
   ```yaml
   - name: Upload BuildChecks report
     if: always()
     id: report
     uses: actions/upload-artifact@v4
     with: { name: buildchecks-report, path: build/reports/buildChecks }

   - name: Post BuildChecks commit status
     if: always()
     env: { GH_TOKEN: "${{ github.token }}" }
     run: |
       state="${{ steps.gate.outcome == 'success' && 'success' || 'failure' }}"
       gh api -X POST "repos/${{ github.repository }}/statuses/${{ github.event.pull_request.head.sha || github.sha }}" \
         -f state="$state" -f context="buildchecks" \
         -f description="$(cat build/reports/buildChecks/summary.txt)" \
         -f target_url="${{ steps.report.outputs.artifact-url }}"

   - name: Post BuildChecks PR comment
     if: always() && github.event_name == 'pull_request'
     env: { GH_TOKEN: "${{ github.token }}" }
     run: |
       cp build/reports/buildChecks/summary.md comment.md
       printf '\n📦 **[Download full HTML report](%s)**\n' "${{ steps.report.outputs.artifact-url }}" >> comment.md
       gh pr comment "${{ github.event.pull_request.number }}" --body-file comment.md --edit-last --create-if-none
   ```
   Same `buildchecks` status + sticky-comment outcome SW's `GitHubPublisher` produces, native to Actions.
   (One-code-path alternative: port SW's `GitHubPublisher` verbatim — it already reads `GITHUB_SHA` /
   `GITHUB_REF` / `GITHUB_REPOSITORY` — and call it from a `postChecks` finalizer. For a GitHub-Actions-only
   repo the `gh` steps are lighter; recommendation: use them.)

> **Note on the earlier security flag:** SW's `buildchecks-4-migration` branch **already removed** the
> hardcoded fallback token — its v4 `GitHubPublisher` is `GITHUB_TOKEN`-only (a `DEFAULT_REPO_SLUG`
> constant is the only literal). The v3.3 token still lives in git history on `dev`, so it should still be
> **revoked**, but there's nothing to re-fix on the migration branch. Shelf's config is `GITHUB_TOKEN`-only.

## Phase 4 — Publishing like BuildChecks 4.x (gh-pages Maven repo, no Sonatype)

Swap `com.vanniktech.maven.publish` + Sonatype for the core `maven-publish` plugin writing to a local
`build/maven-repo`, then commit that to a `gh-pages` branch. Consumers resolve from GitHub Pages. Only
`GITHUB_TOKEN` — no signing keys, no Sonatype account/staging.

**This model is already proven:** BuildChecks itself publishes its CLI to `https://toddway.github.io/BuildChecks`,
and SW's v4 branch consumes it from there (Phase 3, step 1). Shelf would publish to
`https://toddway.github.io/Shelf` the same way — one consistent gh-pages Maven pattern across your repos.

1. `maven-publish` config with a `Pages` repository at `layout.buildDirectory.dir("maven-repo")`. KMP
   auto-creates a publication per target (`kotlinMultiplatform` metadata, `jvm`, `js`, `iosX64`, …);
   `publishAllPublicationsToPagesRepository` publishes them all. Set coordinates deliberately
   (`group = com.toddway.shelf`; decide the root artifactId — currently capital `Shelf`).
2. **`.github/workflows/release.yml`** on tag `v*` (port BuildChecks'): `./gradlew assemble` →
   `publishAllPublicationsToPagesRepository` → check out `gh-pages` in a worktree (orphan on first
   release) → `touch .nojekyll` → copy `build/maven-repo/.` → commit + push. Optionally
   `gh release create` with source/artifacts and `--generate-notes`.
3. **Consumer change (SW app + README):**
   ```kotlin
   repositories { maven { url = uri("https://toddway.github.io/Shelf/") } }
   dependencies {
       implementation("com.toddway.shelf:shelf:2.1.0")
       implementation("com.toddway.shelf:shelf-coroutines:2.1.0") // if using ShelfList
   }
   ```

**Tradeoff to confirm:** gh-pages Maven is drastically simpler but consumers must add the repo URL, and
it drops the current Maven Central presence (`com.toddway.shelf:Shelf` on Central). Since the SW app is
the primary consumer and can add one repo line, this matches the BuildChecks simplification. Keep Maven
Central only if public/global discoverability (no repo line for strangers) is a hard requirement.

## Versioning & toolchain policy (decided)

- **Next published version = `3.0.0`** (major), set at Phase 4 — not the `2.1.0` currently in
  `gradle.properties`. Rationale: even though the new API is purely additive, this release (a) raises the
  minimum consumer Kotlin from 1.5 → 2.0 and (b) changes distribution (Maven Central → gh-pages, and
  normalizes the artifactId, below). Both break a drop-in upgrade, so signal it as major.
  **`VERSION_NAME` stays `2.1.0` until Phase 4** — it has no effect until publish, and the number is
  decided together with the publishing/coordinate questions in one place.
- **artifactId normalization:** fold `Shelf` → lowercase `shelf` (+ `shelf-coroutines`) into the same
  3.0.0 / gh-pages cutover, since coordinates are changing anyway.
- **Shelf is a broadly-consumed public artifact**, so keep the **published Kotlin conservative** (stay on
  the 2.0.x baseline). For a library, the *published* Kotlin sets the **minimum Kotlin a consumer must
  have** — bumping it narrows reach. Do **not** chase latest Kotlin for Shelf's publish version.
  (`languageVersion`/`apiVersion` can hold an older target on a newer compiler, but for MPP klibs the floor
  tracks the compiler fairly tightly — verify the emitted metadata version rather than assume.)
- **Shelf's Gradle is invisible to consumers** (they resolve a Maven artifact, they never run Shelf's
  build), so it's free to move to latest stable: **Gradle 8-latest now, Gradle 9 only after Phase 3's
  quality tooling is confirmed 9-clean** (the current build already warns it's 9-incompatible).
- **BuildChecks 4.x imposes no Kotlin/Gradle floor on Shelf** — it's a forked-JVM CLI (`JavaExec`), not a
  plugin, so its own Kotlin/Gradle are free to be latest. The only consumer-facing constraint is the **CLI
  jar's JDK/bytecode target**, which must be ≤ the JDK the consuming build's `JavaExec` runs (JDK 17 in
  CI); pin a Java toolchain on the task if you ever need to decouple that.
- **Any Kotlin/Gradle latest-stable bump is a post-Phase-4 step**, done on its own branch — not folded into
  Phases 1–4.

## Open decisions

Resolved this pass:

1. ~~**Kotlin target:** 2.0.21 vs 1.9.24.~~ → **2.0.21** (done in Phase 1); keep the published version
   conservative going forward per the policy above.
2. ~~**Networking test:** remove vs keep.~~ → **removed** (`JvmTests` + ktor + buildkonfig gone).
3. ~~**Build script format:** `.kts` + version catalog now vs later.~~ → **later** (deferred; still Groovy
   `.gradle`). Its own step, post-Phase-1.
5. ~~**iOS targets:** confirm actuals compile for arm64/sim.~~ → **confirmed**: `iosX64`/`iosArm64`/
   `iosSimulatorArm64` all compile, and `iosSimulatorArm64Test` runs green (36/36).

Still open:

4. **Publishing target:** gh-pages Maven repo (recommended) vs stay on Maven Central — decided at Phase 4,
   together with 3.0.0 + the artifactId normalization above.
6. **Coverage tool** BuildChecks 4.x ingests for MPP: JaCoCo XML (JVM-only coverage) vs Kover
   (MPP-aggregated) — decide in Phase 3.

## Sequencing

Phase 1 (green build on modern JDK) → Phase 2/3 together (CI + BuildChecks status/comment) → Phase 4
(release workflow + publishing). Each phase is an independent, reviewable checkpoint.

> **CI/iOS note for Phase 2:** Kotlin/Native Apple targets only build on a **macOS host** — a Linux CI
> runner builds jvm + js only. Gating iOS in CI requires a `macos-*` runner (has Xcode + simulators). The
> Phase 1 all-platform green was verified locally on Apple Silicon; treat that as the iOS/JS confidence
> check and decide CI runner OS in Phase 2.
