# === Octaves of the One — the Great Work showcase ===
gamerule advance_time false
gamerule advance_weather false
gamerule spawn_mobs false
gamerule block_drops false
gamerule keep_inventory true
weather clear 1000000
time set 2000
difficulty peaceful
function echoes_showcase:build/shell
setblock -6 -59 -4 minecraft:lectern
setblock -6 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"OCTAVES",color:"aqua"},{text:"OF THE ONE",color:"aqua"},{text:"The Great Work",color:"gold"},{text:"walk east ->",color:"gray"}],has_glowing_text:1b,color:"white"}}
setblock -4 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"How to read",color:"gold"},{text:"this hall",color:"gold"},{text:"name + lore",color:"gray"},{text:"on each sign",color:"gray"}],has_glowing_text:1b,color:"white"}}
setworldspawn -6 -60 0
spawnpoint @a -6 -60 0
setblock 0 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"I · Refine & Generate",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 3 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 3 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:raw_echocite",count:1}}
setblock 3 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Raw Echocite",color:"aqua"},{text:"Smelt into an",color:"white"},{text:"Echo Ingot, or",color:"white"},{text:"crush for",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 6 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 6 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:echocite_dust",count:1}}
setblock 6 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Echocite Dust",color:"aqua"},{text:"Smelt into an",color:"white"},{text:"Echo Ingot.",color:"white"},{text:"Crushed from",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 9 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 9 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:echo_ingot",count:1}}
setblock 9 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Echo Ingot",color:"aqua"},{text:"The core",color:"white"},{text:"crafting",color:"white"},{text:"material of the",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 12 -60 -4 echoes:echocite_brick_slab
setblock 12 -59 -4 echoes:resonant_coil
setblock 12 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Coil",color:"aqua"},{text:"winds ambient",color:"white"},{text:"sound into",color:"white"},{text:"stored Light.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 15 -60 -4 echoes:echocite_brick_slab
setblock 15 -59 -4 echoes:stillness_core
setblock 15 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Stillness Core",color:"aqua"},{text:"The still",color:"white"},{text:"centre of zero:",color:"white"},{text:"slowly",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 18 -60 -4 echoes:echocite_brick_slab
setblock 18 -59 -4 echoes:octave_coil
setblock 18 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Coil",color:"aqua"},{text:"Higher-octave",color:"white"},{text:"generator - a",color:"white"},{text:"strong baseline",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 21 -60 -4 echoes:echocite_brick_slab
setblock 21 -59 -4 echoes:storm_caller
setblock 21 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Storm Caller",color:"aqua"},{text:"A conductive",color:"white"},{text:"spire - during",color:"white"},{text:"thunderstorms",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 27 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"II · The Wired Grid",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 30 -60 -4 echoes:echocite_brick_slab
setblock 30 -59 -4 echoes:wave_conduit
setblock 30 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Conduit",color:"aqua"},{text:"carries Light",color:"white"},{text:"between devices",color:"white"},{text:"(1,000/t).",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 33 -60 -4 echoes:echocite_brick_slab
setblock 33 -59 -4 echoes:dense_wave_conduit
setblock 33 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Dense Wave Conduit",color:"aqua"},{text:"×16 throughput",color:"white"},{text:"(16,000/t).",color:"white"},{text:"",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 36 -60 -4 echoes:echocite_brick_slab
setblock 36 -59 -4 echoes:octave_conduit
setblock 36 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Conduit",color:"aqua"},{text:"Highest-octave",color:"white"},{text:"carrier -",color:"white"},{text:"64,000 Light/t",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 39 -60 -4 echoes:echocite_brick_slab
setblock 39 -59 -4 echoes:resonance_cell
setblock 39 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonance Cell",color:"aqua"},{text:"banks up to",color:"white"},{text:"250,000 Light.",color:"white"},{text:"",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 42 -60 -4 echoes:echocite_brick_slab
setblock 42 -59 -4 echoes:greater_resonance_cell
setblock 42 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Greater Resonance Cell",color:"aqua"},{text:"High-octave",color:"white"},{text:"bank - stores",color:"white"},{text:"up to 2,000,000",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 45 -60 -4 echoes:echocite_brick_slab
setblock 45 -59 -4 echoes:compressor
setblock 45 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Compressor",color:"aqua"},{text:"crushes ore",color:"white"},{text:"into doubled",color:"white"},{text:"dust using",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 48 -60 -4 echoes:echocite_brick_slab
setblock 48 -59 -4 echoes:transmuter
setblock 48 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Transmuter",color:"aqua"},{text:"smelts any",color:"white"},{text:"furnace recipe",color:"white"},{text:"with Light, no",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 51 -60 -4 echoes:echocite_brick_slab
setblock 51 -59 -4 echoes:balancer
setblock 51 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Balancer",color:"aqua"},{text:"Evens Light",color:"white"},{text:"across all",color:"white"},{text:"storage on its",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 57 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"III · Radiation",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 60 -60 -4 echoes:echocite_brick_slab
setblock 60 -59 -4 echoes:growth_radiator
setblock 60 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Growth Radiator",color:"aqua"},{text:"Radiates Light",color:"white"},{text:"as life - grows",color:"white"},{text:"nearby crops",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 63 -60 -4 echoes:echocite_brick_slab
setblock 63 -59 -4 echoes:warmth_radiator
setblock 63 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Warmth Radiator",color:"aqua"},{text:"Radiates heat -",color:"white"},{text:"cooks dropped",color:"white"},{text:"items and melts",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 66 -60 -4 echoes:echocite_brick_slab
setblock 66 -59 -4 echoes:polarity_field
setblock 66 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Polarity Field",color:"aqua"},{text:"Two poles in",color:"white"},{text:"one: Attract",color:"white"},{text:"pulls items in,",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 72 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"IV · Wireless Transport",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 75 -60 -4 echoes:echocite_brick_slab
setblock 75 -59 -4 echoes:wave_relay
setblock 75 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Relay",color:"aqua"},{text:"beams items,",color:"white"},{text:"fluids, and",color:"white"},{text:"Light over a",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 78 -60 -4 echoes:echocite_brick_slab
setblock 78 -59 -4 echoes:wave_amplifier
setblock 78 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Amplifier",color:"aqua"},{text:"doubles a",color:"white"},{text:"channel's",color:"white"},{text:"throughput (×16",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 81 -60 -4 echoes:echocite_brick_slab
setblock 81 -59 -4 echoes:wave_filter
setblock 81 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Filter",color:"aqua"},{text:"restricts a",color:"white"},{text:"channel's items",color:"white"},{text:"to a whitelist.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 84 -60 -4 echoes:echocite_brick_slab
setblock 84 -59 -4 echoes:wave_splitter
setblock 84 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Splitter",color:"aqua"},{text:"even",color:"white"},{text:"round-robin vs.",color:"white"},{text:"fill-first",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 87 -60 -4 echoes:echocite_brick_slab
setblock 87 -59 -4 echoes:wave_repeater
setblock 87 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Repeater",color:"aqua"},{text:"extends a",color:"white"},{text:"channel across",color:"white"},{text:"dimensions.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 90 -60 -4 echoes:echocite_brick_slab
setblock 90 -59 -4 echoes:wave_coupler
setblock 90 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Coupler",color:"aqua"},{text:"bridges the",color:"white"},{text:"wired grid to a",color:"white"},{text:"wireless",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 93 -60 -4 echoes:echocite_brick_slab
setblock 93 -59 -4 echoes:wave_chest
setblock 93 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Chest",color:"aqua"},{text:"27-slot storage",color:"white"},{text:"that lives on a",color:"white"},{text:"channel.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 96 -60 -4 echoes:echocite_brick_slab
setblock 96 -59 -4 echoes:signal_relay
setblock 96 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Signal Relay",color:"aqua"},{text:"broadcasts",color:"white"},{text:"redstone over a",color:"white"},{text:"channel.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 99 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 99 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:wave_tuner",count:1}}
setblock 99 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Tuner",color:"aqua"},{text:"copy/paste a",color:"white"},{text:"channel;",color:"white"},{text:"sneak-use opens",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 102 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 102 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:wave_atlas",count:1}}
setblock 102 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Wave Atlas",color:"aqua"},{text:"lists the",color:"white"},{text:"devices on each",color:"white"},{text:"active channel.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 108 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"V · The Octave Climb",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 111 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 111 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:silentite_crystal",count:1}}
setblock 111 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Silentite Crystal",color:"aqua"},{text:"Silent crystal",color:"white"},{text:"of the Deep",color:"white"},{text:"Dark - for the",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 114 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 114 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:drum_core",count:1}}
setblock 114 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Drum Core",color:"aqua"},{text:"An alternate",color:"white"},{text:"Coil membrane;",color:"white"},{text:"also powers the",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 117 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 117 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_seed",count:1}}
setblock 117 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Seed",color:"aqua"},{text:"The octave's",color:"white"},{text:"rest point -",color:"white"},{text:"catalyst that",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 120 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 120 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:radiant_dust",count:1}}
setblock 120 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Radiant Dust",color:"aqua"},{text:"Charged dust -",color:"white"},{text:"smelt or blast",color:"white"},{text:"into a Radiant",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 123 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 123 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:radiant_ingot",count:1}}
setblock 123 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Radiant Ingot",color:"aqua"},{text:"Block-of-light",color:"white"},{text:"tier material;",color:"white"},{text:"builds the",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 129 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"VI · Transmutation",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 132 -60 -4 echoes:echocite_brick_slab
setblock 132 -59 -4 echoes:transmutation_table
setblock 132 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Transmutation Table",color:"aqua"},{text:"Your",color:"white"},{text:"Bound-Light",color:"white"},{text:"terminal:",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 135 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 135 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:transmutation_tablet",count:1}}
setblock 135 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Transmutation Tablet",color:"aqua"},{text:"Portable",color:"white"},{text:"transmutation",color:"white"},{text:"terminal -",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 138 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 138 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:light_mote",count:1}}
setblock 138 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Light Mote",color:"aqua"},{text:"Raw Light - the",color:"white"},{text:"universal One",color:"white"},{text:"(64 Light",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 141 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 141 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:tonic_mote",count:1}}
setblock 141 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Tonic Mote",color:"aqua"},{text:"Light wound an",color:"white"},{text:"octave - the",color:"white"},{text:"tonic (256",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 144 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 144 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:mediant_mote",count:1}}
setblock 144 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Mediant Mote",color:"aqua"},{text:"The chord's",color:"white"},{text:"middle tone",color:"white"},{text:"(1,024 Light",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 147 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 147 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:dominant_mote",count:1}}
setblock 147 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Dominant Mote",color:"aqua"},{text:"The strong",color:"white"},{text:"tone, nearing",color:"white"},{text:"the crest",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 150 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 150 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:harmonic_mote",count:1}}
setblock 150 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Harmonic Mote",color:"aqua"},{text:"The resolved",color:"white"},{text:"crest - balance",color:"white"},{text:"(16,384 Light",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 153 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 153 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_1",count:1}}
setblock 153 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star I",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 156 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 156 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_2",count:1}}
setblock 156 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star II",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 159 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 159 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_3",count:1}}
setblock 159 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star III",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 162 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 162 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_4",count:1}}
setblock 162 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star IV",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 165 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 165 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_5",count:1}}
setblock 165 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star V",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 168 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 168 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:octave_star_6",count:1}}
setblock 168 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Octave Star VI",color:"aqua"},{text:"Portable",color:"white"},{text:"Bound-Light",color:"white"},{text:"battery",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 174 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"VII · The Octave Grove",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 177 -60 -4 echoes:echocite_brick_slab
setblock 177 -59 -4 echoes:lumewood_log
setblock 177 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lumewood Log",color:"aqua"},{text:"A glowing log",color:"white"},{text:"from the Octave",color:"white"},{text:"Grove.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 180 -60 -4 echoes:echocite_brick_slab
setblock 180 -59 -4 echoes:lumewood_planks
setblock 180 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lumewood Planks",color:"aqua"},{text:"Luminous planks",color:"white"},{text:"- the base of",color:"white"},{text:"the Lumewood",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 183 -60 -4 echoes:echocite_brick_slab
setblock 183 -59 -4 echoes:lumewood_stairs
setblock 183 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lumewood Stairs",color:"aqua"},{text:"Glowing",color:"white"},{text:"Lumewood",color:"white"},{text:"stairs.",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 186 -60 -4 echoes:echocite_brick_slab
setblock 186 -59 -4 echoes:lumewood_slab
setblock 186 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lumewood Slab",color:"aqua"},{text:"Glowing",color:"white"},{text:"Lumewood slab.",color:"white"},{text:"",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 189 -60 -4 echoes:echocite_brick_slab
setblock 189 -59 -4 echoes:lume_lantern
setblock 189 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lume Lantern",color:"aqua"},{text:"A full-bright",color:"white"},{text:"decorative",color:"white"},{text:"block of woven",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 192 -60 -4 echoes:echocite_brick_slab
setblock 192 -59 -4 echoes:verdant_loam
setblock 192 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Verdant Loam",color:"aqua"},{text:"Living soil -",color:"white"},{text:"pulses Light",color:"white"},{text:"upward to grow",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 195 -60 -4 echoes:echocite_brick_slab
setblock 195 -59 -4 echoes:echocite_bricks
setblock 195 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Echocite Bricks",color:"aqua"},{text:"Luminous",color:"white"},{text:"masonry crafted",color:"white"},{text:"from Echocite",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 198 -60 -4 echoes:echocite_brick_slab
setblock 198 -59 -4 echoes:lumebloom
setblock 198 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Lumebloom",color:"aqua"},{text:"A glowing",color:"white"},{text:"flower - grants",color:"white"},{text:"Glowing when",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 204 -57 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"= WING =",color:"gold"},{text:"VIII · Flight & Gear",color:"aqua"},{text:""},{text:""}],has_glowing_text:1b,color:"white"}}
setblock 207 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 207 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_pickaxe",count:1}}
setblock 207 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Pickaxe",color:"aqua"},{text:"Resonant tool -",color:"white"},{text:"tuned to the",color:"white"},{text:"octave;",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 210 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 210 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_axe",count:1}}
setblock 210 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Axe",color:"aqua"},{text:"Resonant tool -",color:"white"},{text:"tuned to the",color:"white"},{text:"octave;",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 213 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 213 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_sword",count:1}}
setblock 213 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Sword",color:"aqua"},{text:"Resonant blade",color:"white"},{text:"- tuned to the",color:"white"},{text:"octave; a hefty",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 216 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 216 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_shovel",count:1}}
setblock 216 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Shovel",color:"aqua"},{text:"Resonant tool -",color:"white"},{text:"tuned to the",color:"white"},{text:"octave;",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 219 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 219 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_hoe",count:1}}
setblock 219 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Hoe",color:"aqua"},{text:"Resonant tool -",color:"white"},{text:"tuned to the",color:"white"},{text:"octave;",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 222 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 222 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:resonant_thrusters",count:1}}
setblock 222 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Resonant Thrusters",color:"aqua"},{text:"hold use to fly",color:"white"},{text:"where you look.",color:"white"},{text:"",color:"white"}],has_glowing_text:1b,color:"white"}}
setblock 225 -60 -4 echoes:echocite_brick_slab
summon minecraft:item_frame 225 -59 -4 {Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"echoes:light_meter",count:1}}
setblock 225 -58 -4 minecraft:oak_wall_sign[facing=south]{front_text:{messages:[{text:"Light Meter",color:"aqua"},{text:"reads a",color:"white"},{text:"device's role,",color:"white"},{text:"stored Light,",color:"white"}],has_glowing_text:1b,color:"white"}}
function echoes_showcase:build/demos
tellraw @a {"text":"The Great Work hall is built. Walk east through the wings.","color":"aqua"}
say SHOWCASE_BUILD_DONE
forceload remove all
