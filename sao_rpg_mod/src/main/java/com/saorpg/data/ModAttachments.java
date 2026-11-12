package com.saorpg.data;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class ModAttachments {
	/** Datos RPG del jugador (nivel, xp, energia, maestrias). Se conservan al morir. */
	public static final AttachmentType<PlayerRpgData> RPG = AttachmentRegistry.<PlayerRpgData>builder()
		.persistent(PlayerRpgData.CODEC)
		.copyOnDeath()
		.initializer(PlayerRpgData::new)
		.buildAndRegister(Identifier.of("sao_rpg", "rpg"));

	/** Nivel de un mob (se guarda con el mob). */
	public static final AttachmentType<Integer> MOB_LEVEL = AttachmentRegistry.createPersistent(
		Identifier.of("sao_rpg", "mob_level"), Codec.INT);

	public static void init() {
		// Fuerza la carga de la clase para registrar los attachments.
	}

	private ModAttachments() {
	}
}
