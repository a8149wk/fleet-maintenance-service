#!/usr/bin/env bash
# =====================================================
# Fleet Maintenance System - Linux/macOS launcher
#
# Picks up:
#   ./config/database.properties   - DB connection
#   ./config/logging.properties    - log path & rotation
# Logs are written to whatever logging.file.name points to.
# =====================================================
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$APP_HOME"

JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx1024m -Dfile.encoding=UTF-8}"

APP_JAR="$(ls -1 fleet-maintenance-system-*.jar 2>/dev/null | head -n 1 || true)"
if [[ -z "${APP_JAR}" ]]; then
    echo "[ERROR] Could not find fleet-maintenance-system-*.jar in ${APP_HOME}" >&2
    exit 1
fi

mkdir -p logs

echo "Starting ${APP_JAR} from ${APP_HOME}"
echo "JAVA_OPTS=${JAVA_OPTS}"
echo

exec java ${JAVA_OPTS} -jar "${APP_JAR}" "$@"
