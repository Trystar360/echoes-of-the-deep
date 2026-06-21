package com.echoes.transmute;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player <b>Bound Light</b> account — the EMC ledger. Each player has a pool of
 * banked Light Value and a set of <i>attuned</i> items (the tones they've "learned" by
 * dissolving). Both the Transmutation Table (block) and the Transmutation Tablet (item)
 * are windows into this one account, so your Light and knowledge follow you. Stored once
 * on the overworld; survives restarts.
 */
public class TransmutationState extends SavedData {
    public static final String KEY = "echoes_transmutation";

    /** One player's banked Light + learned tones. */
    public static final class Account {
        public long light;
        public final Set<Identifier> attuned = new HashSet<>();
    }

    private final Map<UUID, Account> accounts = new HashMap<>();

    public static final Type<TransmutationState> TYPE =
            new Type<>(TransmutationState::new, TransmutationState::fromNbt, null);

    public Account of(UUID id) {
        return accounts.computeIfAbsent(id, k -> new Account());
    }

    /** The state for this server (always the overworld copy, so it's dimension-wide). */
    public static TransmutationState get(ServerLevel world) {
        return world.getServer().getOverworld().getPersistentStateManager()
                .getOrCreate(TYPE, KEY);
    }

    public static TransmutationState fromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        TransmutationState s = new TransmutationState();
        ListTag list = nbt.getList("accounts", Tag.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag a = list.getCompound(i);
            Account acc = new Account();
            acc.light = a.getLong("light");
            for (Tag e : a.getList("attuned", Tag.STRING_TYPE)) {
                Identifier id = Identifier.tryParse(e.asString());
                if (id != null) acc.attuned.add(id);
            }
            s.accounts.put(a.getUuid("id"), acc);
        }
        return s;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Account> e : accounts.entrySet()) {
            Account acc = e.getValue();
            if (acc.light == 0 && acc.attuned.isEmpty()) continue;
            CompoundTag a = new CompoundTag();
            a.putUuid("id", e.getKey());
            a.putLong("light", acc.light);
            ListTag tones = new ListTag();
            for (Identifier id : acc.attuned) tones.add(net.minecraft.nbt.NbtString.of(id.toString()));
            a.put("attuned", tones);
            list.add(a);
        }
        nbt.put("accounts", list);
        return nbt;
    }
}
