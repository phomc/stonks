/*
 * Copyright (c) 2023 PhoMC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.phomc.stonks.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.ui.menus.MainMenu;
import dev.phomc.stonks.ui.menus.offers.OffersListMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class StonksCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> market() {
		return Commands.literal("market").requires(v -> v.hasPermission(Commands.LEVEL_ALL))
				.then(Commands.literal("myoffers").executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					Market market = Market.from(player);
					OffersListMenu menu = new OffersListMenu(market, null, player);
					menu.open();
					return 1;
				}))
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					Market market = Market.from(player);
					MainMenu menu = new MainMenu(market, null, player);
					menu.open();
					return 1;
				});
	}
}
