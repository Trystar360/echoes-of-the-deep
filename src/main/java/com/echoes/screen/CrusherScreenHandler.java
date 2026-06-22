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
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CrusherScreenHandler extends AbstractContainerMenu {
    /** Button id: open this block's Info panel (server resolves the position). */
    public static final int B_INFO = 50;
    /** Button id: open this block's Config screen. */
    public static final int B_CONFIG = 51;

    private final Container inventory;
    private final ContainerData props;
    private final @Nullable BlockPos pos;   // server-side only; null on the client

    private static final int MACHINE_SLOTS = 5;          // input, output, byproduct, 2 augments
    // Augment slot screen positions (right column).
    public static final int AUG0_X = 152, AUG0_Y = 17, AUG1_X = 152, AUG1_Y = 39;

    /** Client constructor. */
    public CrusherScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(3), null);
    }

    public CrusherScreenHandler(int syncId, Inventory playerInv, Container inv, ContainerData props, @Nullable BlockPos pos) {
        super(ModScreens.CRUSHER, syncId);
        this.inventory = inv;
        this.props = props;
        this.pos = pos;
        checkContainerSize(inv, MACHINE_SLOTS);
        inv.startOpen(playerInv.player);

        this.addSlot(new Slot(inv, 0, 56, 35));        // input
        this.addSlot(new Slot(inv, 1, 116, 35) {       // output (extract-only)
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inv, 2, 116, 57) {       // byproduct (extract-only)
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inv, 3, AUG0_X, AUG0_Y) {   // augment slots
            @Override public boolean mayPlace(ItemStack s) { return com.echoes.block.entity.CrusherBlockEntity.isAugment(s); }
        });
        this.addSlot(new Slot(inv, 4, AUG1_X, AUG1_Y) {
            @Override public boolean mayPlace(ItemStack s) { return com.echoes.block.entity.CrusherBlockEntity.isAugment(s); }
        });

        // player inventory + hotbar
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));

        this.addDataSlots(props);
    }

    public int progress() { return props.get(0); }
    public int maxProgress() { return props.get(1); }
    public int storedRu() { return props.get(2); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (pos == null || player.level().isClientSide()) return super.clickMenuButton(player, id);
        if (id == B_INFO) {
            player.openMenu(new InfoScreenFactory(player.level().getBlockState(pos).getBlock().getName(), pos));
            return true;
        }
        if (id == B_CONFIG && player.level().getBlockEntity(pos) instanceof com.echoes.config.Configurable cfg) {
            player.openMenu(new ConfigScreenFactory(cfg, pos));
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (slotIndex < MACHINE_SLOTS) {
                if (!this.moveItemStackTo(original, MACHINE_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (com.echoes.block.entity.CrusherBlockEntity.isAugment(original)) {
                if (!this.moveItemStackTo(original, 3, 5, false)) return ItemStack.EMPTY;   // player -> augment slots
            } else if (!this.moveItemStackTo(original, 0, 1, false)) {  // player -> input only
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }
}
