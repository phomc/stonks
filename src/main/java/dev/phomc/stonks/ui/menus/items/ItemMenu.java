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

package dev.phomc.stonks.ui.menus.items;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.offers.InstantTrade;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.utils.DisplayUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class ItemMenu extends MarketMenu {
	private MarketItem item;
	public int availableForSale;
	private boolean instantSold = false;

	public ItemMenu(Market market, MarketMenu previousMenu, ServerPlayer player, MarketItem item) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.item.title", "Market > %s", item.item.getHoverName().getString()));
		this.item = item;
		this.availableForSale = market.itemsComparator.countInContainers(item.item, player.getInventory());

		setSlot(4, new GuiElement(item.item, (idx, click, action, gui) -> {}));
		setSlot(22, new GuiElement(item.item, (idx, click, action, gui) -> {}));

		drawButtons();
	}

	public void drawButtons() {
		setSlot(19, new GuiElementBuilder(Items.GOLD_INGOT)
				.setName(Component.literal("Buy Instantly").withStyle(ChatFormatting.GOLD))
				.addLoreLine(DisplayUtils.labeledValue("Instant Buy Price: ", item.avgInstantBuy))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal("Left click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("for options").withStyle(ChatFormatting.GRAY)))
				.addLoreLine(Component.literal("Right click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to buy x1").withStyle(ChatFormatting.GRAY)))
				.setCallback((idx, click, action, gui) -> {
					InstantBuyMenu menu = new InstantBuyMenu(market, this, player, item);
					if (click.isLeft) menu.open();
					if (click.isRight) menu.instantBuy(1);
				}));

		setSlot(20, new GuiElementBuilder(Items.HOPPER)
				.setName(Component.literal("Sell Instantly").withStyle(ChatFormatting.GOLD))
				.addLoreLine(DisplayUtils.labeledValue("Instant Sell Price: ", item.avgInstantSell))
				.addLoreLine(Component.literal(availableForSale + " items for ").withStyle(ChatFormatting.GRAY)
						.append(Component.literal(DisplayUtils.PRICE_FORMATTER.format(availableForSale * (item.avgInstantSell == null? 0 : item.avgInstantSell))).withStyle(ChatFormatting.GOLD)))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to sell all").withStyle(ChatFormatting.GRAY)))
				.setCallback((idx, click, action, gui) -> instantSellAll()));

		setSlot(24, new GuiElementBuilder(Items.DIAMOND)
				.setName(Component.literal("Create Buy Offer").withStyle(ChatFormatting.GOLD))
				.addLoreLine(DisplayUtils.labeledValue("Avg Buy Price: ", item.avgInstantSell))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("for options").withStyle(ChatFormatting.GRAY)))
				.setCallback((idx, click, action, gui) -> openCreateOfferMenu(OfferType.BUY)));

		setSlot(25, new GuiElementBuilder(Items.HOPPER)
				.setName(Component.literal("Create Sell Offer").withStyle(ChatFormatting.GOLD))
				.addLoreLine(DisplayUtils.labeledValue("Avg Sell Price: ", item.avgInstantBuy))
				.addLoreLine(Component.empty())
				.addLoreLine(availableForSale > 0? Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("for options").withStyle(ChatFormatting.GRAY)) : Component.literal("You have no items").withStyle(ChatFormatting.RED))
				.setCallback((idx, click, action, gui) -> openCreateOfferMenu(OfferType.SELL)));
	}

	public void openCreateOfferMenu(OfferType type) {
		OfferCreateMenu menu = new OfferCreateMenu(market, this, getPlayer(), item, type);
		menu.open();
	}

	public void instantSellAll() {
		if (availableForSale == 0) return;
		if (instantSold) return ;
		instantSold = true;

		setSlot(20, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
				.setName(Component.literal("Please wait...").withStyle(ChatFormatting.GRAY)));

		int count2nd = market.itemsComparator.countInContainers(item.item, getPlayer().getInventory());
		if (count2nd < availableForSale) {
			getPlayer().sendSystemMessage(Component.literal("Failed to place offer: Items magically disappeared").withStyle(ChatFormatting.RED));
			return;
		}

		market.itemsComparator.removeInContainers(item.item, getPlayer().getInventory(), availableForSale);
		InstantTrade trade = InstantTrade.instantSell(item, availableForSale);

		market.service.executeInstantTrade(trade).thenCompose(trade2 -> {
			return market.currency.send(player.getUUID(), trade2.budget).thenRun(() -> {
				setSlot(20, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
						.setName(Component.literal("Items sold!").withStyle(ChatFormatting.GREEN)));

				if (trade2.amount == 0) {
					player.sendSystemMessage(Component.literal("You've sold " + availableForSale + "x " + item.item.getHoverName().getString() + " for " + DisplayUtils.PRICE_FORMATTER.format(trade2.budget) + ".").withStyle(ChatFormatting.GRAY));
				} else {
					player.getInventory().placeItemBackInInventory(item.item.copyWithCount(trade2.amount));
					player.sendSystemMessage(Component.literal("You've sold " + (availableForSale - trade2.amount) + "x " + item.item.getHoverName().getString() + " for " + DisplayUtils.PRICE_FORMATTER.format(trade2.budget) + ", but " + trade2.amount + " items can't be sold. ").withStyle(ChatFormatting.YELLOW)
							.append(Component.literal("(Not enough demand?)").withStyle(ChatFormatting.GRAY)));
				}
			}).exceptionally(t -> {
				t.printStackTrace();
				player.sendSystemMessage(Component.literal("Unable to send " + DisplayUtils.PRICE_FORMATTER.format(trade2.budget) + "to your purse. Please contact an administrator.").withStyle(ChatFormatting.RED));
				return null;
			});
		}).exceptionally(t -> {
			t.printStackTrace();
			player.getInventory().placeItemBackInInventory(item.item.copyWithCount(availableForSale));
			player.sendSystemMessage(Component.literal("Unable to execute instant sell request. Please contact an administrator.").withStyle(ChatFormatting.RED));
			return null;
		});
	}

	private int ticked = 0;

	@Override
	public void onTick() {
		ticked++;

		if (ticked >= 100) {
			market.updateQuickInfo(item, false);
			drawButtons();
		}
	}
}
