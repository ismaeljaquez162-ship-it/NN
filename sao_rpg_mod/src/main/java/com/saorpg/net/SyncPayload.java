package com.saorpg.net;

import com.saorpg.data.MasteryBranch;
import com.saorpg.data.PlayerRpgData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> cliente: estado RPG del jugador para pintar el HUD. */
public record SyncPayload(int level, int xp, int energy, int[] masteryLevel, int[] masteryXp) implements CustomPayload {
	public static final CustomPayload.Id<SyncPayload> ID = new CustomPayload.Id<>(Identifier.of("sao_rpg", "sync"));
	public static final PacketCodec<RegistryByteBuf, SyncPayload> CODEC = PacketCodec.of(SyncPayload::write, SyncPayload::read);

	public static SyncPayload from(PlayerRpgData d) {
		return new SyncPayload(d.level, d.xp, d.energy, d.masteryLevel.clone(), d.masteryXp.clone());
	}

	private static void write(SyncPayload p, RegistryByteBuf buf) {
		buf.writeVarInt(p.level);
		buf.writeVarInt(p.xp);
		buf.writeVarInt(p.energy);
		for (int i = 0; i < MasteryBranch.COUNT; i++) {
			buf.writeVarInt(p.masteryLevel[i]);
			buf.writeVarInt(p.masteryXp[i]);
		}
	}

	private static SyncPayload read(RegistryByteBuf buf) {
		int level = buf.readVarInt();
		int xp = buf.readVarInt();
		int energy = buf.readVarInt();
		int[] ml = new int[MasteryBranch.COUNT];
		int[] mx = new int[MasteryBranch.COUNT];
		for (int i = 0; i < MasteryBranch.COUNT; i++) {
			ml[i] = buf.readVarInt();
			mx[i] = buf.readVarInt();
		}
		return new SyncPayload(level, xp, energy, ml, mx);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
