package dev.phomc.stonks.markets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.phomc.stonks.ui.menus.MainMenu;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;

public class MarketCategory extends GuiElementBuilder {
	public final List<MarketItem> items = new ArrayList<>();

	public MarketCategory() {
		super();
		setCallback(this::click);
	}

	public MarketCategory builder(Consumer<GuiElementBuilder> cb) {
		cb.accept(this);
		return this;
	}

	public MarketCategory add(MarketItem item) {
		items.add(item);
		return this;
	}

	private void click(int idx, ClickType click, net.minecraft.world.inventory.ClickType action, SlotGuiInterface gui) {
		if (gui instanceof MainMenu main) {
			main.selectCategory(this);
		}
	}
}
