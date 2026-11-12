package com.saorpg.client;

import com.saorpg.data.MasteryBranch;
import com.saorpg.logic.GearRules;
import com.saorpg.logic.RpgMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** HUD estilo Sword Art Online: nombre, nivel, barra de vida, energia y XP. */
public final class SaoHud {
	private static float shownHp = -1f;

	public static void render(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!ClientRpgState.saoHud || mc.player == null || mc.options.hudHidden || mc.player.isSpectator()) return;
		// Nota: en 1.21.11 los colores son ARGB; todos llevan alfa (0xFF...) para que se vean.
		TextRenderer tr = mc.textRenderer;

		final int px = 6, py = 6, pw = 220, ph = 60;
		final int barX = px + 26, barW = 130;

		// Panel
		ctx.fill(px, py, px + pw, py + ph, 0x99101820);
		ctx.fill(px, py, px + pw, py + 1, 0xFFE8A33D);
		ctx.fill(px, py + ph - 1, px + pw, py + ph, 0x55FFFFFF);

		// Nombre y nivel
		ctx.drawTextWithShadow(tr, mc.player.getName(), px + 8, py + 5, 0xFFFFFFFF);
		String lv = "Lv. " + ClientRpgState.level;
		ctx.drawTextWithShadow(tr, lv, px + pw - 8 - tr.getWidth(lv), py + 5, 0xFFF2B33D);

		// Vida
		float hp = mc.player.getHealth();
		float maxHp = Math.max(1f, mc.player.getMaxHealth());
		if (shownHp < 0f) shownHp = hp;
		shownHp += (hp - shownHp) * 0.15f;
		float ratio = shownHp / maxHp;
		int top, bottom;
		if (ratio > 0.5f) { top = 0xFF8BE86A; bottom = 0xFF3FA83A; }
		else if (ratio > 0.25f) { top = 0xFFF2E05A; bottom = 0xFFC9A82A; }
		else { top = 0xFFFF6A55; bottom = 0xFFB52A1F; }
		small(ctx, tr, "HP", px + 8, py + 19, 0xFFB8F0A8);
		bar(ctx, barX, py + 17, barW, 9, ratio, top, bottom);
		String hpText = (int) Math.ceil(hp) + "/" + (int) Math.ceil(maxHp);
		ctx.drawTextWithShadow(tr, hpText, barX + barW + 5, py + 18, 0xFFFFFFFF);

		// Energia
		int maxEn = RpgMath.maxEnergy(ClientRpgState.level);
		small(ctx, tr, "EN", px + 8, py + 31, 0xFF9FD8FF);
		bar(ctx, barX, py + 30, barW, 5, (float) ClientRpgState.energy / maxEn, 0xFF6FD0FF, 0xFF2A7FC0);
		small(ctx, tr, ClientRpgState.energy + "/" + maxEn, barX + barW + 5, py + 30, 0xFFCFEAFF);

		// XP
		small(ctx, tr, "XP", px + 8, py + 41, 0xFFFFD08A);
		int need = RpgMath.xpForNext(ClientRpgState.level);
		boolean max = ClientRpgState.level >= RpgMath.MAX_LEVEL;
		bar(ctx, barX, py + 40, barW, 4, max ? 1f : (float) ClientRpgState.xp / need, 0xFFFFC857, 0xFFE8892D);
		String xpText = max ? "XP MAX"
			: "XP " + ClientRpgState.xp + " / " + need + "  (faltan " + (need - ClientRpgState.xp) + ")";
		small(ctx, tr, xpText, px + 8, py + 48, 0xFFFFE2B0);

		// Rama del objeto en mano
		MasteryBranch branch = GearRules.branchOf(mc.player.getMainHandStack());
		if (branch != null) {
			int have = ClientRpgState.masteryLevel[branch.ordinal()];
			int req = GearRules.requiredLevel(mc.player.getMainHandStack());
			String label = Text.translatable(branch.translationKey()).getString() + " " + have + (have < req ? " (req. " + req + ")" : "");
			int color = have < req ? 0xFFFF7B6B : 0xFFB8F0A8;
			int w = (int) (tr.getWidth(label) * 0.75f);
			small(ctx, tr, label, px + pw - 8 - w, py + 48, color);
		}
	}

	private static void bar(DrawContext c, int x, int y, int w, int h, float ratio, int top, int bottom) {
		c.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF0B0F14);
		c.fill(x, y, x + w, y + h, 0xFF2A333C);
		int fw = Math.round(w * MathHelper.clamp(ratio, 0f, 1f));
		if (fw > 0) {
			c.fillGradient(x, y, x + fw, y + h, top, bottom);
			c.fill(x, y, x + fw, y + 1, 0x44FFFFFF);
		}
	}

	private static void small(DrawContext c, TextRenderer tr, String text, int x, int y, int color) {
		c.getMatrices().pushMatrix();
		c.getMatrices().scale(0.75f, 0.75f);
		c.drawTextWithShadow(tr, text, (int) (x / 0.75f), (int) (y / 0.75f), color);
		c.getMatrices().popMatrix();
	}

	private SaoHud() {
	}
}
