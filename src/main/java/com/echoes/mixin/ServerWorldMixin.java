package com.echoes.mixin;

import com.echoes.energy.ResonanceEvents;
import com.echoes.energy.ResonanceSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns ambient world sound into stored Resonance. When the server plays a sound
 * listed in {@code resonance_sources.json}, the nearest Resonator captures its RU
 * value — so note blocks, anvils, bells, explosions, and thunder all charge the grid.
 *
 * <p>26.1: {@code playSound} is declared on {@link Level} (not {@code ServerLevel}),
 * so we mix into {@code Level} and guard on the server side at runtime.
 */
@Mixin(Level.class)
public abstract class ServerWorldMixin {

    @Inject(
            method = "playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FF)V",
            at = @At("HEAD"))
    private void echoes$captureSound(Entity except, double x, double y, double z,
                                     Holder<SoundEvent> sound, SoundSource category,
                                     float volume, float pitch, CallbackInfo ci) {
        if (((Level) (Object) this) instanceof ServerLevel sw) {
            int ru = ResonanceSources.ru(sound);
            if (ru > 0) {
                ResonanceEvents.emit(sw, new Vec3(x, y, z), ru);
            }
        }
    }
}
