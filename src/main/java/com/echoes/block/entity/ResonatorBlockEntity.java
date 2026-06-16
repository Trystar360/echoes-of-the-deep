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
 * Captures ambient RU emitted by Resonance events and offers it to the network.
 * PROVIDER + STORAGE: its buffer doubles as the network's first source.
 */
public class ResonatorBlockEntity extends BlockEntity implements ResonanceNode {
    public static final long DEFAULT_CAPACITY = 10_000;

    private final ResonanceStorage storage = new ResonanceStorage(DEFAULT_CAPACITY);

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONATOR, pos, state);
    }

    /** Called by ResonanceEvents when a nearby event fires. */
    public void absorbAmbient(int ru) {
        storage.absorb(ru);
        markDirty();
    }

    public ResonanceStorage storage() { return storage; }

    // --- ResonanceNode ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.PROVIDER, NodeRole.STORAGE); }
    @Override public long extract(long max, boolean simulate) { return storage.extract(max, simulate); }
    @Override public long insert(long max, boolean simulate) { return storage.insert(max, simulate); }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getPos(); }

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
