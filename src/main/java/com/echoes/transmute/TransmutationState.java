package com.echoes.transmute;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

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
public class TransmutationState extends PersistentState {
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
    public static TransmutationState get(ServerWorld world) {
        return world.getServer().getOverworld().getPersistentStateManager()
                .getOrCreate(TYPE, KEY);
    }

    public static TransmutationState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        TransmutationState s = new TransmutationState();
        NbtList list = nbt.getList("accounts", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound a = list.getCompound(i);
            Account acc = new Account();
            acc.light = a.getLong("light");
            for (NbtElement e : a.getList("attuned", NbtElement.STRING_TYPE)) {
                Identifier id = Identifier.tryParse(e.asString());
                if (id != null) acc.attuned.add(id);
            }
            s.accounts.put(a.getUuid("id"), acc);
        }
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, Account> e : accounts.entrySet()) {
            Account acc = e.getValue();
            if (acc.light == 0 && acc.attuned.isEmpty()) continue;
            NbtCompound a = new NbtCompound();
            a.putUuid("id", e.getKey());
            a.putLong("light", acc.light);
            NbtList tones = new NbtList();
            for (Identifier id : acc.attuned) tones.add(net.minecraft.nbt.NbtString.of(id.toString()));
            a.put("attuned", tones);
            list.add(a);
        }
        nbt.put("accounts", list);
        return nbt;
    }
}
