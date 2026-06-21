package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;

/**
 * Bridges the wired Resonance grid and a wireless channel. Placed next to a
 * Tuning Conduit it joins the wired network as a STORAGE node; its buffer is
 * simultaneously a wireless RU endpoint. SEND beams the buffer's RU onto the
 * channel (refilled by the wired grid); RECEIVE pulls channel RU into the buffer
 * (drained by the wired grid). It is the formal seam between the two transport
 * systems.
 */
public class ConduitCouplerBlockEntity extends AbstractChannelDeviceBlockEntity implements ResonanceNode {

    public static final long CAPACITY = 50_000;

    private final ResonanceStorage buffer = new ResonanceStorage(CAPACITY);
    private RelayMode mode = RelayMode.SEND;

    public ConduitCouplerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDUIT_COUPLER, pos, state);
    }

    public RelayMode mode() { return mode; }

    public void cycleMode() {
        mode = mode.next();
        sync();
    }

    public ResonanceStorage storage() { return buffer; }

    // --- wireless side: this block's own buffer is the channel endpoint ---
    @Override public RelayMode transportMode() { return mode; }
    @Override public ResonanceNode wirelessEnergy() { return this; }

    // --- wired side: a STORAGE node the conduit network fills/drains ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.STORAGE); }
    @Override public long extract(long max, boolean simulate) { return buffer.extract(max, simulate); }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    @Override
    protected void writeExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        nbt.putInt("mode", mode.ordinal());
        buffer.writeNbt(nbt);
    }

    @Override
    protected void readExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        mode = RelayMode.byId(nbt.getInt("mode"));
        buffer.readNbt(nbt);
    }
}
