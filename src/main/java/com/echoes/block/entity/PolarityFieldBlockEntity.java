package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
        markDirty();
        return attract;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PolarityFieldBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;
        boolean powered = sw.isReceivingRedstonePower(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        if (state.contains(Properties.LIT) && state.get(Properties.LIT) != active) {
            sw.setBlockState(pos, state.with(Properties.LIT, active), Block.NOTIFY_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        Vec3d c = Vec3d.ofCenter(pos);
        Box box = new Box(pos).expand(be.config.tuningA());
        boolean acted = false;

        if (be.attract) {
            List<Entity> pickups = sw.getEntitiesByClass(Entity.class, box,
                    e -> e instanceof ItemEntity || e instanceof ExperienceOrbEntity);
            for (Entity e : pickups) {
                Vec3d dir = c.subtract(e.getPos());
                if (dir.lengthSquared() < 0.6) continue;
                e.setVelocity(e.getVelocity().multiply(0.4).add(dir.normalize().multiply(PULL)));
                e.velocityModified = true;
                acted = true;
            }
        } else {
            List<LivingEntity> mobs = sw.getEntitiesByClass(LivingEntity.class, box,
                    e -> !(e instanceof PlayerEntity) && e.isAlive());
            for (LivingEntity e : mobs) {
                Vec3d dir = e.getPos().subtract(c);
                if (dir.lengthSquared() < 0.01) dir = new Vec3d(0, 1, 0);
                e.setVelocity(e.getVelocity().add(dir.normalize().multiply(PUSH).add(0, 0.2, 0)));
                e.velocityModified = true;
                acted = true;
            }
        }
        if (acted) { be.buffer.extract(COST, false); be.markDirty(); }
    }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() { return buffer.getCapacity() - buffer.getAmount(); }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Text configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putBoolean("attract", attract);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        attract = nbt.getBoolean("attract");
    }
}
