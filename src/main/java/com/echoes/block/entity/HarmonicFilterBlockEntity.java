package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.HarmonicFilterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        implements ImplementedInventory, NamedScreenHandlerFactory {

    public static final int SIZE = 9;
    private final DefaultedList<ItemStack> samples = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);

    public HarmonicFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARMONIC_FILTER, pos, state);
    }

    @Override public DefaultedList<ItemStack> getItems() { return samples; }

    @Override public @Nullable Set<Item> itemWhitelist() {
        Set<Item> set = new LinkedHashSet<>();
        for (ItemStack s : samples) if (!s.isEmpty()) set.add(s.getItem());
        return set.isEmpty() ? null : set;
    }

    // The ghost grid is configured via the screen only — keep hoppers/pipes out.
    @Override public int[] getAvailableSlots(Direction side) { return new int[0]; }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return false; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return false; }

    // --- screen ---
    @Override public Text getDisplayName() { return Text.translatable("block.echoes.wave_filter"); }

    @Override public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new HarmonicFilterScreenHandler(syncId, inv, this);
    }

    @Override
    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        net.minecraft.inventory.Inventories.writeNbt(nbt, samples, lookup);
    }

    @Override
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        net.minecraft.inventory.Inventories.readNbt(nbt, samples, lookup);
    }
}
