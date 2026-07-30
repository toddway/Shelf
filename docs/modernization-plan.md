# Shelf toolchain modernization + GitHub-first CI/publishing + BuildChecks 4.x

Plan for a future session. The library code (storage/serializer decorators + `ShelfList`) is done and
green on JVM/JS; this plan is about the **build, CI, and publishing** around it. The model is
BuildChecks 4.x: GitHub Actions for CI, a **gh-pages Maven repo** for publishing (only
`GITHUB_TOKEN` — no Sonatype, no signing keys), and BuildChecks itself posting a commit status +
sticky PR comment.

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

## Open decisions to confirm before executing

1. **Kotlin target:** 2.0.21 (recommended) vs 1.9.24 (lower churn).
2. **Networking test:** remove `JvmTests` + ktor + buildkonfig (recommended) vs keep and fix.
3. **Build script format:** convert to `.kts` + version catalog now (matches BuildChecks) vs later.
4. **Publishing target:** gh-pages Maven repo (recommended, matches BuildChecks) vs stay on Maven Central.
5. **iOS targets:** confirm the `iosMain` actuals (`DiskStorage`, `Clock`) compile for
   `iosArm64`/`iosSimulatorArm64`, not just the legacy single `iosX64('ios')`.
6. **Coverage tool** BuildChecks 4.x ingests for MPP: JaCoCo XML vs Kover — confirm.

## Sequencing

Phase 1 (green build on modern JDK) → Phase 2/3 together (CI + BuildChecks status/comment) → Phase 4
(release workflow + publishing). Each phase is an independent, reviewable checkpoint.
