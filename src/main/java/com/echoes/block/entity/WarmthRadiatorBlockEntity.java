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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

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

    public static void tick(Level world, BlockPos pos, BlockState state, WarmthRadiatorBlockEntity be) {
        if (!(world instanceof ServerLevel sw)) return;
        boolean powered = sw.hasNeighborSignal(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        int radius = be.config.tuningA();
        if (state.contains(BlockStateProperties.LIT) && state.get(BlockStateProperties.LIT) != active) {
            sw.setBlock(pos, state.setValue(BlockStateProperties.LIT, active), Block.UPDATE_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        // Cook one dropped stack that has a smelting recipe.
        AABB box = new AABB(pos).inflate(radius);
        List<ItemEntity> drops = sw.getEntitiesOfClass(ItemEntity.class, box, e -> !e.getStack().isEmpty());
        for (ItemEntity e : drops) {
            ItemStack stack = e.getStack();
            Optional<? extends net.minecraft.world.item.crafting.RecipeHolder<SmeltingRecipe>> m =
                    sw.recipeAccess().getFirstMatch(RecipeType.SMELTING, new SingleRecipeInput(stack), sw);
            if (m.isEmpty()) continue;
            ItemStack result = m.get().value().craft(new SingleRecipeInput(stack), sw.registryAccess());
            if (result.isEmpty()) continue;
            ItemStack out = result.copy();
            out.setCount(stack.getCount() * result.getCount());
            e.setStack(out);
            be.buffer.extract(COST, false);
            be.setChanged();
            break;
        }

        // Thaw a little snow / ice.
        RandomSource rng = sw.getRandom();
        BlockPos p = pos.add(rng.nextInt(radius * 2 + 1) - radius, rng.nextInt(3) - 1,
                rng.nextInt(radius * 2 + 1) - radius);
        BlockState s = sw.getBlockState(p);
        if (s.is(Blocks.SNOW) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.POWDER_SNOW)) {
            sw.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        } else if (s.is(Blocks.ICE) || s.is(Blocks.FROSTED_ICE)) {
            sw.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
        }
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
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
    }
}
