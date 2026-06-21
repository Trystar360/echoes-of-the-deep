package com.echoes.screen;

import com.echoes.block.entity.HarmonicFilterBlockEntity;
import com.echoes.registry.ModScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;

/**
 * A 3×3 grid of <em>ghost</em> slots: clicking sets a slot to a single-item sample
 * of whatever is on the cursor (without consuming it); clicking with an empty
 * cursor clears it. The samples drive {@link HarmonicFilterBlockEntity#itemWhitelist()}.
 */
public class HarmonicFilterScreenHandler extends AbstractContainerMenu {
    private static final int SIZE = HarmonicFilterBlockEntity.SIZE;
    private final Container filter;

    /** Client constructor. */
    public HarmonicFilterScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(SIZE));
    }

    public HarmonicFilterScreenHandler(int syncId, Inventory playerInv, Container filter) {
        super(ModScreens.HARMONIC_FILTER, syncId);
        this.filter = filter;
        checkContainerSize(filter, SIZE);

        // 3x3 ghost grid
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(filter, r * 3 + c, 62 + c * 18, 18 + r * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player player) { return false; }
                    @Override public int getMaxStackSize() { return 1; }
                });

        // player inventory + hotbar
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex >= 0 && slotIndex < SIZE) {
            // Ghost slot: set/clear a sample from the cursor without consuming it.
            if (actionType == ContainerInput.PICKUP || actionType == ContainerInput.PICKUP_ALL) {
                ItemStack cursor = getCarried();
                Slot slot = this.slots.get(slotIndex);
                if (cursor.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    ItemStack ghost = cursor.copy();
                    ghost.setCount(1);
                    slot.set(ghost);
                }
            }
            return; // ghost slots never run default handling
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    /** No shift-transfer — the grid is configured by clicking, not by moving items. */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return filter.stillValid(player);
    }
}
