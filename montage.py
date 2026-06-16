#!/usr/bin/env python3
"""Read the generated RGBA PNGs and build a zoomed montage for visual review."""
import struct, zlib, os

def read_png(path):
    d = open(path, "rb").read()
    assert d[:8] == b"\x89PNG\r\n\x1a\n"
    i = 8; w = h = 0; idat = b""
    while i < len(d):
        ln = struct.unpack(">I", d[i:i+4])[0]; typ = d[i+4:i+8]; data = d[i+8:i+8+ln]
        if typ == b"IHDR":
            w, h, bd, ct = struct.unpack(">IIBB", data[:10])
        elif typ == b"IDAT":
            idat += data
        elif typ == b"IEND":
            break
        i += 12 + ln
    raw = zlib.decompress(idat)
    px = [(0,0,0,0)]*(w*h); pos = 0
    for y in range(h):
        f = raw[pos]; pos += 1  # filter 0 assumed
        for x in range(w):
            r,g,b,a = raw[pos], raw[pos+1], raw[pos+2], raw[pos+3]; pos += 4
            px[y*w+x] = (r,g,b,a)
    return w, h, px

def write_png(path, w, h, px):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            r,g,b,a = px[y*w+x]; raw += bytes((r,g,b,a))
    def ch(t,dd): return struct.pack(">I",len(dd))+t+dd+struct.pack(">I",zlib.crc32(t+dd)&0xffffffff)
    open(path,"wb").write(b"\x89PNG\r\n\x1a\n"+ch(b"IHDR",struct.pack(">IIBBBBB",w,h,8,6,0,0,0))+ch(b"IDAT",zlib.compress(bytes(raw),9))+ch(b"IEND",b""))

OUT="src/main/resources/assets/echoes/textures"
blocks=["echocite_ore","deepslate_echocite_ore","drumstone_ore","silentite_ore","resonator","tuning_conduit","crusher"]
items=["raw_echocite","echocite_dust","echo_ingot","dull_ingot","resonant_slag","drumstone_shard","drum_core","silentite_crystal","echo_dust"]
tiles=[("block",b) for b in blocks]+[("item",it) for it in items]

SCALE=12; TILE=16*SCALE; GAP=10; COLS=4
ROWS=(len(tiles)+COLS-1)//COLS
MW=COLS*TILE+(COLS+1)*GAP
MH=ROWS*TILE+(ROWS+1)*GAP
def checker(x,y):
    return (150,150,156,255) if ((x//24)+(y//24))%2==0 else (128,128,134,255)
mont=[checker(x,y) for y in range(MH) for x in range(MW)]
def put(mx,my,c):
    if 0<=mx<MW and 0<=my<MH:
        r,g,b,a=c
        if a==0: return
        br,bg,bb,_=mont[my*MW+mx]; af=a/255
        mont[my*MW+mx]=(int(r*af+br*(1-af)),int(g*af+bg*(1-af)),int(b*af+bb*(1-af)),255)
for idx,(d,name) in enumerate(tiles):
    w,h,px=read_png(f"{OUT}/{d}/{name}.png")
    cx=idx%COLS; cy=idx//COLS
    ox=GAP+cx*(TILE+GAP); oy=GAP+cy*(TILE+GAP)
    for y in range(16):
        for x in range(16):
            c=px[y*w+x]
            for sy in range(SCALE):
                for sx in range(SCALE):
                    put(ox+x*SCALE+sx, oy+y*SCALE+sy, c)
write_png("/tmp/echoes_tiles.png", MW, MH, mont)

# GUI at 3x
w,h,px=read_png(f"{OUT}/gui/crusher.png")
S=3; gw,gh=176*S,166*S
g=[(46,46,52,255) for _ in range(gw*gh)]
for y in range(166):
    for x in range(176):
        c=px[y*w+x]
        if c[3]==0: continue
        for sy in range(S):
            for sx in range(S):
                g[(y*S+sy)*gw+(x*S+sx)]=(c[0],c[1],c[2],255)
write_png("/tmp/echoes_gui.png", gw, gh, g)
print(f"tiles montage: {MW}x{MH} -> /tmp/echoes_tiles.png")
print(f"gui preview: {gw}x{gh} -> /tmp/echoes_gui.png")
