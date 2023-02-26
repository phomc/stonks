package dev.phomc.stonks.markets;

import dev.phomc.stonks.Stonks;
import dev.phomc.stonks.modules.ItemIdsConverter;
import dev.phomc.stonks.ui.menus.CommonElements;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class MarketItem {
	public final ItemStack item;

	// Quick info
	public Double avgInstantBuy = null, avgInstantSell = null;
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
				.addLoreLine(labeledValue("Avg Instant Buy: ", avgInstantBuy))
				.addLoreLine(labeledValue("Avg Instant Sell: ", avgInstantSell))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.empty().withStyle(ChatFormatting.GRAY)
						.append(Component.literal("Click ").withStyle(ChatFormatting.GOLD))
						.append("for details"))
				.setCallback(this::click)
				.build();
	}

	private MutableComponent labeledValue(String label, Double value) {
		return Component.literal(label).withStyle(ChatFormatting.GRAY)
				.append(Component.literal(value != null? CommonElements.PRICE_FORMATTER.format(value) : "Not available!").withStyle(value != null? ChatFormatting.GOLD : ChatFormatting.RED));
	}

	private void click(int idx, ClickType click, net.minecraft.world.inventory.ClickType action, SlotGuiInterface gui) {
		Stonks.LOGGER.info("yo {}", ItemIdsConverter.DEFAULT_CONVERTER.fromItemStack(item));
	}
}
