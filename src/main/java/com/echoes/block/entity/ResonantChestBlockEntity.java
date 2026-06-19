package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A 27-slot storage block that is natively on a channel — no separate relay
 * needed. It is a passive buffer: senders on its channel fill it, receivers drain
 * it, but it never trades with other passive stores. Open it like any chest.
 */
public class ResonantChestBlockEntity extends AbstractChannelDeviceBlockEntity
        implements ImplementedInventory, NamedScreenHandlerFactory {

    public static final int SIZE = 27;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);

    public ResonantChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_CHEST, pos, state);
    }

    @Override public DefaultedList<ItemStack> getItems() { return items; }

    @Override public boolean isPassiveStorage() { return true; }

    @Override public @Nullable Storage<ItemVariant> wirelessItems() {
        return InventoryStorage.of(this, null);
    }

    // Full, unsided access for hoppers/pipes and the wireless network.
    @Override public int[] getAvailableSlots(Direction side) {
        int[] slots = new int[SIZE];
        for (int i = 0; i < SIZE; i++) slots[i] = i;
        return slots;
    }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return true; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return true; }

    // --- screen ---
    @Override public Text getDisplayName() { return Text.translatable("block.echoes.wave_chest"); }

    @Override public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, this);
    }

    @Override
    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        net.minecraft.inventory.Inventories.writeNbt(nbt, items, lookup);
    }

    @Override
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        net.minecraft.inventory.Inventories.readNbt(nbt, items, lookup);
    }
}
