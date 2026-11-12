package com.saorpg.logic;

/** Todas las formulas de balance en un solo sitio para ajustarlas facil. */
public final class RpgMath {
	public static final int MAX_LEVEL = 100;
	public static final int MAX_MASTERY = 100;

	/** XP necesaria para pasar de "level" a "level+1". */
	public static int xpForNext(int level) {
		return (int) (80 * Math.pow(level, 1.4));
	}

	public static int maxEnergy(int level) {
		return 100 + level * 2;
	}

	/** Vida extra por nivel (suma a los 20 base). */
	public static double bonusHealth(int level) {
		return (level - 1) * 1.0;
	}

	/** Dano de ataque extra por nivel. */
	public static double bonusAttack(int level) {
		return (level - 1) * 0.1;
	}

	public static int masteryXpForNext(int masteryLevel) {
		return (int) (40 * Math.pow(masteryLevel + 1, 1.4));
	}

	/** XP que da matar un mob de nivel mobLevel siendo el jugador de nivel playerLevel. */
	public static int killXp(int mobLevel, int playerLevel) {
		double base = 8 + mobLevel * 4;
		int diff = mobLevel - playerLevel;
		double factor = Math.max(0.1, Math.min(2.0, 1.0 + diff * 0.1));
		return Math.max(1, (int) Math.round(base * factor));
	}

	/** Nivel de un mob segun distancia al spawn (cada 100 bloques +1) y dimension. */
	public static int mobLevel(double distanceFromSpawn, int dimensionBonus, int randomOffset) {
		int lvl = 1 + (int) (distanceFromSpawn / 100.0) + dimensionBonus + randomOffset;
		return Math.max(1, Math.min(MAX_LEVEL, lvl));
	}

	/** Multiplicador de dano de un arma si faltan "gap" niveles de maestria (min 35%). */
	public static float weaponPenalty(int gap) {
		if (gap <= 0) return 1.0f;
		return Math.max(0.35f, 1.0f - gap * 0.04f);
	}

	/** Dano extra recibido por pieza de armadura sin maestria suficiente (max +15% por pieza). */
	public static float armorPenaltyPerPiece(int gap) {
		if (gap <= 0) return 0f;
		return Math.min(0.15f, gap * 0.015f);
	}

	private RpgMath() {
	}
}
