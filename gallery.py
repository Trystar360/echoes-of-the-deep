#!/usr/bin/env python3
"""Render committed texture-gallery images for the README from the actual PNGs.
Outputs docs/images/textures.png (every sprite) and docs/images/machines.png
(directional front | side | top for the machine blocks)."""
import struct, zlib
OUT="src/main/resources/assets/echoes/textures"
def read_png(path):
    d=open(path,"rb").read(); i=8; w=h=0; idat=b""
    while i<len(d):
        ln=struct.unpack(">I",d[i:i+4])[0]; typ=d[i+4:i+8]; data=d[i+8:i+8+ln]
        if typ==b"IHDR": w,h=struct.unpack(">II",data[:8])
        elif typ==b"IDAT": idat+=data
        elif typ==b"IEND": break
        i+=12+ln
    raw=zlib.decompress(idat); px=[(0,0,0,0)]*(w*h); pos=0
    for y in range(h):
        pos+=1
        for x in range(w):
            px[y*w+x]=(raw[pos],raw[pos+1],raw[pos+2],raw[pos+3]); pos+=4
    return w,h,px
def write_png(path,w,h,px):
    raw=bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            r,g,b,a=px[y*w+x]; raw+=bytes((r,g,b,a))
    def ch(t,dd): return struct.pack(">I",len(dd))+t+dd+struct.pack(">I",zlib.crc32(t+dd)&0xffffffff)
    open(path,"wb").write(b"\x89PNG\r\n\x1a\n"+ch(b"IHDR",struct.pack(">IIBBBBB",w,h,8,6,0,0,0))+ch(b"IDAT",zlib.compress(bytes(raw),9))+ch(b"IEND",b""))
def frame0(name, kind):  # top 16x16 frame of a (possibly animated) texture
    w,h,px=read_png(f"{OUT}/{kind}/{name}.png")
    return [[px[y*w+x] for x in range(16)] for y in range(16)]

def checker(x,y,a=(150,150,156),b=(128,128,134)):
    return (*(a if ((x//24)+(y//24))%2==0 else b),255)
def blit(img,W,ox,oy,tex,sc):
    for y in range(16):
        for x in range(16):
            c=tex[y][x]
            if c[3]==0: continue
            for sy in range(sc):
                for sx in range(sc):
                    img[(oy+y*sc+sy)*W+(ox+x*sc+sx)]=(c[0],c[1],c[2],255)

# --- full sprite sheet ---
blocks=["echocite_ore","deepslate_echocite_ore","drumstone_ore","silentite_ore",
        "stillness_core","resonator","tuning_conduit","dense_conduit","resonance_capacitor",
        "crusher","attunement_furnace","radiator","warmth_radiator","polarity_field","balancer","resonant_relay","resonant_amplifier",
        "harmonic_filter","resonant_splitter","echo_repeater","conduit_coupler",
        "resonant_chest","note_relay","greater_accumulator","octave_coil","octave_conduit",
        "storm_caller",
        "lumewood_log","lumewood_log_top","lumewood_planks","lumewood_leaves",
        "lumewood_sapling","lumewood_trapdoor","lumebloom","lume_lantern",
        "verdant_loam","echocite_bricks"]
items=["raw_echocite","echocite_dust","echo_ingot","dull_ingot","resonant_slag",
       "drumstone_shard","drum_core","silentite_crystal","echo_dust",
       "octave_seed","radiant_dust","radiant_ingot",
       "frequency_tuner","channel_atlas","resonance_meter","resonance_thrusters",
       "resonant_pickaxe","resonant_axe","resonant_shovel","resonant_sword","resonant_hoe"]
tiles=[("block",b) for b in blocks]+[("item",i) for i in items]
SC=10; TILE=16*SC; GAP=10; COLS=6
ROWS=(len(tiles)+COLS-1)//COLS
W=COLS*TILE+(COLS+1)*GAP; H=ROWS*TILE+(ROWS+1)*GAP
img=[checker(x,y) for y in range(H) for x in range(W)]
for idx,(kind,name) in enumerate(tiles):
    cx=idx%COLS; cy=idx//COLS
    blit(img,W,GAP+cx*(TILE+GAP),GAP+cy*(TILE+GAP),frame0(name,kind),SC)
write_png("docs/images/textures.png",W,H,img)

# --- directional machine faces: front | side | top ---
side=frame0("device_side","block"); top=frame0("device_top","block")
machines=["stillness_core","resonator","radiator","warmth_radiator","polarity_field","balancer","attunement_furnace","crusher","resonant_relay",
          "echo_repeater","conduit_coupler","resonant_chest","note_relay",
          "resonant_amplifier","harmonic_filter","resonant_splitter"]
SC2=8; T2=16*SC2; G=10; C3=3
W2=GAP+C3*(T2+GAP); H2=GAP+len(machines)*(T2+GAP)
img2=[checker(x,y,(60,64,70),(50,54,60)) for y in range(H2) for x in range(W2)]
for r,name in enumerate(machines):
    oy=GAP+r*(T2+GAP)
    blit(img2,W2,GAP,oy,frame0(name,"block"),SC2)
    blit(img2,W2,GAP+(T2+GAP),oy,side,SC2)
    blit(img2,W2,GAP+2*(T2+GAP),oy,top,SC2)
write_png("docs/images/machines.png",W2,H2,img2)
print("wrote docs/images/textures.png", f"{W}x{H}", "and docs/images/machines.png", f"{W2}x{H2}")
