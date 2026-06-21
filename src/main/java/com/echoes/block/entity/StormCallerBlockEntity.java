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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Russell's high-potential discharge wound back into Light. A tall conductive
 * spire that, during a thunderstorm, channels the sky's lightning to itself far
 * more often than nature would and banks the windfall (PROVIDER + STORAGE). The
 * self-struck bolts are cosmetic — the conductive spire grounds them — so it
 * never sets a builder's base alight. Tunable strike frequency via the config GUI.
 */
public class StormCallerBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final long CAPACITY = 400_000;
    /** Light banked per channelled strike (a single natural bolt is ~2,000 via ambient capture). */
    public static final int STRIKE_LIGHT = 40_000;

    /** The spire exposes redstone behaviour, per-face I/O, and a tunable strike rate. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.rate", 1, 4, 1, 2)
            .build();

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);
    private final BlockConfig config = new BlockConfig();
    private int counter = 0;

    public StormCallerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORM_CALLER, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, StormCallerBlockEntity be) {
        if (world.isClient || !(world instanceof ServerLevel sw)) return;
        if (!world.isThundering()) { be.counter = 0; return; }
        if (be.storage.isFull()) return;
        if (!be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        // Must see open sky to draw a bolt down onto the spire.
        if (!world.isSkyVisibleAllowingSea(pos.above())) return;

        // Tuning "rate" 1..4 sets how often the spire calls a strike (~9s .. ~2s).
        int period = 220 - be.config.tuningA() * 50;
        if (++be.counter < period) return;
        be.counter = 0;

        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
        bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        bolt.setCosmetic(true);   // visual + sound only — the spire grounds the charge
        sw.spawnEntity(bolt);

        be.storage.absorb(STRIKE_LIGHT);
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
