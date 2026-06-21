package com.echoes.screen;

import com.echoes.registry.ModScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class CrusherScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData props;

    private static final int MACHINE_SLOTS = 3;

    /** Client constructor. */
    public CrusherScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(3));
    }

    public CrusherScreenHandler(int syncId, Inventory playerInv, Container inv, ContainerData props) {
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
    public boolean canUse(Player player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(Player player, int slotIndex) {
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
