package dev.phomc.stonks.ui.menus.items;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.ui.menus.SignInput;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class OfferCreateMenu extends MarketMenu {
	private MarketItem item;
	private OfferType type;

	private int customAmount = -1;

	public OfferCreateMenu(Market market, MarketMenu previousMenu, ServerPlayer player, MarketItem item, OfferType type) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.item.offeramount.title", "Market > %s > %s", item.item.getHoverName().getString(), type.display));
		this.item = item;
		this.type = type;

		setSlot(4, new GuiElement(item.item, (idx, click, action, gui) -> {}));

		setSlot(19, presetButton(1));
		setSlot(20, presetButton(16));
		setSlot(21, presetButton(64));
		setSlot(22, presetButton(64 * 4));
		setSlot(23, presetButton(64 * 16));

		setCustomAmount(-1);
	}

	private GuiElement presetButton(int amount) {
		Item displayIcon = amount < 64? Items.GOLD_NUGGET : Items.GOLD_INGOT;
		int displayCount = amount < 64? amount : amount / 64;

		GuiElementBuilder builder = new GuiElementBuilder(displayIcon, displayCount)
				.setName(Component.literal(amount + " item" + (amount == 1? "" : "s")).withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.empty().append(type.display).withStyle(ChatFormatting.DARK_GRAY))
				.addLoreLine(Component.empty());

		if (type == OfferType.SELL && previousMenu instanceof ItemMenu im && im.availableForSale < amount) {
			builder.addLoreLine(Component.literal("You don't have enough items").withStyle(ChatFormatting.RED));
		} else {
			builder.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to setup").withStyle(ChatFormatting.GRAY)));
			builder.setCallback(() -> {
				OfferPriceMenu menu = new OfferPriceMenu(market, this, player, item, type, amount);
				menu.open();
			});
		}

		return builder.build();
	}

	public boolean setCustomAmount(int amount) {
		this.customAmount = amount;
		boolean notEnough = previousMenu instanceof ItemMenu im && im.availableForSale < amount;

		GuiElementBuilder builder = new GuiElementBuilder(Items.DIAMOND)
				.setName(Component.literal("Custom" + (amount != -1? (" (" + amount + " item" + (amount == 1? "" : "s") + ")") : "")).withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.empty().append(type.display).withStyle(ChatFormatting.DARK_GRAY))
				.addLoreLine(Component.empty());

		if (amount == -1) {
			builder.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to set custom amount").withStyle(ChatFormatting.GRAY)));
			builder.setCallback(() -> openCustomAmountGui());
		} else if (type == OfferType.SELL && notEnough) {
			builder.addLoreLine(Component.literal("Not enough items").withStyle(ChatFormatting.RED));
			builder.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to change").withStyle(ChatFormatting.GRAY)));
			builder.setCallback(() -> openCustomAmountGui());
		} else {
			builder.addLoreLine(Component.literal("Left click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to setup price").withStyle(ChatFormatting.GRAY)));
			builder.addLoreLine(Component.literal("Right click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to change amount").withStyle(ChatFormatting.GRAY)));
			builder.setCallback((idx, click, action, gui) -> {
				if (click.isLeft) {
					OfferPriceMenu menu = new OfferPriceMenu(market, this, player, item, type, customAmount);
					menu.open();
				}

				if (click.isRight) openCustomAmountGui();
			});
		}

		setSlot(25, builder);
		return amount == -1 || !notEnough;
	}

	public void openCustomAmountGui() {
		SignInput input = new SignInput(player, str -> {
			try {
				int amount2 = Integer.parseInt(str);

				if (amount2 <= 0) {
					player.sendSystemMessage(Component.literal("You can only place an offer with at least 1 item.").withStyle(ChatFormatting.RED));
					OfferCreateMenu.this.open();
				} else {
					setCustomAmount(amount2);
					boolean notEnough = previousMenu instanceof ItemMenu im && im.availableForSale < amount2;

					if (!notEnough) {
						OfferPriceMenu menu = new OfferPriceMenu(market, this, player, item, type, customAmount);
						menu.open();
					} else {
						OfferCreateMenu.this.open();
					}
				}
			} catch (NumberFormatException t) {
				player.sendSystemMessage(Component.literal("That doesn't look like a number to me...").withStyle(ChatFormatting.RED));
				OfferCreateMenu.this.open();
			}
		});

		if (customAmount > 0) input.setLine(0, Component.literal("" + customAmount));
		input.setLine(1, Component.literal("\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF"));
		input.setLine(2, Component.literal("Please type how much you want"));
		input.setLine(3, Component.literal("to " + (type == OfferType.BUY? "buy" : "sell") + " above"));
		input.setColor(DyeColor.WHITE);
		input.setSignType(Blocks.DARK_OAK_SIGN);
		input.open();
	}
}
