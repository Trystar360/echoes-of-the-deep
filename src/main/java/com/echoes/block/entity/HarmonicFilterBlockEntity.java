package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Constrains item transport on its channel to a whitelist. Right-click with an
 * item to add its type; empty-hand right-click clears the list. Multiple filters
 * on one channel union their whitelists.
 */
public class HarmonicFilterBlockEntity extends AbstractChannelDeviceBlockEntity {

    private static final int MAX_ENTRIES = 9;
    private final Set<Item> whitelist = new LinkedHashSet<>();

    public HarmonicFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARMONIC_FILTER, pos, state);
    }

    @Override public @Nullable Set<Item> itemWhitelist() {
        return whitelist.isEmpty() ? null : whitelist;
    }

    /** @return new whitelist size, or -1 if it was already full. */
    public int addItem(Item item) {
        if (whitelist.size() >= MAX_ENTRIES && !whitelist.contains(item)) return -1;
        whitelist.add(item);
        sync();
        return whitelist.size();
    }

    public void clearFilter() {
        whitelist.clear();
        sync();
    }

    public int size() { return whitelist.size(); }

    @Override
    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (Item item : whitelist) list.add(NbtString.of(Registries.ITEM.getId(item).toString()));
        nbt.put("whitelist", list);
    }

    @Override
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        whitelist.clear();
        NbtList list = nbt.getList("whitelist", NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getString(i));
            if (id != null && Registries.ITEM.containsId(id)) whitelist.add(Registries.ITEM.get(id));
        }
    }
}
