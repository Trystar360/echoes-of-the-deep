package com.echoes.block.entity;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

/**
 * The standard Fabric convenience interface: implement getItems() and you get a
 * full SidedInventory for free. Keeps the Crusher readable.
 */
public interface ImplementedInventory extends SidedInventory {

    DefaultedList<ItemStack> getItems();

    static ImplementedInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }

    @Override default int size() { return getItems().size(); }

    @Override default boolean isEmpty() {
        for (ItemStack s : getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override default ItemStack getStack(int slot) { return getItems().get(slot); }

    @Override default ItemStack removeStack(int slot, int count) {
        ItemStack result = net.minecraft.inventory.Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override default ItemStack removeStack(int slot) {
        return net.minecraft.inventory.Inventories.removeStack(getItems(), slot);
    }

    @Override default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }

    @Override default void clear() { getItems().clear(); }

    @Override default void markDirty() {}

    @Override default boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) { return true; }

    // Default sided access: top = input (slot 0), everything else = output (slot 1).
    @Override default int[] getAvailableSlots(Direction side) {
        return side == Direction.UP ? new int[]{0} : new int[]{1};
    }

    @Override default boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    @Override default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }
}
