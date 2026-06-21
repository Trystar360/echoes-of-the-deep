package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The still magnetic centre of zero — Russell's fulcrum from which all motion
 * springs. It draws a steady trickle of Light out of the stillness and offers it
 * to the grid (PROVIDER + STORAGE), giving a baseline generator that doesn't depend
 * on ambient capture. Slow by design: the still point gives only as it is balanced.
 */
public class StillnessCoreBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final long CAPACITY = 50_000;
    private static final int GEN_PER_TICK = 4;

    /** The Core exposes redstone behaviour and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().sides().build();

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);
    private final BlockConfig config = new BlockConfig();

    public StillnessCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STILLNESS_CORE, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, StillnessCoreBlockEntity be) {
        if (world.isClient || be.storage.isFull()) return;
        if (world instanceof ServerLevel sw && !be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
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

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        super.writeNbt(nbt, lookup);
        storage.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void readNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        super.readNbt(nbt, lookup);
        storage.readNbt(nbt);
        config.readNbt(nbt);
    }
}
