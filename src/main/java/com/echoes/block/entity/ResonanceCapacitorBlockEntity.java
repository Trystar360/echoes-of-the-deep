package com.echoes.block.entity;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/**
 * Bulk RU storage for the wired grid. A pure STORAGE node with a large buffer, so
 * surplus Resonance can be banked and drawn on later instead of capped at the
 * Resonators' small reserves. Comparator-readable by fill level.
 */
public class ResonanceCapacitorBlockEntity extends BlockEntity implements ResonanceNode {
    public static final long CAPACITY = 250_000;

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);

    public ResonanceCapacitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_CAPACITOR, pos, state);
    }

    public ResonanceStorage storage() { return storage; }

    // --- ResonanceNode (STORAGE) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.STORAGE); }
    @Override public long extract(long max, boolean simulate) { return storage.extract(max, simulate); }
    @Override public long insert(long max, boolean simulate) { return storage.insert(max, simulate); }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getPos(); }
    @Override public long storedRu() { return storage.getAmount(); }
    @Override public long capacityRu() { return storage.getCapacity(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        storage.writeNbt(nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        storage.readNbt(nbt);
    }
}
