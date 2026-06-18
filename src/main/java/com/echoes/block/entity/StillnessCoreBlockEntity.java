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
import net.minecraft.world.World;

/**
 * The still magnetic centre of zero — Russell's fulcrum from which all motion
 * springs. It draws a steady trickle of Light out of the stillness and offers it
 * to the grid (PROVIDER + STORAGE), giving a baseline generator that doesn't depend
 * on ambient capture. Slow by design: the still point gives only as it is balanced.
 */
public class StillnessCoreBlockEntity extends BlockEntity implements ResonanceNode {
    public static final long CAPACITY = 50_000;
    private static final int GEN_PER_TICK = 4;

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);

    public StillnessCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STILLNESS_CORE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StillnessCoreBlockEntity be) {
        if (world.isClient || be.storage.isFull()) return;
        be.storage.absorb(GEN_PER_TICK);
        be.markDirty();
    }

    public ResonanceStorage storage() { return storage; }

    @Override public int roleMask() { return NodeRole.of(NodeRole.PROVIDER, NodeRole.STORAGE); }
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
