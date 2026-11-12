package com.saorpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/** Datos RPG persistentes de un jugador (se guardan con el jugador via Data Attachment). */
public class PlayerRpgData {
	public int level = 1;
	public int xp = 0;
	public int energy = 100;
	public int[] masteryLevel = new int[MasteryBranch.COUNT];
	public int[] masteryXp = new int[MasteryBranch.COUNT];

	/** No se guarda: indica que hay que sincronizar con el cliente. */
	public transient boolean dirty = true;

	public PlayerRpgData() {
	}

	private PlayerRpgData(int level, int xp, int energy, List<Integer> mLvl, List<Integer> mXp) {
		this.level = Math.max(1, level);
		this.xp = Math.max(0, xp);
		this.energy = Math.max(0, energy);
		for (int i = 0; i < MasteryBranch.COUNT; i++) {
			if (i < mLvl.size()) this.masteryLevel[i] = mLvl.get(i);
			if (i < mXp.size()) this.masteryXp[i] = mXp.get(i);
		}
	}

	private static List<Integer> toList(int[] arr) {
		List<Integer> out = new ArrayList<>(arr.length);
		for (int v : arr) out.add(v);
		return out;
	}

	public static final Codec<PlayerRpgData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
		Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
		Codec.INT.optionalFieldOf("energy", 100).forGetter(d -> d.energy),
		Codec.INT.listOf().optionalFieldOf("mastery_level", new ArrayList<>()).forGetter(d -> toList(d.masteryLevel)),
		Codec.INT.listOf().optionalFieldOf("mastery_xp", new ArrayList<>()).forGetter(d -> toList(d.masteryXp))
	).apply(i, PlayerRpgData::new));
}
