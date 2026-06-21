package com.echoes.config;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.Direction;

/**
 * Per-device, NBT-persisted configuration: wireless channel + octave, redstone
 * behaviour, six per-face transfer modes, and up to two block-specific tuning
 * dials. One instance lives on each {@link Configurable} block entity.
 *
 * <p>Side modes are indexed by {@link Direction#getId()} (0=DOWN … 5=EAST).
 */
public final class BlockConfig {
    public static final int OCTAVES = 9;   // Russell's nine octaves of tones
    public static final int CHANNELS = 16; // mirrors WirelessNetworkManager.CHANNELS

    private int channel = 0;
    private int octave = 0;
    private RedstoneMode redstone = RedstoneMode.ALWAYS;
    private final SideMode[] sides = { SideMode.BOTH, SideMode.BOTH, SideMode.BOTH,
                                       SideMode.BOTH, SideMode.BOTH, SideMode.BOTH };
    private int tuningA = 0;
    private int tuningB = 0;

    // --- channel / octave ---
    public int channel() { return channel; }
    public void setChannel(int v) { channel = ((v % CHANNELS) + CHANNELS) % CHANNELS; }
    public int octave() { return octave; }
    public void setOctave(int v) { octave = ((v % OCTAVES) + OCTAVES) % OCTAVES; }

    // --- redstone ---
    public RedstoneMode redstone() { return redstone; }
    public void setRedstone(RedstoneMode m) { redstone = m; }

    // --- sides ---
    public SideMode side(Direction dir) { return sides[dir.get3DDataValue()]; }
    public SideMode side(int id) { return sides[id]; }
    public void setSide(int id, SideMode m) { sides[id] = m; }
    public void cycleSide(int id) { sides[id] = sides[id].next(); }

    // --- tuning ---
    public int tuningA() { return tuningA; }
    public int tuningB() { return tuningB; }
    public void setTuningA(int v) { tuningA = v; }
    public void setTuningB(int v) { tuningB = v; }

    /** Seed unset tuning dials from their spec defaults (call once after construction). */
    public void applyDefaults(ConfigSpec spec) {
        if (spec.tuning(0) != null) tuningA = spec.tuning(0).def();
        if (spec.tuning(1) != null) tuningB = spec.tuning(1).def();
    }

    // --- NBT ---
    public void writeNbt(ValueOutput out) {
        ValueOutput c = out.child("Config");
        c.putInt("channel", channel);
        c.putInt("octave", octave);
        c.putInt("redstone", redstone.id());
        int[] sm = new int[6];
        for (int i = 0; i < 6; i++) sm[i] = sides[i].id();
        c.putIntArray("sides", sm);
        c.putInt("tuningA", tuningA);
        c.putInt("tuningB", tuningB);
    }

    public void readNbt(ValueInput in) {
        var opt = in.child("Config");
        if (opt.isEmpty()) return;
        ValueInput c = opt.get();
        channel = c.getIntOr("channel", 0);
        octave = c.getIntOr("octave", 0);
        redstone = RedstoneMode.byId(c.getIntOr("redstone", 0));
        c.getIntArray("sides").ifPresent(sm -> {
            for (int i = 0; i < 6 && i < sm.length; i++) sides[i] = SideMode.byId(sm[i]);
        });
        tuningA = c.getIntOr("tuningA", 0);
        tuningB = c.getIntOr("tuningB", 0);
    }
}
