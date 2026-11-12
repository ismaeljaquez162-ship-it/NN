package com.saorpg.data;

/**
 * Ramas de equipamiento (estilo Albion Online).
 * Cada rama tiene su propio nivel de maestria. Para usar un objeto a plena potencia
 * necesitas el nivel de maestria de SU rama, no importa "que clase eres".
 */
public enum MasteryBranch {
	SWORD("sword", false),
	AXE("axe", false),
	SPEAR("spear", false),
	BOW("bow", false),
	CROSSBOW("crossbow", false),
	TRIDENT("trident", false),
	MACE("mace", false),
	LIGHT_ARMOR("light_armor", true),
	MEDIUM_ARMOR("medium_armor", true),
	HEAVY_ARMOR("heavy_armor", true);

	public static final int COUNT = values().length;

	public final String id;
	public final boolean armor;

	MasteryBranch(String id, boolean armor) {
		this.id = id;
		this.armor = armor;
	}

	public String translationKey() {
		return "branch.sao_rpg." + id;
	}

	public static MasteryBranch byId(String id) {
		for (MasteryBranch b : values()) {
			if (b.id.equalsIgnoreCase(id)) return b;
		}
		return null;
	}
}
