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

package dev.phomc.stonks.ui.menus.offers;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.TempPlayerData;
import dev.phomc.stonks.ui.menus.MarketMenu;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class OffersListMenu extends MarketMenu {
	private static final GuiElement LOADING = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
			.setName(Component.literal("Loading...").withStyle(ChatFormatting.GRAY))
			.build();

	private TempPlayerData temporaryData;
	private long nextUpdateTimestamp;
	private int currentPage = 0;

	public OffersListMenu(Market market, MarketMenu previousMenu, ServerPlayer player) {
		super(market, previousMenu, MenuType.GENERIC_9x6, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.myoffers.title", "Market > My Offers"));
		this.temporaryData = market.getTemporaryData(player);
		this.nextUpdateTimestamp = this.temporaryData.nextOffersListUpdate;

		drawOffers();
	}

	public void drawOffers(int page) {
		for (int i = 0; i < (9 * 5); i++) {
			int slot = 9 + i;
			clearSlot(slot);
			if (temporaryData.nextOffersListUpdateLock) {
				if (slot == 31) setSlot(31, LOADING);
			} else {
				if (i < temporaryData.offers.size()) setSlot(slot, temporaryData.offers.get(i).buildElement());
			}
		}
	}

	public void drawOffers() {
		drawOffers(currentPage);
	}

	private int ticked = 0;

	@Override
	public void onTick() {
		ticked++;

		if (ticked >= 20) {
			market.updateOffersList(temporaryData, false);

			if (nextUpdateTimestamp != temporaryData.nextOffersListUpdate || temporaryData.nextOffersListUpdateLock) {
				drawOffers();
				nextUpdateTimestamp = temporaryData.nextOffersListUpdate;
			}
		}
	}
}
