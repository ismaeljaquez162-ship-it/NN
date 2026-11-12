package com.saorpg.logic;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.saorpg.data.MasteryBranch;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;

/**
 * Comandos de admin / pruebas:
 *   /sao xp add <jugador> <cantidad>
 *   /sao level set <jugador> <nivel>
 *   /sao mastery set <jugador> <rama> <nivel>
 *   /sao reset <jugador>
 */
public final class RpgCommands {

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("sao")
				.requires(src -> src.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS))
				.then(CommandManager.literal("xp")
					.then(CommandManager.literal("add")
						.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
								.executes(ctx -> {
									ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
									int amount = IntegerArgumentType.getInteger(ctx, "amount");
									RpgService.addXp(p, amount);
									ctx.getSource().sendFeedback(() -> Text.literal("+" + amount + " XP -> " + p.getName().getString()), true);
									return 1;
								})))))
				.then(CommandManager.literal("level")
					.then(CommandManager.literal("set")
						.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("level", IntegerArgumentType.integer(1, RpgMath.MAX_LEVEL))
								.executes(ctx -> {
									ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
									int level = IntegerArgumentType.getInteger(ctx, "level");
									RpgService.setLevel(p, level);
									ctx.getSource().sendFeedback(() -> Text.literal(p.getName().getString() + " -> nivel " + level), true);
									return 1;
								})))))
				.then(CommandManager.literal("mastery")
					.then(CommandManager.literal("set")
						.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("branch", StringArgumentType.word())
								.suggests((c, b) -> CommandSource.suggestMatching(Arrays.stream(MasteryBranch.values()).map(x -> x.id), b))
								.then(CommandManager.argument("level", IntegerArgumentType.integer(0, RpgMath.MAX_MASTERY))
									.executes(ctx -> {
										ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
										MasteryBranch branch = MasteryBranch.byId(StringArgumentType.getString(ctx, "branch"));
										if (branch == null) {
											ctx.getSource().sendError(Text.literal("Rama desconocida"));
											return 0;
										}
										int level = IntegerArgumentType.getInteger(ctx, "level");
										RpgService.setMastery(p, branch, level);
										ctx.getSource().sendFeedback(() -> Text.literal(branch.id + " -> " + level), true);
										return 1;
									}))))))
				.then(CommandManager.literal("reset")
					.then(CommandManager.argument("player", EntityArgumentType.player())
						.executes(ctx -> {
							ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
							RpgService.reset(p);
							ctx.getSource().sendFeedback(() -> Text.literal("Reiniciado: " + p.getName().getString()), true);
							return 1;
						}))));
		});
	}

	private RpgCommands() {
	}
}
