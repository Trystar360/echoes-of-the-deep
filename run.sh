#!/usr/bin/env bash
# Convenience wrapper: sets JAVA_HOME to the brew JDK 21, then forwards to gradlew.
#   ./run.sh build        # build the mod jar
#   ./run.sh runClient    # launch single-player to playtest
#   ./run.sh runServer    # headless server smoke test
set -e
export JAVA_HOME="${JAVA_HOME:-$(brew --prefix openjdk@21 2>/dev/null)/libexec/openjdk.jdk/Contents/Home}"
if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "JDK 21 not found. Install with: brew install openjdk@21" >&2
  exit 1
fi
exec "$(dirname "$0")/gradlew" "$@"
