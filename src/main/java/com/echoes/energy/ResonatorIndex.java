package com.echoes.energy;

import com.echoes.block.entity.ResonatorBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A per-world, per-chunk index of every loaded Resonator's position. Ambient sound
 * capture ({@link ResonanceEvents}) fires often — it's the hottest path in the mod
 * — so it looks up candidates here instead of walking every block entity (chests,
 * furnaces, machines, …) of every chunk in range just to find the ones that happen
 * to be Resonators. Registered from {@link ResonatorBlockEntity#onLoad()},
 * unregistered from {@code setRemoved()}.
 *
 * <p>Chunk unload is deliberately <b>not</b> hooked here the way the energy/wireless
 * networks do: unlike those, a stale entry here is just a {@link BlockPos} (no live
 * object reference to hold or write through), so the worst case is a position that
 * sits unqueried until its chunk reloads or the block is actually broken — bounded,
 * cheap, and self-resolving, not a correctness risk.
 */
public final class ResonatorIndex {
    private ResonatorIndex() {}

    private static final Map<ServerLevel, Map<ChunkPos, Set<BlockPos>>> BY_WORLD = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> BY_WORLD.clear());
    }

    public static void register(ServerLevel world, BlockPos pos) {
        BY_WORLD.computeIfAbsent(world, w -> new HashMap<>())
                .computeIfAbsent(chunkOf(pos), c -> new HashSet<>())
                .add(pos.immutable());
    }

    public static void unregister(ServerLevel world, BlockPos pos) {
        Map<ChunkPos, Set<BlockPos>> chunks = BY_WORLD.get(world);
        if (chunks == null) return;
        ChunkPos key = chunkOf(pos);
        Set<BlockPos> set = chunks.get(key);
        if (set == null) return;
        set.remove(pos);
        if (set.isEmpty()) chunks.remove(key);
    }

    /** Nearest loaded Resonator within {@code radius} of {@code pos}, or null. */
    public static ResonatorBlockEntity nearest(ServerLevel world, Vec3 pos, int radius) {
        Map<ChunkPos, Set<BlockPos>> chunks = BY_WORLD.get(world);
        if (chunks == null || chunks.isEmpty()) return null;

        BlockPos origin = BlockPos.containing(pos);
        double r2 = (double) radius * radius;
        int minCx = (origin.getX() - radius) >> 4, maxCx = (origin.getX() + radius) >> 4;
        int minCz = (origin.getZ() - radius) >> 4, maxCz = (origin.getZ() + radius) >> 4;

        ResonatorBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                Set<BlockPos> candidates = chunks.get(new ChunkPos(cx, cz));
                if (candidates == null) continue;
                for (BlockPos p : candidates) {
                    double d = pos.distanceToSqr(Vec3.atCenterOf(p));
                    if (d > r2 || d >= bestDist) continue;
                    // hasChunk first, same as ResonanceNetwork#nodeAt: never force-load a
                    // chunk just to check a candidate (also gracefully skips stale entries
                    // for positions whose chunk is currently unloaded).
                    if (!world.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
                    if (world.getBlockEntity(p) instanceof ResonatorBlockEntity res) {
                        best = res;
                        bestDist = d;
                    }
                }
            }
        }
        return best;
    }

    private static ChunkPos chunkOf(BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
