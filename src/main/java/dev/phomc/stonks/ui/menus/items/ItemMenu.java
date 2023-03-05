package dev.phomc.stonks.ui.menus.items;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
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
	protected int availableForSale;
	private boolean instantSold = false;

	public ItemMenu(Market market, MarketMenu previousMenu, ServerPlayer player, MarketItem item) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.item.title", "Market > %s", item.item.getHoverName().getString()));
		this.item = item;
		this.availableForSale = market.itemsComparator.countInContainers(item.item, player.getInventory());

		setSlot(4, new GuiElement(item.item, (idx, click, action, gui) -> {}));
		setSlot(22, new GuiElement(item.item, (idx, click, action, gui) -> {}));

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
		if (instantSold) return;
	}
}
