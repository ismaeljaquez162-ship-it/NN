package com.saorpg.client;

import com.saorpg.data.MasteryBranch;
import com.saorpg.logic.RpgMath;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Tablero simple de maestrias: nivel y progreso de cada rama de equipamiento. */
public class MasteryScreen extends Screen {

	public MasteryScreen() {
		super(Text.translatable("screen.sao_rpg.mastery"));
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int y = 30;
		ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y, 0xFFF2B33D);
		y += 14;
		ctx.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal("Lv. " + ClientRpgState.level), cx, y, 0xFFFFFFFF);
		y += 20;

		int barW = 140, x = cx - 110;
		for (MasteryBranch b : MasteryBranch.values()) {
			int lvl = ClientRpgState.masteryLevel[b.ordinal()];
			int xp = ClientRpgState.masteryXp[b.ordinal()];
			int need = RpgMath.masteryXpForNext(lvl);
			boolean max = lvl >= RpgMath.MAX_MASTERY;

			ctx.drawTextWithShadow(this.textRenderer, Text.translatable(b.translationKey()), x, y, 0xFFFFFFFF);
			int bx = x + 90;
			ctx.fill(bx - 1, y, bx + barW + 1, y + 9, 0xFF0B0F14);
			ctx.fill(bx, y + 1, bx + barW, y + 8, 0xFF2A333C);
			int fw = max ? barW : Math.round(barW * (float) xp / Math.max(1, need));
			if (fw > 0) ctx.fillGradient(bx, y + 1, bx + fw, y + 8, 0xFF6FD0FF, 0xFF2A7FC0);
			ctx.drawTextWithShadow(this.textRenderer, String.valueOf(lvl), bx + barW + 6, y, 0xFFFFD08A);
			y += 16;
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
