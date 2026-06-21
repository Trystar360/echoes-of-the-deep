package com.echoes.block.entity;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;

/**
 * The standard convenience interface: implement getItems() and you get a full
 * {@link WorldlyContainer} for free. Keeps the Crusher readable.
 *
 * <p>26.1: vanilla container method names (getItem/setItem/removeItem/…) replace the
 * old Yarn names.
 */
public interface ImplementedInventory extends WorldlyContainer {

    NonNullList<ItemStack> getItems();

    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    @Override default int getContainerSize() { return getItems().size(); }

    @Override default boolean isEmpty() {
        for (ItemStack s : getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override default ItemStack getItem(int slot) { return getItems().get(slot); }

    @Override default ItemStack removeItem(int slot, int count) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(getItems(), slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override default ItemStack removeItemNoUpdate(int slot) {
        return net.minecraft.world.ContainerHelper.takeItem(getItems(), slot);
    }

    @Override default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override default void clearContent() { getItems().clear(); }

    @Override default void setChanged() {}

    @Override default boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }

    // Default sided access: top = input (slot 0), everything else = output (slot 1).
    @Override default int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[]{0} : new int[]{1};
    }

    @Override default boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    @Override default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }
}
