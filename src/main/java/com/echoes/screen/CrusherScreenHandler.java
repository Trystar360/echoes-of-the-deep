package com.echoes.screen;

import com.echoes.registry.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CrusherScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate props;

    private static final int MACHINE_SLOTS = 3;

    /** Client constructor. */
    public CrusherScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(MACHINE_SLOTS), new ArrayPropertyDelegate(3));
    }

    public CrusherScreenHandler(int syncId, PlayerInventory playerInv, Inventory inv, PropertyDelegate props) {
        super(ModScreens.CRUSHER, syncId);
        this.inventory = inv;
        this.props = props;
        checkSize(inv, MACHINE_SLOTS);
        inv.onOpen(playerInv.player);

        this.addSlot(new Slot(inv, 0, 56, 35));        // input
        this.addSlot(new Slot(inv, 1, 116, 35) {       // output (extract-only)
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inv, 2, 116, 57) {       // byproduct (extract-only)
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        // player inventory + hotbar
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));

        this.addProperties(props);
    }

    public int progress() { return props.get(0); }
    public int maxProgress() { return props.get(1); }
    public int storedRu() { return props.get(2); }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack original = slot.getStack();
            newStack = original.copy();
            if (slotIndex < MACHINE_SLOTS) {
                if (!this.insertItem(original, MACHINE_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.insertItem(original, 0, 1, false)) {  // player -> input only
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }
}
