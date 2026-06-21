package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.registry.ModItems;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The Transmutation Table is now a terminal into the per-player Bound-Light account
 * ({@link com.echoes.transmute.TransmutationState}), so the block itself is stateless.
 * This entity only carries any Light banked by the original block-pool version, so it can
 * be migrated to the opening player (or scattered as Mote coins on break) — never lost.
 */
public class TransmutationTableBlockEntity extends BlockEntity {

    private long legacyLight;

    public TransmutationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSMUTATION_TABLE, pos, state);
    }

    public long legacyLight() { return legacyLight; }

    /** Take and clear the legacy banked Light (for migration to a player's account). */
    public long drainLegacyLight() {
        long l = legacyLight;
        legacyLight = 0;
        setChanged();
        return l;
    }

    /** On break, scatter any legacy banked Light as Mote coins (largest tone first). */
    public void dropBankedLight(Level level, BlockPos pos) {
        long remaining = legacyLight;
        legacyLight = 0;
        for (int t = ModItems.MOTES.length - 1; t >= 0 && remaining > 0; t--) {
            long value = ModItems.MOTE_VALUES[t];
            long count = remaining / value;
            remaining %= value;
            while (count > 0) {
                int batch = (int) Math.min(64, count);
                count -= batch;
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(ModItems.MOTES[t], batch));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        if (legacyLight > 0) nbt.putLong("bound_light", legacyLight);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        legacyLight = nbt.getLongOr("bound_light", 0L);
    }
}
