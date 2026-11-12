package com.saorpg.logic;

import com.saorpg.data.ModAttachments;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/** Asigna nivel a los mobs hostiles y escala su vida y dano. */
public final class MobLevels {
	private static final Identifier HEALTH_ID = Identifier.of("sao_rpg", "mob_health");
	private static final Identifier ATTACK_ID = Identifier.of("sao_rpg", "mob_attack");

	public static void onEntityLoad(net.minecraft.entity.Entity entity, ServerWorld world) {
		if (!(entity instanceof MobEntity mob) || !(mob instanceof Monster)) return;
		if (mob.getAttached(ModAttachments.MOB_LEVEL) != null) return; // ya tiene nivel (cargado del disco)

		// Distancia al centro del mundo (0,0). Cada 100 bloques sube 1 nivel.
		double dist = Math.sqrt(mob.getX() * mob.getX() + mob.getZ() * mob.getZ());

		int dimBonus = 0;
		if (world.getRegistryKey() == World.NETHER) dimBonus = 15;
		else if (world.getRegistryKey() == World.END) dimBonus = 30;

		int random = world.getRandom().nextInt(4) - 1; // -1..+2
		int level = RpgMath.mobLevel(dist, dimBonus, random);
		mob.setAttached(ModAttachments.MOB_LEVEL, level);

		EntityAttributeInstance hp = mob.getAttributeInstance(EntityAttributes.MAX_HEALTH);
		if (hp != null && !hp.hasModifier(HEALTH_ID)) {
			hp.addPersistentModifier(new EntityAttributeModifier(HEALTH_ID, 0.10 * (level - 1), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
			mob.setHealth(mob.getMaxHealth());
		}
		EntityAttributeInstance atk = mob.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
		if (atk != null && !atk.hasModifier(ATTACK_ID)) {
			atk.addPersistentModifier(new EntityAttributeModifier(ATTACK_ID, 0.04 * (level - 1), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}

		if (!mob.hasCustomName()) {
			mob.setCustomName(Text.literal("[Lv." + level + "] ").formatted(colorFor(level)).append(mob.getType().getName().copy().formatted(Formatting.WHITE)));
			mob.setCustomNameVisible(true);
		}
	}

	private static Formatting colorFor(int level) {
		if (level < 10) return Formatting.GREEN;
		if (level < 25) return Formatting.YELLOW;
		if (level < 40) return Formatting.GOLD;
		if (level < 60) return Formatting.RED;
		return Formatting.DARK_PURPLE;
	}

	private MobLevels() {
	}
}
