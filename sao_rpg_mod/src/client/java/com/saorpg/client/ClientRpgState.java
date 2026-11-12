package com.saorpg.client;

import com.saorpg.data.MasteryBranch;
import com.saorpg.net.SyncPayload;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Estado RPG recibido del servidor + preferencia local del HUD. */
public final class ClientRpgState {
	public static int level = 1;
	public static int xp = 0;
	public static int energy = 100;
	public static int[] masteryLevel = new int[MasteryBranch.COUNT];
	public static int[] masteryXp = new int[MasteryBranch.COUNT];

	/** true = barras SAO, false = corazones normales. */
	public static boolean saoHud = true;

	public static void apply(SyncPayload p) {
		level = p.level();
		xp = p.xp();
		energy = p.energy();
		masteryLevel = p.masteryLevel();
		masteryXp = p.masteryXp();
	}

	private static Path configFile() {
		return FabricLoader.getInstance().getConfigDir().resolve("sao_rpg_client.properties");
	}

	public static void load() {
		Path f = configFile();
		if (!Files.exists(f)) return;
		try (InputStream in = Files.newInputStream(f)) {
			Properties props = new Properties();
			props.load(in);
			saoHud = Boolean.parseBoolean(props.getProperty("saoHud", "true"));
		} catch (IOException ignored) {
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("saoHud", Boolean.toString(saoHud));
		try (OutputStream out = Files.newOutputStream(configFile())) {
			props.store(out, "SAO RPG client config");
		} catch (IOException ignored) {
		}
	}

	private ClientRpgState() {
	}
}
