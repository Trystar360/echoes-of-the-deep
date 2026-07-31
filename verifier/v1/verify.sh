#!/bin/bash
# Verifier v1 — Echoes of the Deep "fully loaded" acceptance checks
# Usage: verifier/v1/verify.sh ; exit 0 = PASS
cd "$(dirname "$0")/../.." || exit 1
fail=0
check() { # name, condition-result(0/1)
  if [ "$2" -eq 0 ]; then echo "PASS: $1"; else echo "FAIL: $1"; fail=1; fi
}

# 1. Jar built
ls build/libs/echoes-of-the-deep-*.jar >/dev/null 2>&1
check "mod jar exists (build/libs)" $?

# 2. Unit tests passed (JUnit XML present, no failures)
if ls build/test-results/test/*.xml >/dev/null 2>&1; then
  f=$(grep -h 'failures="[1-9]' build/test-results/test/*.xml | wc -l)
  e=$(grep -h 'errors="[1-9]' build/test-results/test/*.xml | wc -l)
  [ "$f" -eq 0 ] && [ "$e" -eq 0 ]; check "unit tests green" $?
else
  check "unit tests green (no results found)" 1
fi

# 3. Texture coverage: every block/item model references an existing texture
python3 verifier/v1/check_assets.py
check "asset integrity (models->textures, lang keys)" $?

# 4. Texture resolution: all block/item textures are 32x32 (or 32xn animated strips)
python3 verifier/v1/check_textures32.py
check "textures are 32x" $?

# 5. Feature presence: the four reference-mod signature systems exist in code
grep -rq "TransmutationTable" src/main/java && t1=0 || t1=1; check "ProjectE-style transmutation" $t1
grep -rq "class.*Conduit" src/main/java && t2=0 || t2=1; check "TD-style conduits" $t2
grep -rq "Verdant\|Grove\|crop" -i src/main/java && t3=0 || t3=1; check "MA-style botanicals" $t3
grep -rq "WirelessNetworkManager" src/main/java && t4=0 || t4=1; check "AE2-style wireless channels" $t4

exit $fail
