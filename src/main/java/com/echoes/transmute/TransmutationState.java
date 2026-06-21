package com.echoes.transmute;

import com.echoes.EchoesMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/**
 * Per-player <b>Bound Light</b> account — the EMC ledger. Each player has a pool of
 * banked Light Value and a set of <i>attuned</i> items (the tones they've "learned" by
 * dissolving). Both the Transmutation Table (block) and the Transmutation Tablet (item)
 * are windows into this one account. Stored once on the overworld; survives restarts.
 *
 * <p>26.1: {@link SavedData} is codec-serialized via a {@link SavedDataType}.
 */
public class TransmutationState extends SavedData {

    /** One player's banked Light + learned tones. */
    public static final class Account {
        public long light;
        public final Set<Identifier> attuned = new HashSet<>();
    }

    private final Map<UUID, Account> accounts = new HashMap<>();

    private static final Codec<Account> ACCOUNT_CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.LONG.fieldOf("light").orElse(0L).forGetter(a -> a.light),
            Identifier.CODEC.listOf().fieldOf("attuned").orElse(List.of())
                    .forGetter(a -> List.copyOf(a.attuned))
    ).apply(in, (light, attuned) -> {
        Account a = new Account();
        a.light = light;
        a.attuned.addAll(attuned);
        return a;
    }));

    public static final Codec<TransmutationState> CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, ACCOUNT_CODEC).xmap(map -> {
                TransmutationState s = new TransmutationState();
                map.forEach((id, acc) -> {
                    if (acc.light != 0 || !acc.attuned.isEmpty()) s.accounts.put(id, acc);
                });
                return s;
            }, s -> {
                Map<UUID, Account> out = new HashMap<>();
                s.accounts.forEach((id, acc) -> {
                    if (acc.light != 0 || !acc.attuned.isEmpty()) out.put(id, acc);
                });
                return out;
            });

    public static final SavedDataType<TransmutationState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "transmutation"),
            TransmutationState::new, CODEC, DataFixTypes.LEVEL);

    public Account of(UUID id) {
        return accounts.computeIfAbsent(id, k -> new Account());
    }

    /** The state for this server (always the overworld copy, so it's dimension-wide). */
    public static TransmutationState get(ServerLevel world) {
        return world.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
