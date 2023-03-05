package dev.phomc.stonks.ui.menus.items;

import java.util.UUID;

import dev.phomc.stonks.Stonks;
import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.ui.SignInput;
import dev.phomc.stonks.utils.DisplayUtils;
import dev.phomc.stonks.utils.GeneralConverter;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class OfferPriceMenu extends MarketMenu {
	private MarketItem item;
	private OfferType type;
	private int amount;

	private Double topPrice;
	private Double avgPrice;
	private Double customPrice;

	public OfferPriceMenu(Market market, MarketMenu previousMenu, ServerPlayer player, MarketItem item, OfferType type, int amount) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.item.offerprice.title", "Market > %s > %s > Pricing", item.item.getHoverName().getString(), type.display));
		this.item = item;
		this.type = type;
		this.amount = amount;

		this.topPrice = type == OfferType.BUY? item.instantSell : item.instantBuy;
		this.avgPrice = type == OfferType.BUY? item.avgInstantSell : item.avgInstantBuy;

		setSlot(4, new GuiElement(item.item, (idx, click, action, gui) -> {}));

		setSlot(19, new GuiElementBuilder((topPrice != null && topPrice > 0.2)? Items.DIAMOND : Items.BARRIER)
				.setName(Component.literal("Top offer " + (type == OfferType.BUY? "+" : "-") + " 0.1").withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.literal("Get your order filled first").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.empty())
				.addLoreLine(DisplayUtils.labeledValue("Price per unit: ", topPrice != null? (topPrice + (type == OfferType.BUY? 0.1 : -0.1)) : null))
				.addLoreLine(DisplayUtils.labeledValue("Total: ", topPrice != null? ((topPrice + (type == OfferType.BUY? 0.1 : -0.1)) * amount) : null))
				.addLoreLine(topPrice != null? Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to place offer").withStyle(ChatFormatting.GRAY)) : Component.literal("Can't place offer").withStyle(ChatFormatting.RED))
				.setCallback(() -> {
					if (topPrice == null) return;
					if (topPrice <= 0.2) return;
					placeOffer(topPrice + (type == OfferType.BUY? 0.1 : -0.1));
				}));

		setSlot(21, new GuiElementBuilder(topPrice != null? Items.GOLD_INGOT : Items.BARRIER)
				.setName(Component.literal("Same as top offer").withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.empty())
				.addLoreLine(DisplayUtils.labeledValue("Price per unit: ", topPrice))
				.addLoreLine(DisplayUtils.labeledValue("Total: ", topPrice != null? (topPrice * amount) : null))
				.addLoreLine(topPrice != null? Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to place offer").withStyle(ChatFormatting.GRAY)) : Component.literal("Can't place offer").withStyle(ChatFormatting.RED))
				.setCallback(() -> {
					if (topPrice == null) return;
					placeOffer(topPrice);
				}));

		setSlot(23, new GuiElementBuilder((avgPrice != null && avgPrice > 0.1)? Items.GOLD_NUGGET : Items.BARRIER)
				.setName(Component.literal("Average price").withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.empty())
				.addLoreLine(DisplayUtils.labeledValue("Price per unit: ", avgPrice))
				.addLoreLine(DisplayUtils.labeledValue("Total: ", avgPrice != null? (avgPrice * amount) : null))
				.addLoreLine(topPrice != null? Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to place offer").withStyle(ChatFormatting.GRAY)) : Component.literal("Can't place offer").withStyle(ChatFormatting.RED))
				.setCallback(() -> {
					if (avgPrice == null) return;
					if (avgPrice <= 0.1) return;
					placeOffer(avgPrice);
				}));

		setCustomPrice(null);
	}

	public void placeOffer(double price) {
		close();
		getPlayer().sendSystemMessage(Component.literal("Placing offer...").withStyle(ChatFormatting.GRAY));

		switch (type) {
		case BUY: placeBuyOffer(price); break;
		case SELL: placeSellOffer(price); break;
		default: break;
		}
	}

	private void placeBuyOffer(double price) {
		double total = price * amount;

		OrderOffer offer = new OrderOffer(getPlayer().getUUID(), type, item.item, amount, price, System.currentTimeMillis() + Market.ORDER_DURATION);
		market.currency.take(getPlayer().getUUID(), total)
		.thenCompose($_ -> market.service.makeOffer(offer)
				.thenAccept(__ -> {
					market.getTemporaryData(player).offers.add(offer);
					getPlayer().sendSystemMessage(Component.empty().withStyle(ChatFormatting.GRAY)
							.append(type.display)
							.append(" for " + item.item.getHoverName().getString() + " has been placed @ " + DisplayUtils.PRICE_FORMATTER.format(price) + "/ea (" + DisplayUtils.PRICE_FORMATTER.format(price * amount) + " in total)"));
					})
				.exceptionally(t -> {
					t.printStackTrace();
					getPlayer().sendSystemMessage(Component.literal("Failed to place offer, refunding...").withStyle(ChatFormatting.RED));

					market.currency.send(getPlayer().getUUID(), total)
					.thenAccept(__ -> {
						getPlayer().sendSystemMessage(Component.literal("Failed to place offer. Please contact an administrator.").withStyle(ChatFormatting.RED));
					})
					.exceptionally(t2 -> {
						// TODO: Better refund error handling
						UUID reportUuid = UUID.randomUUID();

						Stonks.LOGGER.error("--- FAILED TO REFUND ---");
						t2.printStackTrace();
						Stonks.LOGGER.error("---");
						Stonks.LOGGER.error("Error ID for this incident: {}", reportUuid);
						Stonks.LOGGER.error("Player UUID: {}", getPlayer().getUUID());
						Stonks.LOGGER.error("Amount: {}", total);
						Stonks.LOGGER.error("---");

						getPlayer().sendSystemMessage(Component.literal("Failed to refund. Please contact an administrator and give them this ID: " + reportUuid).withStyle(ChatFormatting.RED));
						return null;
					});
					return null;
				}))
		.exceptionally(t -> {
			t.printStackTrace();
			getPlayer().sendSystemMessage(Component.literal("Failed to place offer. Please contact an administrator.").withStyle(ChatFormatting.RED));
			return null;
		});
	}

	private void placeSellOffer(double price) {
		int count2nd = market.itemsComparator.countInContainers(item.item, getPlayer().getInventory());

		if (count2nd < amount) {
			getPlayer().sendSystemMessage(Component.literal("Failed to place offer: Items magically disappeared").withStyle(ChatFormatting.RED));
			return;
		}

		market.itemsComparator.removeInContainers(item.item, getPlayer().getInventory(), amount); // TODO: do something with leftovers

		OrderOffer offer = new OrderOffer(getPlayer().getUUID(), type, item.item, amount, price, System.currentTimeMillis() + Market.ORDER_DURATION);
		market.service.makeOffer(offer)
		.thenAccept($_ -> {
			market.getTemporaryData(player).offers.add(offer);
			getPlayer().sendSystemMessage(Component.empty().withStyle(ChatFormatting.GRAY)
					.append(type.display)
					.append(" for " + item.item.getHoverName().getString() + " has been placed @ " + DisplayUtils.PRICE_FORMATTER.format(price) + "/ea (" + DisplayUtils.PRICE_FORMATTER.format(price * amount) + " in total)"));
		})
		.exceptionally(t -> {
			t.printStackTrace();
			getPlayer().sendSystemMessage(Component.literal("Failed to place offer, refunding items...").withStyle(ChatFormatting.RED));
			getPlayer().getInventory().placeItemBackInInventory(item.item.copyWithCount(amount));
			getPlayer().sendSystemMessage(Component.literal("Failed to place offer. Please contact an administrator.").withStyle(ChatFormatting.RED));
			return null;
		});
	}

	public void setCustomPrice(Double price) {
		this.customPrice = price;
		GuiElementBuilder builder = new GuiElementBuilder(Items.DARK_OAK_SIGN)
				.setName(Component.literal("Custom " + (price != null? ("(" + DisplayUtils.PRICE_FORMATTER.format(price) + ")") : "price per unit")).withStyle(ChatFormatting.GOLD));

		if (price == null) {
			builder.addLoreLine(Component.empty());
			builder.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to set price").withStyle(ChatFormatting.GRAY)));
			builder.setCallback(() -> openCustomPriceGui());
		} else {
			builder.addLoreLine(DisplayUtils.labeledValue("Total: ", price * amount));
			builder.addLoreLine(Component.empty());
			builder.addLoreLine(Component.literal("Left click ").withStyle(ChatFormatting.YELLOW)
					.append("to confirm"));
			builder.addLoreLine(Component.literal("Right click ").withStyle(ChatFormatting.YELLOW)
					.append("to change price"));
			builder.setCallback((idx, click, action, gui) -> {
				if (click.isLeft) placeOffer(customPrice);
				if (click.isRight) openCustomPriceGui();
			});
		}

		setSlot(25, builder);
	}

	public void openCustomPriceGui() {
		SignInput input = new SignInput(player, str -> {
			try {
				double price = GeneralConverter.moneyWithSuffix(str);
				if (price <= 0) player.sendSystemMessage(Component.literal("Price must be above 0").withStyle(ChatFormatting.RED));
				setCustomPrice(price);
			} catch (NumberFormatException e) {
				player.sendSystemMessage(Component.literal("That doesn't look like a number to me...").withStyle(ChatFormatting.RED));
			}

			OfferPriceMenu.this.open();
		});

		if (customPrice != null) input.setLine(0, Component.literal(DisplayUtils.PRICE_FORMATTER.format(customPrice)));
		input.setLine(1, Component.literal("\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF"));
		input.setLine(2, Component.literal("Type price per unit above"));
		input.setLine(3, Component.literal("You can use 'k', 'm' or 'b' suffixes"));
		input.setColor(DyeColor.WHITE);
		input.setSignType(Blocks.DARK_OAK_SIGN);
		input.open();
	}
}
