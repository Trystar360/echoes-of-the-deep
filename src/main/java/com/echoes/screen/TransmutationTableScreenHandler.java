package com.echoes.screen;

import com.echoes.block.entity.TransmutationTableBlockEntity;
import com.echoes.registry.ModItems;
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
import org.jetbrains.annotations.Nullable;

/**
 * The Transmutation Table menu: an input slot (dissolve matter → Bound Light), an
 * extract-only output slot, and five withdraw buttons (one per Mote tone) that pay the
 * banked Bound Light back out as currency.
 */
public class TransmutationTableScreenHandler extends ScreenHandler {
    private static final int MACHINE_SLOTS = 2;
    private static final long DIGIT = 32768L;

    private final Inventory inventory;
    private final PropertyDelegate props;
    private final @Nullable TransmutationTableBlockEntity be; // null on the client

    /** Client constructor. */
    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(MACHINE_SLOTS), new ArrayPropertyDelegate(3), null);
    }

    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInv, Inventory inv,
                                           PropertyDelegate props, @Nullable TransmutationTableBlockEntity be) {
        super(ModScreens.TRANSMUTATION_TABLE, syncId);
        this.inventory = inv;
        this.props = props;
        this.be = be;
        checkSize(inv, MACHINE_SLOTS);
        inv.onOpen(playerInv.player);

        this.addSlot(new Slot(inv, TransmutationTableBlockEntity.INPUT, 44, 35));   // dissolve
        this.addSlot(new Slot(inv, TransmutationTableBlockEntity.OUTPUT, 116, 35) { // extract-only
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));

        this.addProperties(props);
    }

    /** Banked Bound Light, reconstructed from the 3×15-bit property delegate. */
    public long boundLight() {
        return (props.get(0) & 0xFFFFL) + props.get(1) * DIGIT + props.get(2) * DIGIT * DIGIT;
    }

    public static long moteValue(int tier) { return ModItems.MOTE_VALUES[tier]; }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < ModItems.MOTES.length && be != null) {
            be.withdraw(id, player);
            return true;
        }
        return super.onButtonClick(player, id);
    }

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
            } else if (!this.insertItem(original, 0, 1, false)) { // player -> input only
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }
}
