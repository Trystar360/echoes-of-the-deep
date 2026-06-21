package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The two poles of one device. ATTRACT (centripetal) draws dropped items and XP
 * toward it — a vacuum; REPEL (centrifugal) throws mobs outward — a ward. Spends a
 * little Light while acting; glows while charged.
 */
public class PolarityFieldBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    private static final long BUFFER = 3_000;
    private static final long COST = 20;
    private static final int INTERVAL = 5;
    private static final double PULL = 0.28, PUSH = 0.45;

    /** Polarity Fields expose redstone behaviour, per-face I/O and an adjustable radius. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.radius", 2, 12, 1, 6)
            .build();

    private final ResonanceStorage buffer = new ResonanceStorage(BUFFER);
    private final BlockConfig config = new BlockConfig();
    private boolean attract;
    private int timer;

    public PolarityFieldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLARITY_FIELD, pos, state);
        config.applyDefaults(SPEC);
    }

    public boolean attract() { return attract; }

    public boolean toggle() {
        attract = !attract;
        setChanged();
        return attract;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, PolarityFieldBlockEntity be) {
        if (!(world instanceof ServerLevel sw)) return;
        boolean powered = sw.hasNeighborSignal(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        if (state.contains(BlockStateProperties.LIT) && state.get(BlockStateProperties.LIT) != active) {
            sw.setBlock(pos, state.setValue(BlockStateProperties.LIT, active), Block.UPDATE_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        Vec3 c = Vec3.ofCenter(pos);
        AABB box = new AABB(pos).inflate(be.config.tuningA());
        boolean acted = false;

        if (be.attract) {
            List<Entity> pickups = sw.getEntitiesOfClass(Entity.class, box,
                    e -> e instanceof ItemEntity || e instanceof ExperienceOrb);
            for (Entity e : pickups) {
                Vec3 dir = c.subtract(e.blockPosition());
                if (dir.lengthSqr() < 0.6) continue;
                e.setDeltaMovement(e.getDeltaMovement().multiply(0.4).add(dir.normalize().multiply(PULL)));
                e.hasImpulse = true;
                acted = true;
            }
        } else {
            List<LivingEntity> mobs = sw.getEntitiesOfClass(LivingEntity.class, box,
                    e -> !(e instanceof Player) && e.isAlive());
            for (LivingEntity e : mobs) {
                Vec3 dir = e.blockPosition().subtract(c);
                if (dir.lengthSqr() < 0.01) dir = new Vec3(0, 1, 0);
                e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().multiply(PUSH).add(0, 0.2, 0)));
                e.hasImpulse = true;
                acted = true;
            }
        }
        if (acted) { be.buffer.extract(COST, false); be.setChanged(); }
    }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() { return buffer.getCapacity() - buffer.getAmount(); }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putBoolean("attract", attract);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        attract = nbt.getBoolean("attract");
    }
}
