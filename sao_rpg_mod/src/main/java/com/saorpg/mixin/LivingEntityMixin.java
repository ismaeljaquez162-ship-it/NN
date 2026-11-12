package com.saorpg.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.saorpg.logic.RpgEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Sistema de "clase libre" estilo Albion:
 *  - Si atacas con un arma cuya maestria no tienes, haces menos dano.
 *  - Si llevas armadura cuya maestria no tienes, recibes mas dano.
 *
 * En 1.21.11 damage(...) solo se ejecuta en el servidor (recibe un ServerWorld).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@WrapMethod(method = "damage")
	private boolean saoRpg$scaleDamage(ServerWorld world, DamageSource source, float amount, Operation<Boolean> original) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
			amount *= RpgEvents.outgoingDamageMultiplier(attacker);
		}
		if (self instanceof ServerPlayerEntity victim) {
			amount *= RpgEvents.incomingDamageMultiplier(victim);
		}
		return original.call(world, source, amount);
	}
}
