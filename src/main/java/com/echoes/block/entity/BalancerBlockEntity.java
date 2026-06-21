package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Rhythmic balanced interchange made a block: every few ticks it nudges all storage
 * on the adjacent network toward the same fill ratio, so the grid breathes evenly
 * and no Resonance Cell hoards. Place it against a Wave Conduit.
 */
public class BalancerBlockEntity extends BlockEntity implements Configurable {
    private static final int INTERVAL = 10;

    /** Balancers expose redstone behaviour, per-face I/O and an adjustable interchange rate. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.rate", 500, 8000, 500, 2000)
            .build();

    private final BlockConfig config = new BlockConfig();
    private int timer;

    public BalancerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BALANCER, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, BalancerBlockEntity be) {
        if (!(world instanceof ServerLevel sw)) return;
        if (!be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        ResonanceNetworkManager.get(sw).balanceAround(pos, be.config.tuningA());
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        config.readNbt(nbt);
    }
}
