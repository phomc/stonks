package dev.phomc.stonks.ui.menus;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketCategory;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.ui.CommonElements;
import dev.phomc.stonks.ui.menus.items.ItemMenu;
import dev.phomc.stonks.ui.menus.offers.OffersListMenu;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class MainMenu extends MarketMenu {
	protected static final GuiElement UNSELECTED = new GuiElementBuilder(Items.BARRIER)
			.setName(Component.literal("Please select a category!").withStyle(ChatFormatting.RED))
			.addLoreLine(Component.empty())
			.addLoreLine(Component.literal("You must select a category on the").withStyle(ChatFormatting.GRAY))
			.addLoreLine(Component.literal("left of this menu!").withStyle(ChatFormatting.GRAY))
			.build();

	private MarketCategory selectedCategory;

	public MainMenu(Market market, MarketMenu previousMenu, ServerPlayer player) {
		super(market, previousMenu, MenuType.GENERIC_9x6, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.main.title", "Market"));
		this.selectedCategory = market.getTemporaryData(player).selectedCategory;

		setSlot(3, new GuiElementBuilder(Items.GOLD_INGOT)
				.setName(Component.literal("Sell all").withStyle(ChatFormatting.YELLOW))
				.addLoreLine(Component.empty().withStyle(ChatFormatting.GRAY)
						.append(Component.literal("Click ").withStyle(ChatFormatting.GOLD))
						.append("to sell all items in your"))
				.addLoreLine(Component.literal("inventory").withStyle(ChatFormatting.GRAY))
				.setCallback(this::sellAll));

		setSlot(5, new GuiElementBuilder(Items.PAPER)
				.setName(Component.literal("View offers").withStyle(ChatFormatting.YELLOW))
				.addLoreLine(Component.empty().withStyle(ChatFormatting.GRAY)
						.append(Component.literal("Click ").withStyle(ChatFormatting.GOLD))
						.append(Component.literal("to view your offers")))
				.setCallback((idx, click, action, gui) -> {
					OffersListMenu myOffers = new OffersListMenu(market, this, player);
					myOffers.open();
				}));

		drawCategoriesBar();
		drawCategoryView();
	}

	public MarketCategory getSelectedCategory() {
		return selectedCategory;
	}

	public void selectCategory(MarketCategory category) {
		this.selectedCategory = category;
		market.getTemporaryData(getPlayer()).selectedCategory = category;
		drawCategoryView();
	}

	public void drawCategoriesBar() {
		setCategory(0, 0); setSlot(10, CommonElements.EMPTY);
		setCategory(1, 0); setSlot(19, CommonElements.EMPTY);
		setCategory(2, 0); setSlot(28, CommonElements.EMPTY);
		setCategory(3, 0); setSlot(37, CommonElements.EMPTY);
		setCategory(4, 0); setSlot(46, CommonElements.EMPTY);
	}

	public void setCategory(int idx, MarketCategory category) {
		if (category == null) clearSlot(9 + idx * 9);
		else setSlot(9 + idx * 9, category);
	}

	public void setCategory(int idx, int scroll) {
		MarketCategory category = idx + scroll < market.categories.size()? market.categories.get(idx + scroll) : null;
		setCategory(idx, category);
	}

	public void drawCategoryView() {
		for (int i = 0; i < (7 * 5); i++) {
			int row = i / 7, col = i % 7;
			int slot = (2 + col) + (1 + row) * 9;
			clearSlot(slot);

			if (selectedCategory == null) {
				if (i == (7 * 2 + 3)) setSlot(slot, UNSELECTED);
			} else if (i < selectedCategory.items.size()) {
				MarketItem item = selectedCategory.items.get(i);
				market.updateQuickInfo(item, false);
				setSlot(slot, item.buildElement());
			}
		}
	}

	public void sellAll() {
		close();
		getPlayer().sendSystemMessage(Component.literal("Selling all items...").withStyle(ChatFormatting.GRAY));
		boolean soldOnce = false;

		for (MarketCategory category : market.categories) {
			for (MarketItem item : category.items) {
				ItemMenu menu = new ItemMenu(market, this, player, item);

				if (menu.availableForSale > 0) {
					menu.instantSellAll();
					soldOnce = true;
				}
			}
		}

		if (!soldOnce) getPlayer().sendSystemMessage(Component.literal("No items sold.").withStyle(ChatFormatting.RED));
	}

	// Internal update clock
	private int ticked = 0;

	@Override
	public void onTick() {
		ticked++;
		if (ticked > 20 * 10) {
			ticked = 0;
			drawCategoryView();
		}
	}
}
