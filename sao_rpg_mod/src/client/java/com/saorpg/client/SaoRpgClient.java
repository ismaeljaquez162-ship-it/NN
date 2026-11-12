package com.saorpg.client;

import com.saorpg.net.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SaoRpgClient implements ClientModInitializer {
	private static KeyBinding toggleHud;
	private static KeyBinding openScreen;

	@Override
	public void onInitializeClient() {
		ClientRpgState.load();

		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) -> ClientRpgState.apply(payload));

		// Categoria de teclas (traduccion: "key.category.sao_rpg.main")
		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("sao_rpg", "main"));
		toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.sao_rpg.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
		openScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.sao_rpg.open_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, category));

		// Nuestro HUD se dibuja encima de todo.
		HudElementRegistry.addLast(Identifier.of("sao_rpg", "sao_hud"), SaoHud::render);

		// En modo SAO se oculta la barra de corazones de vanilla.
		HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, previous -> (context, tickCounter) -> {
			if (!ClientRpgState.saoHud) previous.render(context, tickCounter);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleHud.wasPressed()) {
				ClientRpgState.saoHud = !ClientRpgState.saoHud;
				ClientRpgState.save();
				if (client.player != null) {
					client.player.sendMessage(Text.translatable(ClientRpgState.saoHud
						? "message.sao_rpg.hud_sao" : "message.sao_rpg.hud_hearts"), true);
				}
			}
			while (openScreen.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					client.setScreen(new MasteryScreen());
				}
			}
		});
	}
}
