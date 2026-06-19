package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Rhythmic balanced interchange made a block: every few ticks it nudges all storage
 * on the adjacent network toward the same fill ratio, so the grid breathes evenly
 * and no Accumulator hoards. Place it against a Wave Conduit.
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

    public static void tick(World world, BlockPos pos, BlockState state, BalancerBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;
        if (!be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        ResonanceNetworkManager.get(sw).balanceAround(pos, be.config.tuningA());
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Text configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        config.writeNbt(nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        config.readNbt(nbt);
    }
}
