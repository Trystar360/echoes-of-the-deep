package com.echoes.screen;

import com.echoes.registry.ModItems;
import com.echoes.registry.ModScreens;
import com.echoes.transmute.LightValues;
import com.echoes.transmute.TransmutationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The transmutation terminal — shared by the Transmutation Table (block) and the
 * Transmutation Tablet (item). Both operate on the opening player's {@link
 * TransmutationState.Account} (Bound Light pool + learned tones), ProjectE-style:
 *
 * <ul>
 *   <li><b>Dissolve / learn</b> (input slot → button): banks the item's Light Value
 *       and <i>learns</i> it (adds it to your knowledge) so it appears in the grid.</li>
 *   <li><b>Knowledge grid</b>: every item you've learned shows as a display icon.
 *       Click one to <b>create</b> it (left = one, shift/right = a stack), paying its
 *       Light Value out of your banked pool. Light is conserved — creating costs
 *       exactly what dissolving refunds — so there is no infinite duplication.</li>
 *   <li><b>Withdraw</b> (five tone buttons): pays the pool out as Mote coins.</li>
 * </ul>
 *
 * <p>The knowledge grid is built from real (display-only) menu slots filled by the
 * server, so it rides vanilla menu syncing — no custom packets. The set is paged
 * when a player has learned more than one screen's worth.
 */
public class TransmutationTableScreenHandler extends AbstractContainerMenu {
    public static final int INPUT = 0, OUTPUT = 1;
    public static final int GRID_COLS = 9, GRID_ROWS = 3, GRID_SIZE = GRID_COLS * GRID_ROWS;
    public static final int KNOWLEDGE_START = 2;                 // first knowledge slot index
    private static final int MACHINE_SLOTS = KNOWLEDGE_START + GRID_SIZE;
    private static final long DIGIT = 32768L;

    // button ids
    public static final int BTN_DISSOLVE = 100, BTN_PAGE_PREV = 101, BTN_PAGE_NEXT = 102;

    // slot layout (kept in sync with the client screen)
    public static final int INPUT_X = 8, OUTPUT_X = 150, SLOT_Y = 18;
    public static final int GRID_X = 8, GRID_Y = 90;
    private static final int INV_Y = 160, HOTBAR_Y = 218;

    private final SimpleContainer inv = new SimpleContainer(2);                 // input + output
    private final SimpleContainer knowledge = new SimpleContainer(GRID_SIZE);   // display-only grid
    private final ContainerData props;
    private final Player player;
    private final @Nullable TransmutationState state;       // server only
    private final TransmutationState.@Nullable Account account; // server only
    private List<Item> learned = List.of();                 // server only, sorted, current view
    private int page = 0;

    public TransmutationTableScreenHandler(int syncId, Inventory playerInv) {
        super(ModScreens.TRANSMUTATION_TABLE, syncId);
        this.player = playerInv.player;

        if (player.level() instanceof ServerLevel sw) {
            this.state = TransmutationState.get(sw);
            this.account = state.of(player.getUUID());
            this.props = new ContainerData() {
                @Override public int get(int i) {
                    long bl = account.light;
                    return switch (i) {
                        case 0 -> (int) (bl % DIGIT);
                        case 1 -> (int) ((bl / DIGIT) % DIGIT);
                        case 2 -> (int) ((bl / (DIGIT * DIGIT)) % DIGIT);
                        case 3 -> pageCount();
                        case 4 -> page;
                        default -> 0;
                    };
                }
                @Override public void set(int i, int v) { }
                @Override public int getCount() { return 5; }
            };
        } else {
            this.state = null;
            this.account = null;
            this.props = new SimpleContainerData(5);
        }

        this.addSlot(new Slot(inv, INPUT, INPUT_X, SLOT_Y));         // dissolve / learn
        this.addSlot(new Slot(inv, OUTPUT, OUTPUT_X, SLOT_Y) {       // created items out (extract-only)
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Knowledge grid — display-only slots the server fills with learned items.
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int idx = KNOWLEDGE_START + row * GRID_COLS + col;
                this.addSlot(new Slot(knowledge, idx - KNOWLEDGE_START,
                        GRID_X + col * 18, GRID_Y + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                });
            }
        }

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y));

        this.addDataSlots(props);
        refreshKnowledge();
    }

    /** Banked Bound Light, reconstructed from the 3×15-bit property delegate (client). */
    public long boundLight() {
        return (props.get(0) & 0xFFFFL) + props.get(1) * DIGIT + props.get(2) * DIGIT * DIGIT;
    }

    public int pageCountClient() { return Math.max(1, props.get(3)); }
    public int pageClient()      { return props.get(4); }

    public static long moteValue(int tier) { return ModItems.MOTE_VALUES[tier]; }

    private static Identifier id(Item item) { return BuiltInRegistries.ITEM.getKey(item); }

    private int pageCount() {
        if (account == null) return 1;
        int n = account.attuned.size();
        return Math.max(1, (n + GRID_SIZE - 1) / GRID_SIZE);
    }

    /** Rebuild the sorted learned list and fill the visible page's display slots (server). */
    private void refreshKnowledge() {
        if (account == null) return;
        List<Item> all = new ArrayList<>();
        for (Identifier rid : account.attuned) {
            Item it = BuiltInRegistries.ITEM.getValue(rid);
            if (it != null && it != net.minecraft.world.item.Items.AIR) all.add(it);
        }
        all.sort(Comparator.comparing(it -> id(it).toString()));
        int pages = Math.max(1, (all.size() + GRID_SIZE - 1) / GRID_SIZE);
        if (page >= pages) page = pages - 1;
        if (page < 0) page = 0;
        int from = page * GRID_SIZE;
        this.learned = all;
        for (int i = 0; i < GRID_SIZE; i++) {
            int gi = from + i;
            knowledge.setItem(i, gi < all.size() ? new ItemStack(all.get(gi)) : ItemStack.EMPTY);
        }
    }

    @Override
    public boolean clickMenuButton(Player p, int btn) {
        if (account == null) return false; // server-authoritative
        boolean changed = false;
        if (btn >= 0 && btn < ModItems.MOTES.length) {
            changed = withdraw(btn);
        } else if (btn == BTN_DISSOLVE) {
            changed = dissolve();
        } else if (btn == BTN_PAGE_PREV) {
            if (page > 0) { page--; refreshKnowledge(); }
            return true;
        } else if (btn == BTN_PAGE_NEXT) {
            if (page < pageCount() - 1) { page++; refreshKnowledge(); }
            return true;
        } else {
            return super.clickMenuButton(p, btn);
        }
        if (changed) { refreshKnowledge(); state.setDirty(); }
        return true;
    }

    /** Dissolve the input stack into banked Light and learn its tone. */
    private boolean dissolve() {
        ItemStack in = inv.getItem(INPUT);
        if (in.isEmpty()) return false;
        long unit = LightValues.get(in.getItem());
        if (unit <= 0) return false; // no value / blacklisted
        account.light += unit * in.getCount();
        account.attuned.add(id(in.getItem()));
        inv.setItem(INPUT, ItemStack.EMPTY);
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

    /** Create up to {@code max} copies of a learned item, paying its value from the pool. */
    private boolean create(Item item, int max) {
        if (account == null || item == null) return false;
        if (!account.attuned.contains(id(item))) return false;
        long unit = LightValues.get(item);
        if (unit <= 0) return false;
        int made = 0;
        while (made < max && account.light >= unit && addToOutput(item)) {
            account.light -= unit;
            made++;
        }
        if (made > 0) state.setDirty();
        return made > 0;
    }

    /** Add one of {@code item} to the output slot if it can stack there. */
    private boolean addToOutput(Item item) {
        ItemStack out = inv.getItem(OUTPUT);
        if (out.isEmpty()) { inv.setItem(OUTPUT, new ItemStack(item)); return true; }
        if (out.getItem() == item && out.getCount() < out.getMaxStackSize()) { out.grow(1); return true; }
        return false;
    }

    /** Output slot first (so hoppers can pull), else the player's inventory. */
    private boolean outputInsert(ItemStack stack) {
        ItemStack out = inv.getItem(OUTPUT);
        if (out.isEmpty()) { inv.setItem(OUTPUT, stack); return true; }
        if (ItemStack.isSameItemSameComponents(out, stack) && out.getCount() < out.getMaxStackSize()) {
            out.grow(1); return true;
        }
        return player.getInventory().add(stack);
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player p) {
        // Knowledge grid: clicking a learned item creates it (left = 1, shift/right = a stack).
        if (slotIndex >= KNOWLEDGE_START && slotIndex < MACHINE_SLOTS) {
            if (account == null) return; // client: server is authoritative
            int gi = (slotIndex - KNOWLEDGE_START) + page * GRID_SIZE;
            if (gi < 0 || gi >= learned.size()) return;
            int max = (actionType == ContainerInput.QUICK_MOVE || button == 1) ? 64 : 1;
            if (create(learned.get(gi), max)) refreshKnowledge();
            return;
        }
        super.clicked(slotIndex, button, actionType, p);
    }

    @Override
    public ItemStack quickMoveStack(Player p, int slotIndex) {
        ItemStack moved = ItemStack.EMPTY;
        // Shift-clicking a knowledge slot is handled by clicked() (create a stack).
        if (slotIndex >= KNOWLEDGE_START && slotIndex < MACHINE_SLOTS) return ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            moved = original.copy();
            if (slotIndex < MACHINE_SLOTS) {
                if (!this.moveItemStackTo(original, MACHINE_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(original, INPUT, INPUT + 1, false)) { // player -> input only
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return moved;
    }

    @Override
    public void removed(Player p) {
        super.removed(p);
        if (!p.level().isClientSide()) {
            returnStack(p, INPUT);
            returnStack(p, OUTPUT);
        }
    }

    private void returnStack(Player p, int slot) {
        ItemStack s = inv.getItem(slot);
        if (!s.isEmpty()) {
            p.getInventory().placeItemBackInInventory(s);
            inv.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player p) { return true; }
}
