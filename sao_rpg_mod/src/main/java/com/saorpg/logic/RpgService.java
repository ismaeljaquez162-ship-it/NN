package com.saorpg.logic;

import com.saorpg.data.MasteryBranch;
import com.saorpg.data.ModAttachments;
import com.saorpg.data.PlayerRpgData;
import com.saorpg.net.SyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** Logica del lado servidor: dar XP, subir de nivel, maestrias, energia y sincronizacion. */
public final class RpgService {
	private static final Identifier HEALTH_ID = Identifier.of("sao_rpg", "level_health");
	private static final Identifier ATTACK_ID = Identifier.of("sao_rpg", "level_attack");

	public static PlayerRpgData data(ServerPlayerEntity p) {
		return p.getAttachedOrCreate(ModAttachments.RPG);
	}

	// ---------------------------------------------------------------- nivel / xp

	public static void addXp(ServerPlayerEntity p, int amount) {
		PlayerRpgData d = data(p);
		if (amount <= 0 || d.level >= RpgMath.MAX_LEVEL) return;
		d.xp += amount;
		boolean levelUp = false;
		while (d.level < RpgMath.MAX_LEVEL && d.xp >= RpgMath.xpForNext(d.level)) {
			d.xp -= RpgMath.xpForNext(d.level);
			d.level++;
			levelUp = true;
		}
		if (d.level >= RpgMath.MAX_LEVEL) d.xp = 0;
		if (levelUp) onLevelUp(p, d);
		d.dirty = true;
	}

	public static void setLevel(ServerPlayerEntity p, int level) {
		PlayerRpgData d = data(p);
		d.level = Math.max(1, Math.min(RpgMath.MAX_LEVEL, level));
		d.xp = 0;
		d.energy = RpgMath.maxEnergy(d.level);
		applyAttributes(p);
		d.dirty = true;
	}

	private static void onLevelUp(ServerPlayerEntity p, PlayerRpgData d) {
		applyAttributes(p);
		d.energy = RpgMath.maxEnergy(d.level);
		p.setHealth(p.getMaxHealth());
		p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("LEVEL UP!").formatted(Formatting.GOLD, Formatting.BOLD)));
		p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Nivel " + d.level).formatted(Formatting.YELLOW)));
		p.getEntityWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}

	/** Aplica vida y ataque extra segun el nivel. */
	public static void applyAttributes(ServerPlayerEntity p) {
		PlayerRpgData d = data(p);
		EntityAttributeInstance hp = p.getAttributeInstance(EntityAttributes.MAX_HEALTH);
		if (hp != null) {
			hp.removeModifier(HEALTH_ID);
			hp.addPersistentModifier(new EntityAttributeModifier(HEALTH_ID, RpgMath.bonusHealth(d.level), EntityAttributeModifier.Operation.ADD_VALUE));
		}
		EntityAttributeInstance atk = p.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
		if (atk != null) {
			atk.removeModifier(ATTACK_ID);
			atk.addPersistentModifier(new EntityAttributeModifier(ATTACK_ID, RpgMath.bonusAttack(d.level), EntityAttributeModifier.Operation.ADD_VALUE));
		}
	}

	// ---------------------------------------------------------------- maestrias

	public static void addMasteryXp(ServerPlayerEntity p, MasteryBranch branch, int amount) {
		if (branch == null || amount <= 0) return;
		PlayerRpgData d = data(p);
		int i = branch.ordinal();
		if (d.masteryLevel[i] >= RpgMath.MAX_MASTERY) return;
		d.masteryXp[i] += amount;
		boolean up = false;
		while (d.masteryLevel[i] < RpgMath.MAX_MASTERY && d.masteryXp[i] >= RpgMath.masteryXpForNext(d.masteryLevel[i])) {
			d.masteryXp[i] -= RpgMath.masteryXpForNext(d.masteryLevel[i]);
			d.masteryLevel[i]++;
			up = true;
		}
		if (d.masteryLevel[i] >= RpgMath.MAX_MASTERY) d.masteryXp[i] = 0;
		if (up) {
			p.sendMessage(Text.translatable("message.sao_rpg.mastery_up", Text.translatable(branch.translationKey()), d.masteryLevel[i])
				.formatted(Formatting.AQUA), true);
		}
		d.dirty = true;
	}

	public static void setMastery(ServerPlayerEntity p, MasteryBranch branch, int level) {
		PlayerRpgData d = data(p);
		d.masteryLevel[branch.ordinal()] = Math.max(0, Math.min(RpgMath.MAX_MASTERY, level));
		d.masteryXp[branch.ordinal()] = 0;
		d.dirty = true;
	}

	public static void reset(ServerPlayerEntity p) {
		p.setAttached(ModAttachments.RPG, new PlayerRpgData());
		applyAttributes(p);
		sync(p);
	}

	// ---------------------------------------------------------------- energia

	/** Se llama cada 10 ticks por jugador. */
	public static void tickEnergy(ServerPlayerEntity p) {
		PlayerRpgData d = data(p);
		int max = RpgMath.maxEnergy(d.level);
		int before = d.energy;
		if (d.energy > max) d.energy = max;
		if (p.isSprinting() && !p.isCreative()) {
			d.energy = Math.max(0, d.energy - 3);
			if (d.energy <= 0) p.setSprinting(false);
		} else {
			d.energy = Math.min(max, d.energy + 2);
		}
		if (d.energy != before) d.dirty = true;
	}

	/** Intenta gastar energia. Devuelve false si no hay suficiente. */
	public static boolean spendEnergy(ServerPlayerEntity p, int cost) {
		PlayerRpgData d = data(p);
		if (d.energy < cost) return false;
		d.energy -= cost;
		d.dirty = true;
		return true;
	}

	// ---------------------------------------------------------------- red

	public static void sync(ServerPlayerEntity p) {
		PlayerRpgData d = data(p);
		d.dirty = false;
		ServerPlayNetworking.send(p, SyncPayload.from(d));
	}

	private RpgService() {
	}
}
