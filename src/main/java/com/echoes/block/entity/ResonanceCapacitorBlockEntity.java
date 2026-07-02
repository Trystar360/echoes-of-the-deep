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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

/**
 * Bulk RU storage for the wired grid. A pure STORAGE node with a large buffer, so
 * surplus Resonance can be banked and drawn on later instead of capped at the
 * Resonators' small reserves. Comparator-readable by fill level.
 */
public class ResonanceCapacitorBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final long CAPACITY = 250_000;

    /** Accumulators expose redstone behaviour. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().build();

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);
    private final BlockConfig config = new BlockConfig();

    public ResonanceCapacitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_CAPACITOR, pos, state);
        config.applyDefaults(SPEC);
    }

    public ResonanceStorage storage() { return storage; }

    // --- ResonanceNode (STORAGE) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.STORAGE); }
    @Override public long extract(long max, boolean simulate) { return storage.extract(max, simulate); }
    @Override public long insert(long max, boolean simulate) { return storage.insert(max, simulate); }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return storage.getAmount(); }
    @Override public long capacityRu() { return storage.getCapacity(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        storage.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        storage.readNbt(nbt);
        config.readNbt(nbt);
    }
}
