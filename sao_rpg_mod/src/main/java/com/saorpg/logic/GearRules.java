package com.saorpg.logic;

import com.saorpg.data.MasteryBranch;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Decide a que rama pertenece un objeto y que nivel de maestria pide.
 *
 * Se basa en el ID del objeto ("minecraft:iron_sword", "minecraft:diamond_chestplate"...)
 * en vez de en clases de Java, porque las clases de items cambian mucho entre versiones.
 * Los objetos de otros mods no tienen rama: no se restringen.
 */
public final class GearRules {
	private static final String[] ARMOR_SUFFIXES = {"_helmet", "_chestplate", "_leggings", "_boots"};

	/** Rama del objeto, o null si no aplica. */
	public static MasteryBranch branchOf(ItemStack stack) {
		String path = vanillaPath(stack);
		if (path == null) return null;

		if (path.endsWith("_sword")) return MasteryBranch.SWORD;
		if (path.endsWith("_axe")) return MasteryBranch.AXE;
		if (path.endsWith("_spear")) return MasteryBranch.SPEAR;
		switch (path) {
			case "bow": return MasteryBranch.BOW;
			case "crossbow": return MasteryBranch.CROSSBOW;
			case "trident": return MasteryBranch.TRIDENT;
			case "mace": return MasteryBranch.MACE;
			default: break;
		}

		String armorMaterial = armorMaterial(path);
		if (armorMaterial != null) {
			return switch (armorMaterial) {
				case "leather", "golden" -> MasteryBranch.LIGHT_ARMOR;
				case "diamond", "netherite" -> MasteryBranch.HEAVY_ARMOR;
				default -> MasteryBranch.MEDIUM_ARMOR; // copper, chainmail, iron, turtle
			};
		}
		return null;
	}

	/** Nivel de maestria (de su rama) que pide el objeto. */
	public static int requiredLevel(ItemStack stack) {
		String path = vanillaPath(stack);
		if (path == null) return 0;

		switch (path) {
			case "bow": return 0;
			case "crossbow": return 3;
			case "trident": return 12;
			case "mace": return 25;
			default: break;
		}

		String armorMaterial = armorMaterial(path);
		if (armorMaterial != null) return tier(armorMaterial);

		for (String suffix : new String[]{"_sword", "_axe", "_spear"}) {
			if (path.endsWith(suffix)) {
				return tier(path.substring(0, path.length() - suffix.length()));
			}
		}
		return 0;
	}

	/** Nivel de maestria segun el material. */
	private static int tier(String material) {
		return switch (material) {
			case "wooden", "leather", "turtle" -> 0;
			case "stone", "golden" -> 3;
			case "copper" -> 4;
			case "chainmail" -> 5;
			case "iron" -> 8;
			case "diamond" -> 18;
			case "netherite" -> 30;
			default -> 0;
		};
	}

	/** Devuelve el material si es una pieza de armadura de jugador ("iron_helmet" -> "iron"), si no null. */
	private static String armorMaterial(String path) {
		for (String suffix : ARMOR_SUFFIXES) {
			if (path.endsWith(suffix)) return path.substring(0, path.length() - suffix.length());
		}
		return null;
	}

	private static String vanillaPath(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		Identifier id = Registries.ITEM.getId(stack.getItem());
		if (!"minecraft".equals(id.getNamespace())) return null;
		return id.getPath();
	}

	private GearRules() {
	}
}
