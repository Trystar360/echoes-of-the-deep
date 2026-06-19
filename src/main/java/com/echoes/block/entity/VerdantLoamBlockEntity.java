package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

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

    public static void tick(World world, BlockPos pos, BlockState state, VerdantLoamBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;
        if (!be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        if (++be.timer < be.config.tuningB()) return;
        be.timer = 0;

        int r = be.config.tuningA();
        Random rng = sw.getRandom();
        // A handful of tries to find a growable plant in the volume above the soil.
        for (int i = 0; i < 6; i++) {
            BlockPos p = pos.add(rng.nextInt(r * 2 + 1) - r,
                    1 + rng.nextInt(2),
                    rng.nextInt(r * 2 + 1) - r);
            BlockState s = sw.getBlockState(p);
            if (s.getBlock() instanceof Fertilizable f
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
