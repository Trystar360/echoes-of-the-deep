package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        markDirty();
        return l;
    }

    /** On break, scatter any legacy banked Light as Mote coins (largest tone first). */
    public void dropBankedLight(World world, BlockPos pos) {
        long remaining = legacyLight;
        legacyLight = 0;
        for (int t = ModItems.MOTES.length - 1; t >= 0 && remaining > 0; t--) {
            long value = ModItems.MOTE_VALUES[t];
            long count = remaining / value;
            remaining %= value;
            while (count > 0) {
                int batch = (int) Math.min(64, count);
                count -= batch;
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(ModItems.MOTES[t], batch));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        if (legacyLight > 0) nbt.putLong("bound_light", legacyLight);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        legacyLight = nbt.getLong("bound_light");
    }
}
