package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import com.echoes.transmute.LightValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Banks <b>Bound Light</b> (the EMC pool). Slot 0 accepts matter to dissolve (one item
 * every {@link #DISSOLVE_INTERVAL} ticks, adding its Light Value to the pool); the screen
 * pays the pool back out as Mote "coins" via {@link #withdraw}.
 */
public class TransmutationTableBlockEntity extends BlockEntity
        implements ImplementedInventory, NamedScreenHandlerFactory {

    public static final int INPUT = 0, OUTPUT = 1;
    private static final int SLOTS = 2;
    private static final int DISSOLVE_INTERVAL = 10; // ticks per item dissolved
    /** Display encoding base for the property delegate (3 × 15-bit digits). */
    private static final long DIGIT = 32768L;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOTS, ItemStack.EMPTY);
    private long boundLight;
    private int dissolveTimer;

    private final PropertyDelegate props = new PropertyDelegate() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> (int) (boundLight % DIGIT);
                case 1 -> (int) ((boundLight / DIGIT) % DIGIT);
                case 2 -> (int) ((boundLight / (DIGIT * DIGIT)) % DIGIT);
                default -> 0;
            };
        }
        @Override public void set(int i, int v) { /* display only */ }
        @Override public int size() { return 3; }
    };

    public TransmutationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSMUTATION_TABLE, pos, state);
    }

    @Override public DefaultedList<ItemStack> getItems() { return items; }

    public long boundLight() { return boundLight; }

    public static void tick(World world, BlockPos pos, BlockState state, TransmutationTableBlockEntity be) {
        if (world.isClient) return;
        ItemStack in = be.getStack(INPUT);
        if (in.isEmpty()) { be.dissolveTimer = 0; return; }

        long unit = LightValues.get(in.getItem());
        if (unit <= 0) return; // no value / blacklisted — leave it be

        if (++be.dissolveTimer >= DISSOLVE_INTERVAL) {
            be.dissolveTimer = 0;
            in.decrement(1);
            be.boundLight += unit;
            be.markDirty();
        }
    }

    /** Pay one Mote of the given tier (0..4) out of the pool, if affordable. */
    public boolean withdraw(int tier, PlayerEntity player) {
        if (tier < 0 || tier >= ModItems.MOTES.length) return false;
        long cost = ModItems.MOTE_VALUES[tier];
        if (boundLight < cost) return false;
        boundLight -= cost;
        player.getInventory().offerOrDrop(new ItemStack(ModItems.MOTES[tier]));
        markDirty();
        return true;
    }

    // --- screen ---
    @Override public Text getDisplayName() { return Text.translatable("block.echoes.transmutation_table"); }
    @Override public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new TransmutationTableScreenHandler(syncId, inv, this, props, this);
    }

    @Override public void markDirty() { super.markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        net.minecraft.inventory.Inventories.writeNbt(nbt, items, lookup);
        nbt.putLong("bound_light", boundLight);
        nbt.putInt("dissolve_timer", dissolveTimer);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        net.minecraft.inventory.Inventories.readNbt(nbt, items, lookup);
        boundLight = nbt.getLong("bound_light");
        dissolveTimer = nbt.getInt("dissolve_timer");
    }
}
