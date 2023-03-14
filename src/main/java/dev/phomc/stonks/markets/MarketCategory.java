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
