#!/usr/bin/env bash
# Build the Octaves of the One showcase world from the echoes_showcase datapack,
# headlessly, and package it as a ready-to-play save.
#
#   1. (re)generate the datapack         python3 gen_showcase.py
#   2. fresh superflat server world with the datapack installed
#   3. the minecraft:load tag builds the hall on first start
#   4. save, stop, strip the datapack (so the shipped world is static)
#   5. zip -> showcase/dist/Octaves-of-the-One-Showcase.zip
#
# Requires JDK 25 (Minecraft 26.1 needs Java 25; the mod's dev server runs via
# ./gradlew runServer). Run from the repo root:  ./build_showcase.sh
set -euo pipefail
cd "$(dirname "$0")"

SAVE="Octaves of the One Showcase"
OUT="showcase/dist/Octaves-of-the-One-Showcase.zip"
LOG="$(mktemp)"; FIFO="$(mktemp -u)"

echo "==> generating datapack"
python3 gen_showcase.py

echo "==> preparing fresh server world"
mkdir -p run
echo "eula=true" > run/eula.txt
cat > run/server.properties <<'EOF'
level-type=minecraft:flat
online-mode=false
gamemode=creative
generate-structures=false
spawn-protection=0
view-distance=10
sync-chunk-writes=true
level-name=world
EOF
rm -rf run/world
mkdir -p run/world/datapacks
cp -r showcase/datapack "run/world/datapacks/echoes_showcase"

echo "==> launching dev server (builds via load tag)"
mkfifo "$FIFO"
sleep 100000 > "$FIFO" &              # hold the FIFO open
HOLDER=$!
( ./gradlew runServer --console=plain < "$FIFO" > "$LOG" 2>&1 ) &

echo "==> waiting for the build to finish"
for _ in $(seq 1 180); do
  grep -q "SHOWCASE_BUILD_DONE" "$LOG" && break
  grep -qE "FAILURE: Build failed|A mod crashed" "$LOG" && { echo "server failed:"; tail -20 "$LOG"; exit 1; }
  sleep 5
done
if grep -qiE "Could not set|Not a string|Whilst parsing|is not a list" "$LOG"; then
  echo "!! command errors during build:"; grep -iE "Could not set|Not a string|Whilst parsing|is not a list" "$LOG" | head
fi

echo "==> saving and stopping"
printf 'save-all flush\n' > "$FIFO"; sleep 5
printf 'stop\n' > "$FIFO"
for _ in $(seq 1 24); do grep -q "All dimensions are saved" "$LOG" && break; sleep 1; done
kill "$HOLDER" 2>/dev/null || true

echo "==> packaging save"
rm -rf "/tmp/$SAVE"; cp -r run/world "/tmp/$SAVE"
rm -rf "/tmp/$SAVE/datapacks" "/tmp/$SAVE/session.lock" "/tmp/$SAVE/level.dat_old"
mkdir -p showcase/dist
rm -f "$OUT"
( cd /tmp && zip -rq "$OLDPWD/$OUT" "$SAVE" )
echo "==> done: $OUT"
unzip -l "$OUT" | tail -1
