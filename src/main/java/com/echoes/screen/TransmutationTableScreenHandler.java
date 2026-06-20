package com.echoes.screen;

import com.echoes.registry.ModItems;
import com.echoes.registry.ModScreens;
import com.echoes.transmute.LightValues;
import com.echoes.transmute.TransmutationState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The transmutation terminal — shared by the Transmutation Table (block) and the
 * Transmutation Tablet (item). Both operate on the opening player's {@link
 * TransmutationState.Account} (Bound Light pool + attuned tones):
 *
 * <ul>
 *   <li><b>Dissolve</b> (input slot → button) banks the item's Light Value and attunes it.</li>
 *   <li><b>Withdraw</b> (five tone buttons) pays the pool out as Mote coins.</li>
 *   <li><b>Condense</b> (ghost template slot → button) re-creates an <i>attuned</i> item
 *       from the pool (×1 or a stack).</li>
 * </ul>
 */
public class TransmutationTableScreenHandler extends ScreenHandler {
    public static final int INPUT = 0, OUTPUT = 1, TEMPLATE = 2;
    private static final int MACHINE_SLOTS = 3;
    private static final long DIGIT = 32768L;

    // button ids
    public static final int BTN_DISSOLVE = 10, BTN_CONDENSE_1 = 11, BTN_CONDENSE_STACK = 12;

    // slot layout (kept in sync with the client screen)
    public static final int SLOT_Y = 40, INPUT_X = 26, TEMPLATE_X = 80, OUTPUT_X = 134;
    private static final int INV_Y = 118, HOTBAR_Y = 176;

    private final SimpleInventory inv = new SimpleInventory(MACHINE_SLOTS);
    private final PropertyDelegate props;
    private final PlayerEntity player;
    private final @Nullable TransmutationState state;       // server only
    private final TransmutationState.@Nullable Account account; // server only

    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInv) {
        super(ModScreens.TRANSMUTATION_TABLE, syncId);
        this.player = playerInv.player;

        if (player.getWorld() instanceof ServerWorld sw) {
            this.state = TransmutationState.get(sw);
            this.account = state.of(player.getUuid());
            this.props = new PropertyDelegate() {
                @Override public int get(int i) {
                    long bl = account.light;
                    return switch (i) {
                        case 0 -> (int) (bl % DIGIT);
                        case 1 -> (int) ((bl / DIGIT) % DIGIT);
                        case 2 -> (int) ((bl / (DIGIT * DIGIT)) % DIGIT);
                        default -> 0;
                    };
                }
                @Override public void set(int i, int v) { }
                @Override public int size() { return 3; }
            };
        } else {
            this.state = null;
            this.account = null;
            this.props = new ArrayPropertyDelegate(3);
        }

        this.addSlot(new Slot(inv, INPUT, INPUT_X, SLOT_Y));         // dissolve
        this.addSlot(new Slot(inv, OUTPUT, OUTPUT_X, SLOT_Y) {       // extract-only
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inv, TEMPLATE, TEMPLATE_X, SLOT_Y) {   // ghost target
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public boolean canTakeItems(PlayerEntity p) { return false; }
            @Override public int getMaxItemCount() { return 1; }
        });

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y));

        this.addProperties(props);
    }

    /** Banked Bound Light, reconstructed from the 3×15-bit property delegate (client). */
    public long boundLight() {
        return (props.get(0) & 0xFFFFL) + props.get(1) * DIGIT + props.get(2) * DIGIT * DIGIT;
    }

    public static long moteValue(int tier) { return ModItems.MOTE_VALUES[tier]; }

    private static Identifier id(Item item) { return Registries.ITEM.getId(item); }

    @Override
    public boolean onButtonClick(PlayerEntity p, int btn) {
        if (account == null) return false; // server-authoritative
        boolean changed;
        if (btn >= 0 && btn < ModItems.MOTES.length) {
            changed = withdraw(btn);
        } else if (btn == BTN_DISSOLVE) {
            changed = dissolve();
        } else if (btn == BTN_CONDENSE_1) {
            changed = condense(1);
        } else if (btn == BTN_CONDENSE_STACK) {
            changed = condense(64);
        } else {
            return super.onButtonClick(p, btn);
        }
        if (changed) state.markDirty();
        return true;
    }

    /** Dissolve the input stack into banked Light and attune its tone. */
    private boolean dissolve() {
        ItemStack in = inv.getStack(INPUT);
        if (in.isEmpty()) return false;
        long unit = LightValues.get(in.getItem());
        if (unit <= 0) return false; // no value / blacklisted
        account.light += unit * in.getCount();
        account.attuned.add(id(in.getItem()));
        inv.setStack(INPUT, ItemStack.EMPTY);
        return true;
    }

    /** Pay one Mote of the given tone out of the pool. */
    private boolean withdraw(int tier) {
        long cost = ModItems.MOTE_VALUES[tier];
        if (account.light < cost) return false;
        if (!outputInsert(new ItemStack(ModItems.MOTES[tier]))) return false;
        account.light -= cost;
        return true;
    }

    /** Re-create the (attuned) template item from the pool, up to {@code max} copies. */
    private boolean condense(int max) {
        ItemStack tmpl = inv.getStack(TEMPLATE);
        if (tmpl.isEmpty()) return false;
        Item item = tmpl.getItem();
        if (!account.attuned.contains(id(item))) return false;
        long unit = LightValues.get(item);
        if (unit <= 0) return false;
        int made = 0;
        while (made < max && account.light >= unit && addToOutput(item)) {
            account.light -= unit;
            made++;
        }
        return made > 0;
    }

    /** Add one of {@code item} to the output slot if it can stack there. */
    private boolean addToOutput(Item item) {
        ItemStack out = inv.getStack(OUTPUT);
        if (out.isEmpty()) { inv.setStack(OUTPUT, new ItemStack(item)); return true; }
        if (out.getItem() == item && out.getCount() < out.getMaxCount()) { out.increment(1); return true; }
        return false;
    }

    /** Output slot first (so hoppers can pull), else the player's inventory. */
    private boolean outputInsert(ItemStack stack) {
        ItemStack out = inv.getStack(OUTPUT);
        if (out.isEmpty()) { inv.setStack(OUTPUT, stack); return true; }
        if (ItemStack.areItemsAndComponentsEqual(out, stack) && out.getCount() < out.getMaxCount()) {
            out.increment(1); return true;
        }
        return player.getInventory().insertStack(stack);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity p) {
        if (slotIndex == TEMPLATE) {
            // Ghost slot: set/clear the condense target from the cursor without consuming it.
            if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.PICKUP_ALL) {
                ItemStack cursor = getCursorStack();
                if (cursor.isEmpty()) {
                    inv.setStack(TEMPLATE, ItemStack.EMPTY);
                } else {
                    ItemStack ghost = cursor.copy();
                    ghost.setCount(1);
                    inv.setStack(TEMPLATE, ghost);
                }
            }
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, p);
    }

    @Override
    public ItemStack quickMove(PlayerEntity p, int slotIndex) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            if (slotIndex == TEMPLATE) return ItemStack.EMPTY; // ghost
            ItemStack original = slot.getStack();
            moved = original.copy();
            if (slotIndex < MACHINE_SLOTS) {
                if (!this.insertItem(original, MACHINE_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.insertItem(original, INPUT, INPUT + 1, false)) { // player -> input only
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return moved;
    }

    @Override
    public void onClosed(PlayerEntity p) {
        super.onClosed(p);
        // Return real held items; the ghost template is a phantom and is simply dropped.
        if (!p.getWorld().isClient) {
            returnStack(p, INPUT);
            returnStack(p, OUTPUT);
        }
        inv.setStack(TEMPLATE, ItemStack.EMPTY);
    }

    private void returnStack(PlayerEntity p, int slot) {
        ItemStack s = inv.getStack(slot);
        if (!s.isEmpty()) {
            p.getInventory().offerOrDrop(s);
            inv.setStack(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canUse(PlayerEntity p) { return true; }
}
