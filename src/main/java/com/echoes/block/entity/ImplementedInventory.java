package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;

/**
 * The standard Fabric convenience interface: implement getItems() and you get a
 * full WorldlyContainer for free. Keeps the Crusher readable.
 */
public interface ImplementedInventory extends WorldlyContainer {

    NonNullList<ItemStack> getItems();

    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    @Override default int size() { return getItems().size(); }

    @Override default boolean isEmpty() {
        for (ItemStack s : getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override default ItemStack getStack(int slot) { return getItems().get(slot); }

    @Override default ItemStack removeStack(int slot, int count) {
        ItemStack result = net.minecraft.world.ContainerHelper.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override default ItemStack removeStack(int slot) {
        return net.minecraft.world.ContainerHelper.removeStack(getItems(), slot);
    }

    @Override default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        setChanged();
    }

    @Override default void clear() { getItems().clear(); }

    @Override default void setChanged() {}

    @Override default boolean canPlayerUse(net.minecraft.world.entity.player.Player player) { return true; }

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
