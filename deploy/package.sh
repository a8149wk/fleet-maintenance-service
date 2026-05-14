#!/usr/bin/env bash
# Build the Fleet Maintenance System and assemble a deployable
# distribution under dist/.
#
# Usage:
#   ./deploy/package.sh            - full build + zip
#   ./deploy/package.sh --skip-build  - reuse target/ artifact
#   ./deploy/package.sh --no-zip      - leave dist/ folder, skip zip
set -euo pipefail

SKIP_BUILD=0
NO_ZIP=0
for arg in "$@"; do
    case "$arg" in
        --skip-build) SKIP_BUILD=1 ;;
        --no-zip)     NO_ZIP=1 ;;
        *) echo "Unknown option: $arg" >&2; exit 2 ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DEPLOY_DIR="$REPO_ROOT/deploy"
TARGET_DIR="$REPO_ROOT/target"
DIST_ROOT="$REPO_ROOT/dist"

cd "$REPO_ROOT"

if [[ "$SKIP_BUILD" -eq 0 ]]; then
    echo "[1/3] Building JAR (mvn clean package -DskipTests)..."
    mvn -B clean package -DskipTests
else
    echo "[1/3] Skipping build (--skip-build)"
fi

JAR="$(ls -1 "$TARGET_DIR"/fleet-maintenance-system-*.jar 2>/dev/null | grep -v '\.original$' | head -n 1 || true)"
if [[ -z "$JAR" ]]; then
    echo "[ERROR] Could not find fleet-maintenance-system-*.jar under $TARGET_DIR" >&2
    exit 1
fi

VERSION="$(basename "$JAR" .jar)"
VERSION="${VERSION#fleet-maintenance-system-}"
STAGE="$DIST_ROOT/fleet-maintenance-$VERSION"

echo "[2/3] Staging $STAGE"
rm -rf "$STAGE"
mkdir -p "$STAGE/config" "$STAGE/logs"
cp "$JAR" "$STAGE/"
cp -R "$DEPLOY_DIR/config/." "$STAGE/config/"
cp "$DEPLOY_DIR/run.bat" "$STAGE/"
cp "$DEPLOY_DIR/run.sh"  "$STAGE/"
cp "$DEPLOY_DIR/ecosystem.config.js" "$STAGE/"
cp "$DEPLOY_DIR/README.md" "$STAGE/"
chmod +x "$STAGE/run.sh"

if [[ "$NO_ZIP" -eq 0 ]]; then
    ZIP="$DIST_ROOT/fleet-maintenance-$VERSION.zip"
    echo "[3/3] Compressing -> $ZIP"
    rm -f "$ZIP"
    (cd "$DIST_ROOT" && zip -r "$(basename "$ZIP")" "fleet-maintenance-$VERSION" >/dev/null)
    echo ""
    echo "Done. Folder: $STAGE"
    echo "      Zip:    $ZIP"
else
    echo "[3/3] Skipping zip (--no-zip)"
    echo ""
    echo "Done. Folder: $STAGE"
fi
