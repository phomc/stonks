package dev.phomc.stonks.ui.menus.items;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.offers.InstantTrade;
import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.ui.menus.SignInput;
import dev.phomc.stonks.utils.DisplayUtils;
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

public class InstantBuyMenu extends MarketMenu {
	private MarketItem item;
	private int customAmount = -1;

	public InstantBuyMenu(Market market, MarketMenu previousMenu, ServerPlayer player, MarketItem item) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.instantbuy.title", "Market > %s > Instant Buy", item.item.getHoverName().getString()));
		this.item = item;

		setSlot(4, new GuiElement(item.item, (idx, click, action, gui) -> {}));

		setSlot(19, presetButton(1));
		setSlot(20, presetButton(16));
		setSlot(21, presetButton(32));
		setSlot(22, presetButton(64));
		setSlot(23, presetButton(64 * 2));

		setCustomAmount(-1);
	}

	private GuiElement presetButton(int amount) {
		Item displayIcon = amount < 64? Items.GOLD_NUGGET : Items.GOLD_INGOT;
		int displayCount = amount < 64? amount : amount / 64;

		GuiElementBuilder builder = new GuiElementBuilder(displayIcon, displayCount)
				.setName(Component.literal(amount + " item" + (amount == 1? "" : "s")).withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.literal("Instant buy").withStyle(ChatFormatting.DARK_GRAY))
				.addLoreLine(Component.empty())
				.addLoreLine(DisplayUtils.labeledValue("Estimated cost: ", item.instantBuy != null? (item.instantBuy * amount) : null))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to buy").withStyle(ChatFormatting.GRAY)))
				.setCallback(() -> instantBuy(amount));

		return builder.build();
	}

	public void setCustomAmount(int amount) {
		this.customAmount = amount;

		GuiElementBuilder builder = new GuiElementBuilder(Items.DIAMOND)
				.setName(Component.literal("Custom" + (amount != -1? (" (" + amount + " item" + (amount == 1? "" : "s") + ")") : "")).withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.literal("Instant buy").withStyle(ChatFormatting.DARK_GRAY));

		if (amount == -1) {
			builder.addLoreLine(Component.empty());
			builder.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to set custom amount").withStyle(ChatFormatting.GRAY)));
			builder.setCallback(() -> openCustomAmountGui());
		} else {
			builder.addLoreLine(Component.empty());
			builder.addLoreLine(DisplayUtils.labeledValue("Estimated cost: ", item.instantBuy != null? (item.instantBuy * customAmount) : null));
			builder.addLoreLine(Component.empty());
			builder.addLoreLine(Component.literal("Left click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to buy").withStyle(ChatFormatting.GRAY)));
			builder.addLoreLine(Component.literal("Right click ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal("to change amount").withStyle(ChatFormatting.GRAY)));
			builder.setCallback((idx, click, action, gui) -> {
				if (click.isLeft) instantBuy(customAmount);
				if (click.isRight) openCustomAmountGui();
			});
		}

		setSlot(25, builder);
	}

	public void openCustomAmountGui() {
		SignInput input = new SignInput(player, str -> {
			try {
				int amount2 = Integer.parseInt(str);

				if (amount2 <= 0) {
					player.sendSystemMessage(Component.literal("You can only buy with at least 1 item.").withStyle(ChatFormatting.RED));
					InstantBuyMenu.this.open();
				} else {
					setCustomAmount(amount2);
					InstantBuyMenu.this.open();
				}
			} catch (NumberFormatException t) {
				player.sendSystemMessage(Component.literal("That doesn't look like a number to me...").withStyle(ChatFormatting.RED));
				InstantBuyMenu.this.open();
			}
		});

		if (customAmount > 0) input.setLine(0, Component.literal("" + customAmount));
		input.setLine(1, Component.literal("\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF\u00AF"));
		input.setLine(2, Component.literal("Please type how much you want"));
		input.setLine(3, Component.literal("to buy above"));
		input.setColor(DyeColor.WHITE);
		input.setSignType(Blocks.DARK_OAK_SIGN);
		input.open();
	}

	public void instantBuy(int amount) {
		close();
		if (item.instantBuy == null) {
			player.sendSystemMessage(Component.literal("Unable to estimate cost. Please wait a bit before try again.").withStyle(ChatFormatting.RED));
			return;
		}

		double budgetCap = item.instantBuy * amount + 10;
		InstantTrade trade = InstantTrade.instantBuy(item, budgetCap, amount);

		player.sendSystemMessage(Component.literal("Executing instant buy...").withStyle(ChatFormatting.GRAY));
		market.currency.take(player.getUUID(), budgetCap).thenRun(() -> {
			market.service.executeInstantTrade(trade).thenAccept(trade2 -> {
				double refund = trade2.budget;
				int itemsBought = amount - trade2.amount;

				market.currency.send(player.getUUID(), refund).exceptionally(t2 -> {
					t2.printStackTrace();
					player.sendSystemMessage(Component.literal("Unable to refund " + DisplayUtils.PRICE_FORMATTER.format(refund) + " (leftover money).").withStyle(ChatFormatting.RED));
					return null;
				});

				player.getInventory().placeItemBackInInventory(item.item.copyWithCount(itemsBought));

				if (trade2.amount == 0) {
					player.sendSystemMessage(Component.literal("You've bought " + amount + "x " + item.item.getHoverName().getString() + " for " + DisplayUtils.PRICE_FORMATTER.format(budgetCap - refund) + "!").withStyle(ChatFormatting.GRAY));
				} else {
					player.sendSystemMessage(Component.literal("You've bought " + itemsBought + "x " + item.item.getHoverName().getString() + " for " + DisplayUtils.PRICE_FORMATTER.format(budgetCap - refund) + ", but you can't get " + (amount - itemsBought) + " more. ").withStyle(ChatFormatting.YELLOW)
							.append(Component.literal("(Price manipulation?)").withStyle(ChatFormatting.GRAY)));
				}
			}).exceptionally(t -> {
				t.printStackTrace();
				player.sendSystemMessage(Component.literal("Unable to execute instant buy request, refunding...").withStyle(ChatFormatting.RED));

				market.currency.send(player.getUUID(), budgetCap).thenRun(() -> {
					player.sendSystemMessage(Component.literal("Unable to execute instant buy request. Please contact administrator.").withStyle(ChatFormatting.RED));
				}).exceptionally(t2 -> {
					t2.printStackTrace();
					player.sendSystemMessage(Component.literal("Unable to refund " + DisplayUtils.PRICE_FORMATTER.format(budgetCap) + ". Look like you have a really bad day today.").withStyle(ChatFormatting.RED));
					return null;
				});
				return null;
			});
		}).exceptionally(t -> {
			t.printStackTrace();
			player.sendSystemMessage(Component.literal("Failed to execute instant buy request. Do you have enough money?").withStyle(ChatFormatting.RED));
			return null;
		});
	}
}
