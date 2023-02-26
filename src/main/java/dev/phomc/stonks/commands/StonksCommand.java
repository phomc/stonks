package dev.phomc.stonks.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.ui.menus.MainMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class StonksCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> market() {
		return Commands.literal("market").requires(v -> v.hasPermission(Commands.LEVEL_ALL)).executes(ctx -> {
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			Market market = Market.from(player);
			MainMenu menu = new MainMenu(market, null, player);
			menu.open();
			return 1;
		});
	}
}
