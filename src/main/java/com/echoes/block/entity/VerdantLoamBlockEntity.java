package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Living garden soil. Quietly accelerates the growth of plants around and above it
 * — a passive bonemeal aura — so a tended bed of Verdant Loam keeps itself thriving.
 * Radius and the interval between growth pulses are configurable via the Tuner.
 */
public class VerdantLoamBlockEntity extends BlockEntity implements Configurable {

    /** Verdant Loam exposes redstone behaviour, an aura radius and a pulse interval. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone()
            .tuning("config.echoes.tuning.radius", 1, 5, 1, 2)
            .tuning("config.echoes.tuning.interval", 5, 100, 5, 20)
            .build();

    private final BlockConfig config = new BlockConfig();
    private int timer;

    public VerdantLoamBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VERDANT_LOAM, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, VerdantLoamBlockEntity be) {
        if (!(world instanceof ServerLevel sw)) return;
        if (!be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        if (++be.timer < be.config.tuningB()) return;
        be.timer = 0;

        int r = be.config.tuningA();
        RandomSource rng = sw.getRandom();
        // A handful of tries to find a growable plant in the volume above the soil.
        for (int i = 0; i < 6; i++) {
            BlockPos p = pos.add(rng.nextInt(r * 2 + 1) - r,
                    1 + rng.nextInt(2),
                    rng.nextInt(r * 2 + 1) - r);
            BlockState s = sw.getBlockState(p);
            if (s.getBlock() instanceof BonemealableBlock f
                    && f.isFertilizable(sw, p, s) && f.canGrow(sw, rng, p, s)) {
                f.grow(sw, rng, p, s);
                sw.syncWorldEvent(2005, p, 0); // bonemeal particles
                be.markDirty();
                return;
            }
        }
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
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
