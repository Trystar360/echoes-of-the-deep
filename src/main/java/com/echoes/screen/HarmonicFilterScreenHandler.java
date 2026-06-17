package com.echoes.screen;

import com.echoes.block.entity.HarmonicFilterBlockEntity;
import com.echoes.registry.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * A 3×3 grid of <em>ghost</em> slots: clicking sets a slot to a single-item sample
 * of whatever is on the cursor (without consuming it); clicking with an empty
 * cursor clears it. The samples drive {@link HarmonicFilterBlockEntity#itemWhitelist()}.
 */
public class HarmonicFilterScreenHandler extends ScreenHandler {
    private static final int SIZE = HarmonicFilterBlockEntity.SIZE;
    private final Inventory filter;

    /** Client constructor. */
    public HarmonicFilterScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(SIZE));
    }

    public HarmonicFilterScreenHandler(int syncId, PlayerInventory playerInv, Inventory filter) {
        super(ModScreens.HARMONIC_FILTER, syncId);
        this.filter = filter;
        checkSize(filter, SIZE);

        // 3x3 ghost grid
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(filter, r * 3 + c, 62 + c * 18, 18 + r * 18) {
                    @Override public boolean canInsert(ItemStack stack) { return false; }
                    @Override public boolean canTakeItems(PlayerEntity player) { return false; }
                    @Override public int getMaxItemCount() { return 1; }
                });

        // player inventory + hotbar
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < SIZE) {
            // Ghost slot: set/clear a sample from the cursor without consuming it.
            if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.PICKUP_ALL) {
                ItemStack cursor = getCursorStack();
                Slot slot = this.slots.get(slotIndex);
                if (cursor.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    ItemStack ghost = cursor.copy();
                    ghost.setCount(1);
                    slot.setStack(ghost);
                }
            }
            return; // ghost slots never run default handling
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    /** No shift-transfer — the grid is configured by clicking, not by moving items. */
    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return filter.canPlayerUse(player);
    }
}
