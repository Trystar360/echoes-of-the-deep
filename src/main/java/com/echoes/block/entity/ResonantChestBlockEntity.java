package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A 27-slot storage block that is natively on a channel — no separate relay
 * needed. It is a passive buffer: senders on its channel fill it, receivers drain
 * it, but it never trades with other passive stores. Open it like any chest.
 */
public class ResonantChestBlockEntity extends AbstractChannelDeviceBlockEntity
        implements ImplementedInventory, MenuProvider {

    public static final int SIZE = 27;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ResonantChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_CHEST, pos, state);
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    @Override public boolean isPassiveStorage() { return true; }

    @Override public @Nullable Storage<ItemVariant> wirelessItems() {
        return ContainerStorage.of(this, null);
    }

    // Full, unsided access for hoppers/pipes and the wireless network.
    @Override public int[] getAvailableSlots(Direction side) {
        int[] slots = new int[SIZE];
        for (int i = 0; i < SIZE; i++) slots[i] = i;
        return slots;
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return true; }

    // --- screen ---
    @Override public Component getDisplayName() { return Component.translatable("block.echoes.wave_chest"); }

    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return ChestMenu.threeRows(syncId, inv, this);
    }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
    }
}
