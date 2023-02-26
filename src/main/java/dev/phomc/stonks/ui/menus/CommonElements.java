package dev.phomc.stonks.ui.menus;

import java.text.DecimalFormat;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class CommonElements {
	public static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.##");

	public static final GuiElement EMPTY = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
			.setName(Component.empty())
			.build();
	public static final GuiElement PREV_PAGE = new GuiElementBuilder(Items.ARROW)
			.setName(Component.literal("<- ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Go back").withStyle(ChatFormatting.YELLOW)))
			.setCallback((idx, click, action, gui) -> {
				if (gui instanceof MarketMenu mm && mm.previousMenu != null) mm.previousMenu.open();
			})
			.build();
}
