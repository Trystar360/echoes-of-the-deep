package com.echoes.screen;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.config.RedstoneMode;
import com.echoes.config.SecurityMode;
import com.echoes.config.SideMode;
import com.echoes.config.TuningParam;
import com.echoes.registry.ModScreens;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * A slot-less configuration screen shared by every {@link Configurable} device.
 * Live values are mirrored to the client through a {@link ContainerData};
 * edits travel back via vanilla button clicks ({@link #onButtonClick}). The
 * static {@link ConfigSpec} and title are read straight off the (client- or
 * server-side) block entity addressed by {@link #pos}.
 */
public class ConfigScreenHandler extends AbstractContainerMenu {

    // Property layout (size 13): 0 channel, 1 octave, 2 redstone,
    // 3..8 side modes (Direction id order), 9 tuningA, 10 tuningB,
    // 11 security mode, 12 owned-by-viewer flag.
    public static final int SIZE = 13;
    public static final int P_CHANNEL = 0, P_OCTAVE = 1, P_REDSTONE = 2, P_SIDE0 = 3,
            P_TUNING_A = 9, P_TUNING_B = 10, P_SECURITY = 11, P_OWNED = 12;

    // Button ids.
    public static final int B_CHANNEL_DOWN = 0, B_CHANNEL_UP = 1,
            B_OCTAVE_DOWN = 2, B_OCTAVE_UP = 3, B_REDSTONE = 4, B_SECURITY = 5,
            B_SIDE_BASE = 10,            // 10..15 cycle side i
            B_TUNING_A_DOWN = 20, B_TUNING_A_UP = 21,
            B_TUNING_B_DOWN = 22, B_TUNING_B_UP = 23;

    private final BlockPos pos;
    private final ContainerData properties;
    private final ConfigSpec spec;
    @Nullable private final BlockConfig config; // server-side authoritative config
    private final Player player;

    public ConfigScreenHandler(int syncId, Inventory inv, BlockPos pos) {
        super(ModScreens.CONFIG, syncId);
        this.pos = pos;
        this.player = inv.player;

        BlockEntity be = inv.player.level().getBlockEntity(pos);
        Configurable cfg = be instanceof Configurable c ? c : null;
        this.spec = cfg != null ? cfg.getConfigSpec() : ConfigSpec.builder().build();

        if (!inv.player.level().isClientSide() && cfg != null) {
            this.config = cfg.getConfig();
            if (this.config.owner() == null) {          // first opener owns an unclaimed device
                this.config.claim(inv.player.getUUID());
                cfg.onConfigChanged();                  // persist the claim
            }
            this.properties = backedBy(config, inv.player.getUUID());
        } else {
            this.config = null;
            this.properties = new SimpleContainerData(SIZE);
        }
        addDataSlots(properties);
    }

    public ConfigSpec spec() { return spec; }
    public BlockPos pos() { return pos; }
    public int prop(int i) { return properties.get(i); }

    public Configurable target() {
        BlockEntity be = player.level().getBlockEntity(pos);
        return be instanceof Configurable c ? c : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (config == null) return false; // server only
        boolean changed = true;
        if (id == B_CHANNEL_DOWN) config.setChannel(config.channel() - 1);
        else if (id == B_CHANNEL_UP) config.setChannel(config.channel() + 1);
        else if (id == B_OCTAVE_DOWN) config.setOctave(config.octave() - 1);
        else if (id == B_OCTAVE_UP) config.setOctave(config.octave() + 1);
        else if (id == B_REDSTONE) config.setRedstone(config.redstone().next());
        else if (id >= B_SIDE_BASE && id < B_SIDE_BASE + 6) config.cycleSide(id - B_SIDE_BASE);
        else if (id == B_TUNING_A_DOWN) adjustTuning(0, -1);
        else if (id == B_TUNING_A_UP) adjustTuning(0, 1);
        else if (id == B_TUNING_B_DOWN) adjustTuning(1, -1);
        else if (id == B_TUNING_B_UP) adjustTuning(1, 1);
        else if (id == B_SECURITY) {
            // Only the owner may change the security mode.
            if (config.owner() == null || config.owner().equals(player.getUUID()))
                config.setSecurity(config.security().next());
            else changed = false;
        }
        else changed = false;

        if (changed) {
            Configurable cfg = target();
            if (cfg != null) cfg.onConfigChanged();
            broadcastChanges();
        }
        return changed;
    }

    private void adjustTuning(int idx, int dir) {
        TuningParam p = spec.tuning(idx);
        if (p == null) return;
        if (idx == 0) config.setTuningA(p.adjust(config.tuningA(), dir));
        else config.setTuningB(p.adjust(config.tuningB(), dir));
    }

    private static ContainerData backedBy(BlockConfig c, java.util.UUID viewer) {
        return new ContainerData() {
            @Override public int get(int i) {
                return switch (i) {
                    case P_CHANNEL -> c.channel();
                    case P_OCTAVE -> c.octave();
                    case P_REDSTONE -> c.redstone().id();
                    case P_TUNING_A -> c.tuningA();
                    case P_TUNING_B -> c.tuningB();
                    case P_SECURITY -> c.security().id();
                    case P_OWNED -> (c.owner() == null || c.owner().equals(viewer)) ? 1 : 0;
                    default -> (i >= P_SIDE0 && i < P_SIDE0 + 6) ? c.side(i - P_SIDE0).id() : 0;
                };
            }
            @Override public void set(int i, int v) {
                switch (i) {
                    case P_CHANNEL -> c.setChannel(v);
                    case P_OCTAVE -> c.setOctave(v);
                    case P_REDSTONE -> c.setRedstone(RedstoneMode.byId(v));
                    case P_TUNING_A -> c.setTuningA(v);
                    case P_TUNING_B -> c.setTuningB(v);
                    case P_SECURITY -> c.setSecurity(SecurityMode.byId(v));
                    case P_OWNED -> {}
                    default -> { if (i >= P_SIDE0 && i < P_SIDE0 + 6) c.setSide(i - P_SIDE0, SideMode.byId(v)); }
                }
            }
            @Override public int getCount() { return SIZE; }
        };
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof Configurable
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
