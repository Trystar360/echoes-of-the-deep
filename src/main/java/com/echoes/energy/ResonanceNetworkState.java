package com.echoes.energy;

import com.echoes.EchoesMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Disk-backed snapshot of the wired Resonance grid's topology so networks survive a
 * restart. We persist only the conduit positions per network (plus the id counter);
 * the node lists are transient and re-scanned from the world on the next tick.
 * {@link ResonanceNetworkManager} owns one per {@link
 * net.minecraft.server.level.ServerLevel}.
 *
 * <p>26.1: {@link SavedData} is codec-serialized via a {@link SavedDataType}.
 */
public class ResonanceNetworkState extends SavedData {

    public final Map<Integer, Set<BlockPos>> networks = new HashMap<>();
    public int nextId = 1;

    public static final Codec<ResonanceNetworkState> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.INT.fieldOf("nextId").orElse(1).forGetter(s -> s.nextId),
            Codec.unboundedMap(Codec.STRING, BlockPos.CODEC.listOf()).fieldOf("networks").orElse(Map.of())
                    .forGetter(s -> s.networks.entrySet().stream().collect(Collectors.toMap(
                            e -> String.valueOf(e.getKey()), e -> List.copyOf(e.getValue()))))
    ).apply(in, (nextId, map) -> {
        ResonanceNetworkState s = new ResonanceNetworkState();
        s.nextId = Math.max(1, nextId);
        map.forEach((k, v) -> {
            if (!v.isEmpty()) s.networks.put(Integer.parseInt(k), new HashSet<>(v));
        });
        return s;
    }));

    public static final SavedDataType<ResonanceNetworkState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "resonance_networks"),
            ResonanceNetworkState::new, CODEC, DataFixTypes.LEVEL);
}
