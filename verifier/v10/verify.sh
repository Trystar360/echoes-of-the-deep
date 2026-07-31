#!/bin/bash
# Verifier v10 — Echoes of the Deep "fully loaded" acceptance checks
# Usage: verifier/v10/verify.sh ; exit 0 = PASS
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
python3 verifier/v10/check_assets.py
check "asset integrity (models->textures, lang keys)" $?

# 4. Texture resolution: all block/item textures are 32x32 (or 32xn animated strips)
python3 verifier/v10/check_textures32.py
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

grep -q '^mod_version=0.3.0' gradle.properties && t13=0 || t13=1; check "mod version bumped (0.3.0)" $t13
for a in fabricator echo_bloom echo_essence; do [ -f "src/main/resources/data/echoes/advancement/great_work/$a.json" ] || exit 1; done; check "new advancements present" $?
for k in fabricator echo_bloom echo_essence; do grep -q "advancement.echoes.$k.title" src/main/resources/assets/echoes/lang/en_us.json || exit 1; done; check "advancement lang keys" $?
python3 -c "import json,glob,sys; [json.load(open(f)) for f in glob.glob('src/main/resources/data/echoes/advancement/**/*.json',recursive=True)]"; check "all advancement JSON valid" $?
ls build/libs/echoes-of-the-deep-0.3.0.jar >/dev/null 2>&1; check "0.3.0 jar built" $?

grep -rq "RadiantBloomBlock" src/main/java && t14=0 || t14=1; check "tier-2 resource crop (Radiant Bloom)" $t14
grep -q "RADIANT_BLOOM_SEEDS" src/main/java/com/echoes/registry/ModItems.java && t15=0 || t15=1; check "radiant bloom seeds registered" $t15
[ -f src/main/resources/data/echoes/loot_table/blocks/radiant_bloom.json ] && [ -f src/main/resources/data/echoes/recipe/radiant_ingot_from_essence.json ] && [ -f src/main/resources/data/echoes/recipe/radiant_bloom_seeds.json ]; check "radiant bloom loot+recipes" $?
grep -q '"echoes:radiant_essence": 8192' src/main/resources/data/echoes/light_values.json && t16=0 || t16=1; check "radiant essence in Bound-Light table (8192)" $t16
[ -f src/main/resources/data/echoes/advancement/great_work/radiant_essence.json ] && grep -q "advancement.echoes.radiant_essence.title" src/main/resources/assets/echoes/lang/en_us.json; check "radiant essence advancement+lang" $?
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/block/RadiantBloomBlock.class" && t17=0 && break; t17=1; done; check "radiant bloom in shipped jar" $t17

[ -f src/test/java/com/echoes/data/ResourcesDataTest.java ] && t18=0 || t18=1; check "data-integrity test suite present" $t18
ls build/test-results/test/TEST-com.echoes.data.ResourcesDataTest.xml >/dev/null 2>&1 && ! grep -q 'failures="[1-9]\|errors="[1-9]' build/test-results/test/TEST-com.echoes.data.ResourcesDataTest.xml; check "data-integrity tests green" $?

[ -f src/main/java/com/echoes/item/EncodedPatternItem.java ] && t19=0 || t19=1; check "AE2-style encoded pattern item" $t19
grep -q "BLANK_PATTERN" src/main/java/com/echoes/registry/ModItems.java && grep -q "ENCODED_PATTERN" src/main/java/com/echoes/registry/ModItems.java && t20=0 || t20=1; check "pattern cards registered" $t20
grep -q "restock" src/main/java/com/echoes/block/entity/FabricatorBlockEntity.java && grep -q "loadPattern" src/main/java/com/echoes/block/entity/FabricatorBlockEntity.java && t21=0 || t21=1; check "fabricator template restock" $t21
grep -q "useItemOn" src/main/java/com/echoes/block/FabricatorBlock.java && t22=0 || t22=1; check "pattern card interaction" $t22
[ -f src/main/resources/data/echoes/recipe/blank_pattern.json ] && grep -q "item.echoes.encoded_pattern" src/main/resources/assets/echoes/lang/en_us.json && grep -q "message.echoes.pattern.loaded" src/main/resources/assets/echoes/lang/en_us.json; check "pattern recipe+lang" $?
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/item/EncodedPatternItem.class" && t23=0 && break; t23=1; done; check "encoded pattern in shipped jar" $t23

grep -rq "AbyssalBloomBlock" src/main/java && t24=0 || t24=1; check "tier-3 resource crop (Abyssal Bloom)" $t24
grep -q "ABYSSAL_BLOOM_SEEDS" src/main/java/com/echoes/registry/ModItems.java && t25=0 || t25=1; check "abyssal bloom seeds registered" $t25
[ -f src/main/resources/data/echoes/loot_table/blocks/abyssal_bloom.json ] && [ -f src/main/resources/data/echoes/recipe/abyssal_bloom_seeds.json ] && [ -f src/main/resources/data/echoes/recipe/netherite_scrap_from_essence.json ]; check "abyssal bloom loot+recipes" $?
grep -q '"echoes:abyssal_essence": 16384' src/main/resources/data/echoes/light_values.json && t26=0 || t26=1; check "abyssal essence in Bound-Light table (16384)" $t26
grep -q "getRawBrightness" src/main/java/com/echoes/block/AbyssalBloomBlock.java && t27=0 || t27=1; check "darkness-gated growth" $t27
[ -f src/main/resources/data/echoes/advancement/great_work/abyssal_essence.json ] && grep -q "advancement.echoes.abyssal_essence.title" src/main/resources/assets/echoes/lang/en_us.json; check "abyssal essence advancement+lang" $?
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/block/AbyssalBloomBlock.class" && t28=0 && break; t28=1; done; check "abyssal bloom in shipped jar" $t28

[ -f src/main/java/com/echoes/wireless/RedstoneGate.java ] && t29=0 || t29=1; check "TD-style redstone gate logic (RedstoneGate)" $t29
grep -q "RedstoneMode redstoneMode" src/main/java/com/echoes/wireless/WirelessDevice.java && grep -q "RedstoneMode redstoneMode" src/main/java/com/echoes/block/entity/AbstractChannelDeviceBlockEntity.java && t30=0 || t30=1; check "wireless devices expose redstone mode" $t30
grep -q "RedstoneGate.allowed" src/main/java/com/echoes/wireless/WirelessNetworkManager.java && t31=0 || t31=1; check "network manager enforces redstone gating" $t31
ls build/test-results/test/TEST-com.echoes.data.RedstoneGateTest.xml >/dev/null 2>&1 && ! grep -q 'failures="[1-9]\|errors="[1-9]' build/test-results/test/TEST-com.echoes.data.RedstoneGateTest.xml; check "redstone gate tests green" $?
grep -q "Signal Relay bus" docs/mechanics-guide.md && t32=0 || t32=1; check "docs cover wireless redstone gating" $t32
for j in build/libs/echoes-of-the-deep-[0-9]*.jar; do unzip -l "$j" 2>/dev/null | grep -q "com/echoes/wireless/RedstoneGate.class" && t33=0 && break; t33=1; done; check "redstone gate in shipped jar" $t33

exit $fail
