package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.HarmonicFilterScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Constrains transport on its channel to a whitelist defined by a 3×3 grid of
 * ghost slots (place a sample item to whitelist its type — the item isn't
 * consumed). Multiple filters on a channel union their whitelists. Fluids are
 * matched by their bucket item, so a water bucket in the grid whitelists water.
 */
public class HarmonicFilterBlockEntity extends AbstractChannelDeviceBlockEntity
        implements ImplementedInventory, MenuProvider {

    public static final int SIZE = 9;
    private final NonNullList<ItemStack> samples = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public HarmonicFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARMONIC_FILTER, pos, state);
    }

    @Override public NonNullList<ItemStack> getItems() { return samples; }

    @Override public @Nullable Set<Item> itemWhitelist() {
        Set<Item> set = new LinkedHashSet<>();
        for (ItemStack s : samples) if (!s.isEmpty()) set.add(s.getItem());
        return set.isEmpty() ? null : set;
    }

    // The ghost grid is configured via the screen only — keep hoppers/pipes out.
    @Override public int[] getAvailableSlots(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return false; }

    // --- screen ---
    @Override public Component getDisplayName() { return Component.translatable("block.echoes.wave_filter"); }

    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new HarmonicFilterScreenHandler(syncId, inv, this);
    }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, samples);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, samples);
    }
}
