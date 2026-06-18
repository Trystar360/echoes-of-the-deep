package com.echoes.mixin;

import com.echoes.energy.ResonanceEvents;
import com.echoes.item.ResonanceThrustersItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ambient RU on death, plus fall-damage immunity for Resonance Thruster pilots. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void echoes$onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getWorld() instanceof ServerWorld sw) {
            ResonanceEvents.emit(sw, self.getPos(), 25);
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void echoes$thrusterFallImmunity(float fallDistance, float damageMultiplier,
                                             DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (ResonanceThrustersItem.shieldsFall((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
