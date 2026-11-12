package com.saorpg;

import com.saorpg.data.ModAttachments;
import com.saorpg.logic.RpgCommands;
import com.saorpg.logic.RpgEvents;
import com.saorpg.net.SyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SaoRpgMod implements ModInitializer {
	public static final String MOD_ID = "sao_rpg";

	@Override
	public void onInitialize() {
		ModAttachments.init();
		PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.CODEC);
		RpgEvents.register();
		RpgCommands.register();
	}
}
