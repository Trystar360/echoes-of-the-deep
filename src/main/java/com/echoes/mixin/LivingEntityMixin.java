package com.echoes.mixin;

import com.echoes.energy.ResonanceEvents;
import com.echoes.item.ResonanceThrustersItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ambient RU on death, plus fall-damage immunity for Resonance Thruster pilots. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void echoes$onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel sw) {
            ResonanceEvents.emit(sw, self.position(), 25);
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void echoes$thrusterFallImmunity(double fallDistance, float damageMultiplier,
                                             DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (ResonanceThrustersItem.shieldsFall((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
