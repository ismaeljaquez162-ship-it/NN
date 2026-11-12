package com.saorpg.logic;

import com.saorpg.data.MasteryBranch;
import com.saorpg.data.ModAttachments;
import com.saorpg.data.PlayerRpgData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.EnumSet;
import java.util.Set;

public final class RpgEvents {
	private static final int ATTACK_ENERGY_COST = 3;
	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	public static void register() {
		// Al entrar / reaparecer: aplicar atributos y mandar estado al cliente.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			RpgService.applyAttributes(handler.getPlayer());
			RpgService.sync(handler.getPlayer());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			RpgService.applyAttributes(newPlayer);
			RpgService.sync(newPlayer);
		});

		// Nivel a los mobs hostiles.
		ServerEntityEvents.ENTITY_LOAD.register(MobLevels::onEntityLoad);

		// XP al matar.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;
			Integer mobLevel = entity.getAttached(ModAttachments.MOB_LEVEL);
			if (mobLevel == null) return;
			PlayerRpgData d = RpgService.data(player);
			RpgService.addXp(player, RpgMath.killXp(mobLevel, d.level));
			// Bonus de maestria del arma en mano.
			MasteryBranch weapon = GearRules.branchOf(player.getMainHandStack());
			RpgService.addMasteryXp(player, weapon, 4 + mobLevel);
		});

		// Maestria por uso: pegar con un arma y recibir dano con armadura.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (source.getAttacker() instanceof ServerPlayerEntity attacker && entity != attacker) {
				RpgService.addMasteryXp(attacker, GearRules.branchOf(attacker.getMainHandStack()), 1);
			}
			if (entity instanceof ServerPlayerEntity victim) {
				Set<MasteryBranch> worn = EnumSet.noneOf(MasteryBranch.class);
				for (EquipmentSlot slot : ARMOR_SLOTS) {
					MasteryBranch b = GearRules.branchOf(victim.getEquippedStack(slot));
					if (b != null) worn.add(b);
				}
				for (MasteryBranch b : worn) RpgService.addMasteryXp(victim, b, 1);
			}
		});

		// Atacar cuesta energia (solo servidor, no en creativo).
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() || player.isSpectator() || player.isCreative()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
			if (!RpgService.spendEnergy(sp, ATTACK_ENERGY_COST)) {
				sp.sendMessage(Text.translatable("message.sao_rpg.no_energy").formatted(Formatting.RED), true);
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

		// Regeneracion/gasto de energia y sincronizacion.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				if (p.age % 10 == 0) RpgService.tickEnergy(p);
				if (RpgService.data(p).dirty) RpgService.sync(p);
			}
		});
	}

	/** Utilidad usada por el mixin de dano: cuantos niveles faltan para usar el objeto (0 si cumple). */
	public static int missingLevels(ServerPlayerEntity p, ItemStack stack) {
		MasteryBranch branch = GearRules.branchOf(stack);
		if (branch == null) return 0;
		int have = RpgService.data(p).masteryLevel[branch.ordinal()];
		return Math.max(0, GearRules.requiredLevel(stack) - have);
	}

	public static float incomingDamageMultiplier(ServerPlayerEntity victim) {
		float extra = 0f;
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			extra += RpgMath.armorPenaltyPerPiece(missingLevels(victim, victim.getEquippedStack(slot)));
		}
		return 1.0f + extra;
	}

	public static float outgoingDamageMultiplier(ServerPlayerEntity attacker) {
		return RpgMath.weaponPenalty(missingLevels(attacker, attacker.getMainHandStack()));
	}

	/** Se usa desde el mixin para evitar importar tipos de eventos alli. */
	public static boolean isPlayerSource(DamageSource source) {
		return source.getAttacker() instanceof ServerPlayerEntity;
	}

	private RpgEvents() {
	}
}
