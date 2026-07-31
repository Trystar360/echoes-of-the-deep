package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.registry.ModBlockEntities;
import com.echoes.transmute.LightValues;
import com.echoes.transmute.TransmutationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * The Resonant Condenser — ProjectE's Energy Condenser, Echoes-style. Bound to
 * its owner's transmutation account: anything hoppered (or placed) into its
 * input slots is dissolved into Bound Light (learning the tone, exactly like
 * the table's burn slot), and while a target tone is set it automatically
 * re-creates that item into its output slot, paying the item's value from the
 * account — no GUI, fully automatable. Right-click with an item to set the
 * target (the item is NOT consumed); sneak-right-click with an empty hand to
 * clear; right-click with an empty hand for a status readout.
 *
 * <p>Slots 0–8 are inputs (any face except the bottom), slot 9 is the output
 * (bottom face only), so a chest-on-top / hopper-below build is a complete
 * ProjectE condenser loop. Honors redstone control like every machine.
 */
public class ResonantCondenserBlockEntity extends BlockEntity
        implements ImplementedInventory, Configurable {

    public static final int INPUT_FIRST = 0, INPUT_LAST = 8, OUTPUT = 9, SIZE = 10;
    private static final int INTERVAL = 10; // ticks between dissolve/condense passes

    /** Condensers expose redstone behaviour only — the "inventory" faces are
     * hard-routed (in = not-bottom, out = bottom), so per-face config would be
     * a decoy. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().build();

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final BlockConfig config = new BlockConfig();
    private String target = ""; // item id of the configured output tone, "" = none
    private int timer;

    public ResonantCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_CONDENSER, pos, state);
        config.applyDefaults(SPEC);
    }

    // --- configuration surface (used by the block's interactions) ---

    public String target() { return target; }

    public void setTarget(Item item) {
        target = BuiltInRegistries.ITEM.getKey(item).toString();
        setChanged();
    }

    public void clearTarget() {
        target = "";
        setChanged();
    }

    /** Bound-Light balance of the owning account, or -1 when unclaimed. */
    public long ownerLight(ServerLevel sw) {
        return config.owner() == null ? -1 : TransmutationState.get(sw).of(config.owner()).light;
    }

    // --- tick ---

    public static void tick(Level level, BlockPos pos, BlockState state, ResonantCondenserBlockEntity be) {
        if (!(level instanceof ServerLevel sw)) return;
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!be.config.redstone().allows(sw.hasNeighborSignal(pos))) return;
        if (be.config.owner() == null) return;

        TransmutationState ts = TransmutationState.get(sw);
        TransmutationState.Account acc = ts.of(be.config.owner());
        boolean dirty = false;

        // Dissolve: every valued input stack becomes Bound Light + a learned tone.
        for (int s = INPUT_FIRST; s <= INPUT_LAST; s++) {
            ItemStack in = be.items.get(s);
            if (in.isEmpty()) continue;
            long unit = LightValues.get(in.getItem());
            if (unit <= 0) continue; // valueless items just sit there
            acc.light += unit * in.getCount();
            acc.attuned.add(BuiltInRegistries.ITEM.getKey(in.getItem()));
            be.items.set(s, ItemStack.EMPTY);
            dirty = true;
        }

        // Condense: pay the target's value from the pool, emit one into the output.
        if (!be.target.isEmpty()) {
            Optional<Item> want = BuiltInRegistries.ITEM.getOptional(Identifier.parse(be.target));
            if (want.isPresent()) {
                Item item = want.get();
                long unit = LightValues.get(item);
                if (unit > 0 && acc.attuned.contains(BuiltInRegistries.ITEM.getKey(item))
                        && acc.light >= unit && be.acceptOutput(item)) {
                    acc.light -= unit;
                    dirty = true;
                }
            }
        }

        if (dirty) {
            ts.setDirty();
            be.setChanged();
        }
    }

    /** Add one of {@code item} to the output slot if it can stack there. */
    private boolean acceptOutput(Item item) {
        ItemStack out = items.get(OUTPUT);
        if (out.isEmpty()) { items.set(OUTPUT, new ItemStack(item)); return true; }
        if (out.getItem() == item && out.getCount() < out.getMaxStackSize()) { out.grow(1); return true; }
        return false;
    }

    // --- inventory (hopper routing) ---

    @Override public NonNullList<ItemStack> getItems() { return items; }

    @Override public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return new int[]{OUTPUT};
        int[] slots = new int[INPUT_LAST - INPUT_FIRST + 1];
        for (int i = 0; i < slots.length; i++) slots[i] = INPUT_FIRST + i;
        return slots;
    }

    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= INPUT_FIRST && slot <= INPUT_LAST && dir != Direction.DOWN;
    }

    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT && dir == Direction.DOWN;
    }

    // --- Configurable ---

    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    // --- persistence ---

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
        config.writeNbt(nbt);
        nbt.putString("target", target);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
        config.readNbt(nbt);
        target = nbt.getStringOr("target", "");
    }
}
