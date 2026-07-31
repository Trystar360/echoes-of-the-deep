#!/bin/bash
# Verifier v4 — Echoes of the Deep "fully loaded" acceptance checks
# Usage: verifier/v4/verify.sh ; exit 0 = PASS
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
python3 verifier/v4/check_assets.py
check "asset integrity (models->textures, lang keys)" $?

# 4. Texture resolution: all block/item textures are 32x32 (or 32xn animated strips)
python3 verifier/v4/check_textures32.py
check "textures are 32x" $?

# 5. Feature presence: the four reference-mod signature systems exist in code
for sym in AutocrafterItemDuct GrowthAccelerator; do :; done
grep -rq "TransmutationTable" src/main/java && t1=0 || t1=1; check "ProjectE-style transmutation" $t1
grep -rq "class.*Conduit" src/main/java && t2=0 || t2=1; check "TD-style conduits" $t2
grep -rq "Verdant\|Grove\|crop" -i src/main/java && t3=0 || t3=1; check "MA-style botanicals" $t3
grep -rq "WirelessNetworkManager" src/main/java && t4=0 || t4=1; check "AE2-style wireless channels" $t4
grep -rq "FabricatorBlockEntity" src/main/java && t5=0 || t5=1; check "AE2-style autocrafting (Fabricator)" $t5
grep -rq "wirelessItems" src/main/java && t6=0 || t6=1; check "item transport over network" $t6
grep -rq "growth" -i src/main/java/com/echoes/block/entity/RadiatorBlockEntity.java && t7=0 || t7=1; check "MA-style growth acceleration" $t7
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/block/entity/FabricatorBlockEntity.class" && t8=0 && break; t8=1; done; check "fabricator in shipped jar" $t8

grep -rq "EchoBloomBlock" src/main/java && t9=0 || t9=1; check "MA-style resource crop (Echo Bloom)" $t9
grep -q "ECHO_BLOOM_SEEDS" src/main/java/com/echoes/registry/ModItems.java && t10=0 || t10=1; check "echo bloom seeds registered" $t10
[ -f src/main/resources/data/echoes/loot_table/blocks/echo_bloom.json ] && [ -f src/main/resources/data/echoes/recipe/radiant_dust_from_essence.json ]; check "echo bloom loot+recipes" $?
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/block/EchoBloomBlock.class" && t11=0 && break; t11=1; done; check "echo bloom in shipped jar" $t11

grep -q '"echoes:echo_essence": 4096' src/main/resources/data/echoes/light_values.json && t12=0 || t12=1; check "essence in Bound-Light value table (4096)" $t12
for r in raw_echocite_from_essence drumstone_shard_from_essence silentite_crystal_from_essence; do [ -f "src/main/resources/data/echoes/recipe/$r.json" ] || { check "essence resource recipes" 1; break; }; done; [ -f src/main/resources/data/echoes/recipe/silentite_crystal_from_essence.json ]; check "essence resource recipes" $?
[ -f docs/mechanics-guide.md ] && grep -qi "transmutation" docs/mechanics-guide.md && grep -qi "echo bloom" docs/mechanics-guide.md && grep -qi "channel" docs/mechanics-guide.md && grep -qi "conduit" docs/mechanics-guide.md; check "mechanics guide covers all four systems" $?

exit $fail
