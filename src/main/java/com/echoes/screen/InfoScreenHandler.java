package com.echoes.screen;

import com.echoes.config.Configurable;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModScreens;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only inspection screen for any energy block — opened by right-clicking it
 * with an empty hand (sneak-click for machines, which keep their function menu on a
 * plain click). Shows the block's role, its Light buffer, an in→out flow graphic,
 * and the totals of the network it belongs to. An AE2-style tab strip jumps to the
 * device's Config screen or, for machines, back to its Function menu — so one
 * right-click reaches every panel, no tool required.
 *
 * <p>All live values ride a {@link ContainerData}. Longs (Light amounts, larger than
 * a 16-bit data slot) are packed into three 15-bit digits each, like the
 * Transmutation pool. The snapshot is recomputed once per tick in
 * {@link #broadcastChanges()}.
 */
public class InfoScreenHandler extends AbstractContainerMenu {

    // Data layout. Longs occupy 3 slots (15-bit digits); throughput occupies 2.
    public static final int D_ROLE = 0, D_FLAGS = 1,
            D_STORED = 2,        // 2..4
            D_CAPACITY = 5,      // 5..7
            D_THROUGHPUT = 8,    // 8..9
            D_NET_MEMBERS = 10,
            D_NET_STORED = 11,   // 11..13
            D_NET_CAPACITY = 14; // 14..16
    public static final int SIZE = 17;

    // flag bits in D_FLAGS
    public static final int FLAG_CONFIGURABLE = 1, FLAG_MACHINE = 2;

    // tab buttons
    public static final int B_TAB_CONFIG = 0, B_TAB_FUNCTION = 1;

    private static final long DIGIT = 32768L;

    private final BlockPos pos;
    private final ContainerData data;
    private final int[] snap = new int[SIZE];
    private final @Nullable ServerLevel level;
    private final Player player;

    public InfoScreenHandler(int syncId, Inventory inv, BlockPos pos) {
        super(ModScreens.INFO, syncId);
        this.pos = pos;
        this.player = inv.player;
        this.level = inv.player.level() instanceof ServerLevel sw ? sw : null;
        this.data = new ContainerData() {
            @Override public int get(int i) { return snap[i]; }
            @Override public void set(int i, int v) { snap[i] = v; }
            @Override public int getCount() { return SIZE; }
        };
        if (level != null) recompute();
        addDataSlots(data);
    }

    public BlockPos pos() { return pos; }
    public int role()        { return data.get(D_ROLE); }
    public boolean has(NodeRole r) { return NodeRole.has(role(), r); }
    public boolean configurable()  { return (data.get(D_FLAGS) & FLAG_CONFIGURABLE) != 0; }
    public boolean machine()       { return (data.get(D_FLAGS) & FLAG_MACHINE) != 0; }
    public long stored()      { return readLong(D_STORED); }
    public long capacity()    { return readLong(D_CAPACITY); }
    public long throughput()  { return (data.get(D_THROUGHPUT) & 0xFFFFL) + (long) data.get(D_THROUGHPUT + 1) * DIGIT; }
    public int  netMembers()  { return data.get(D_NET_MEMBERS); }
    public long netStored()   { return readLong(D_NET_STORED); }
    public long netCapacity() { return readLong(D_NET_CAPACITY); }

    private long readLong(int base) {
        return (data.get(base) & 0xFFFFL) + data.get(base + 1) * DIGIT + data.get(base + 2) * DIGIT * DIGIT;
    }
    private void writeLong(int base, long v) {
        snap[base] = (int) (v % DIGIT);
        snap[base + 1] = (int) ((v / DIGIT) % DIGIT);
        snap[base + 2] = (int) ((v / (DIGIT * DIGIT)) % DIGIT);
    }

    @Override
    public void broadcastChanges() {
        if (level != null) recompute();
        super.broadcastChanges();
    }

    private void recompute() {
        BlockEntity be = level.getBlockEntity(pos);
        ResonanceNode node = be instanceof ResonanceNode n ? n : null;
        snap[D_ROLE] = node != null ? node.roleMask() : 0;
        int flags = 0;
        if (be instanceof Configurable) flags |= FLAG_CONFIGURABLE;
        if (be instanceof MenuProvider) flags |= FLAG_MACHINE;
        snap[D_FLAGS] = flags;
        writeLong(D_STORED, node != null ? node.storedRu() : 0);
        writeLong(D_CAPACITY, node != null ? node.capacityRu() : 0);
        long tc = node != null ? node.transferCap() : 0;
        snap[D_THROUGHPUT] = (int) (tc % DIGIT);
        snap[D_THROUGHPUT + 1] = (int) (tc / DIGIT);
        ResonanceNetworkManager.NetInfo ni = ResonanceNetworkManager.get(level).infoFor(pos);
        snap[D_NET_MEMBERS] = ni.members();
        writeLong(D_NET_STORED, ni.stored());
        writeLong(D_NET_CAPACITY, ni.capacity());
    }

    @Override
    public boolean clickMenuButton(Player p, int id) {
        if (level == null || !(p instanceof ServerPlayer)) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if (id == B_TAB_CONFIG && be instanceof Configurable cfg) {
            p.openMenu(new ConfigScreenFactory(cfg, pos));
            return true;
        }
        if (id == B_TAB_FUNCTION && be instanceof MenuProvider mp) {
            p.openMenu(mp);
            return true;
        }
        return false;
    }

    @Override public ItemStack quickMoveStack(Player p, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player p) {
        return p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
