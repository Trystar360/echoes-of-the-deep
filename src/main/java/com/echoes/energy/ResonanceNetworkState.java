package com.echoes.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Disk-backed snapshot of the wired Resonance grid's topology so networks survive
 * a restart. We persist only the conduit positions per network (plus the id
 * counter); the node lists are transient and re-scanned from the world on the next
 * tick. {@link ResonanceNetworkManager} owns one of these per {@link
 * net.minecraft.server.level.ServerLevel} and mirrors into it on every topology
 * change.
 */
public class ResonanceNetworkState extends SavedData {
    public static final String KEY = "echoes_resonance_networks";

    public final Map<Integer, Set<BlockPos>> networks = new HashMap<>();
    public int nextId = 1;

    public static final Type<ResonanceNetworkState> TYPE =
            new Type<>(ResonanceNetworkState::new, ResonanceNetworkState::fromNbt, null);

    public static ResonanceNetworkState fromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ResonanceNetworkState s = new ResonanceNetworkState();
        s.nextId = Math.max(1, nbt.getInt("nextId"));
        ListTag list = nbt.getList("networks", Tag.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag n = list.getCompound(i);
            Set<BlockPos> set = new HashSet<>();
            for (long packed : n.getLongArray("pos")) set.add(BlockPos.fromLong(packed));
            if (!set.isEmpty()) s.networks.put(n.getInt("id"), set);
        }
        return s;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putInt("nextId", nextId);
        ListTag list = new ListTag();
        for (Map.Entry<Integer, Set<BlockPos>> e : networks.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            CompoundTag n = new CompoundTag();
            n.putInt("id", e.getKey());
            long[] arr = new long[e.getValue().size()];
            int i = 0;
            for (BlockPos p : e.getValue()) arr[i++] = p.asLong();
            n.putLongArray("pos", arr);
            list.add(n);
        }
        nbt.put("networks", list);
        return nbt;
    }
}
