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
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Radiates heat: a campfire of Light. Cooks dropped items on the ground (vanilla
 * smelting), melts snow and ice nearby, and glows while charged.
 */
public class WarmthRadiatorBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    private static final long BUFFER = 3_000;
    private static final long COST = 60;        // Light per cooked stack
    private static final int INTERVAL = 10;

    /** Warmth Radiators expose redstone behaviour, per-face I/O and an adjustable radius. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.radius", 1, 8, 1, 4)
            .build();

    private final ResonanceStorage buffer = new ResonanceStorage(BUFFER);
    private final BlockConfig config = new BlockConfig();
    private int timer;

    public WarmthRadiatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARMTH_RADIATOR, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WarmthRadiatorBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;
        boolean powered = sw.isReceivingRedstonePower(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        int radius = be.config.tuningA();
        if (state.contains(Properties.LIT) && state.get(Properties.LIT) != active) {
            sw.setBlockState(pos, state.with(Properties.LIT, active), Block.NOTIFY_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        // Cook one dropped stack that has a smelting recipe.
        Box box = new Box(pos).expand(radius);
        List<ItemEntity> drops = sw.getEntitiesByClass(ItemEntity.class, box, e -> !e.getStack().isEmpty());
        for (ItemEntity e : drops) {
            ItemStack stack = e.getStack();
            Optional<? extends net.minecraft.recipe.RecipeEntry<SmeltingRecipe>> m =
                    sw.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(stack), sw);
            if (m.isEmpty()) continue;
            ItemStack result = m.get().value().craft(new SingleStackRecipeInput(stack), sw.getRegistryManager());
            if (result.isEmpty()) continue;
            ItemStack out = result.copy();
            out.setCount(stack.getCount() * result.getCount());
            e.setStack(out);
            be.buffer.extract(COST, false);
            be.markDirty();
            break;
        }

        // Thaw a little snow / ice.
        Random rng = sw.getRandom();
        BlockPos p = pos.add(rng.nextInt(radius * 2 + 1) - radius, rng.nextInt(3) - 1,
                rng.nextInt(radius * 2 + 1) - radius);
        BlockState s = sw.getBlockState(p);
        if (s.isOf(Blocks.SNOW) || s.isOf(Blocks.SNOW_BLOCK) || s.isOf(Blocks.POWDER_SNOW)) {
            sw.setBlockState(p, Blocks.AIR.getDefaultState());
        } else if (s.isOf(Blocks.ICE) || s.isOf(Blocks.FROSTED_ICE)) {
            sw.setBlockState(p, Blocks.WATER.getDefaultState());
        }
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
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
    }
}
