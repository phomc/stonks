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
