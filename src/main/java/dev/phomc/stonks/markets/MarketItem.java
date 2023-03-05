package dev.phomc.stonks.markets;

import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.ui.menus.items.ItemMenu;
import dev.phomc.stonks.utils.DisplayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MarketItem {
	public final ItemStack item;

	// Quick info
	public Double avgInstantBuy = null, avgInstantSell = null;
	public Double instantBuy = null, instantSell = null;
	public boolean isUpdating = false;
	public long nextUpdate = 0;

	public MarketItem(ItemStack item) {
		this.item = item;
	}

	public boolean shouldUpdate() {
		return System.currentTimeMillis() > nextUpdate;
	}

	public GuiElement buildElement() {
		return new GuiElementBuilder(item.getItem())
				.setName(item.getHoverName())
				.addLoreLine(Component.empty())
				.addLoreLine(DisplayUtils.labeledValue("Instant Buy: ", instantBuy))
				.addLoreLine(DisplayUtils.labeledValue("Instant Sell: ", instantSell))
				.addLoreLine(DisplayUtils.labeledValue("Avg Instant Buy: ", avgInstantBuy))
				.addLoreLine(DisplayUtils.labeledValue("Avg Instant Sell: ", avgInstantSell))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.empty().withStyle(ChatFormatting.GRAY)
						.append(Component.literal("Click ").withStyle(ChatFormatting.GOLD))
						.append("for details"))
				.setCallback(this::click)
				.build();
	}

	private void click(int idx, ClickType click, net.minecraft.world.inventory.ClickType action, SlotGuiInterface gui) {
		if (gui instanceof MarketMenu mm) {
			ItemMenu item = new ItemMenu(mm.market, mm, mm.getPlayer(), this);
			item.open();
		}
	}
}
