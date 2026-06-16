package com.echoes.mixin;

import com.echoes.energy.ResonanceEvents;
import com.echoes.energy.ResonanceSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns ambient world sound into stored Resonance. When the server plays a sound
 * listed in {@code resonance_sources.json}, the nearest Resonator captures its RU
 * value — so note blocks, anvils, bells, explosions, and thunder all charge the grid.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(
            method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("HEAD"))
    private void echoes$captureSound(PlayerEntity except, double x, double y, double z,
                                     RegistryEntry<SoundEvent> sound, SoundCategory category,
                                     float volume, float pitch, long seed, CallbackInfo ci) {
        int ru = ResonanceSources.ru(sound);
        if (ru > 0) {
            ResonanceEvents.emit((ServerWorld) (Object) this, new Vec3d(x, y, z), ru);
        }
    }
}
