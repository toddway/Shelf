#!/bin/sh
# One-command release for Shelf.
#
#   ./release.sh <version>          prep only, then push yourself:  git push origin multiplatform <tag>
#   ./release.sh <version> --push   prep AND push (skips the manual last-look before going public)
#
# This bumps VERSION_NAME, verifies the build is green, commits, and tags. The push then triggers
# .github/workflows/release.yml, which publishes every KMP artifact (metadata + jvm + js + iOS) to
# the gh-pages Maven repo (https://toddway.github.io/Shelf) and creates a GitHub Release. Everything
# authenticates with the built-in GITHUB_TOKEN — no signing keys or Sonatype tokens.
#
# The push is the irreversible, public step — the default stops just before it so you can inspect
# the commit/diff first. Pass --push only when you're confident.
set -eu

BRANCH="multiplatform"
VERSION=""
PUSH=0
for arg in "$@"; do
    case "$arg" in
        --push) PUSH=1 ;;
        [0-9]*.[0-9]*.[0-9]*) VERSION="$arg" ;;
        *) echo "usage: ./release.sh <version> [--push]   (e.g. ./release.sh 3.0.0 --push)" >&2; exit 1 ;;
    esac
done
[ -n "$VERSION" ] || { echo "usage: ./release.sh <version> [--push]   (e.g. ./release.sh 3.0.0)" >&2; exit 1; }
TAG="v$VERSION"

# --- Guards: refuse to run unless the tree is a clean, releasable state ---
[ -n "$(git status --porcelain)" ] && { echo "error: working tree not clean — commit or stash first" >&2; exit 1; }
[ "$(git rev-parse --abbrev-ref HEAD)" = "$BRANCH" ] || { echo "error: not on $BRANCH" >&2; exit 1; }
git rev-parse -q --verify "refs/tags/$TAG" >/dev/null 2>&1 && { echo "error: tag $TAG already exists" >&2; exit 1; }

# 1. Bump the published version. (`-i.bak` + rm is portable across BSD/macOS and GNU sed.)
sed -i.bak -E "s/^VERSION_NAME=.*/VERSION_NAME=$VERSION/" gradle.properties && rm -f gradle.properties.bak

# 2. Verify the build is green at this version before tagging.
./gradlew --quiet build

# 3. Commit + tag.
git add gradle.properties
git commit -q -m "Release $TAG"
git tag "$TAG"

echo "Prepared $TAG"
if [ "$PUSH" -eq 1 ]; then
    echo "Pushing $BRANCH + $TAG to origin ..."
    git push origin "$BRANCH" "$TAG"
else
    echo "Release it with:  git push origin $BRANCH $TAG"
fi
