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

package dev.phomc.stonks.offers;

import dev.phomc.stonks.markets.MarketItem;

public class InstantTrade {
	public final MarketItem item;
	public final OfferType instantMode;
	public double budget; // Store leftover/received money
	public int amount; // How much items received/How much items left

	public InstantTrade(MarketItem item, OfferType instantMode) {
		this.item = item;
		this.instantMode = instantMode;
	}

	/**
	 * <p>Create new instant buy request.</p>
	 * @param item
	 * @param budget How much money player is willing to spend.
	 * @param amount How many items player needed.
	 */
	public static InstantTrade instantBuy(MarketItem item, double budget, int amount) {
		InstantTrade trade = new InstantTrade(item, OfferType.BUY);
		trade.budget = budget;
		trade.amount = amount;
		return trade;
	}

	/**
	 * <p>Create new instant sell request.</p>
	 * @param item
	 * @param amount How many items does player have.
	 */
	public static InstantTrade instantSell(MarketItem item, int amount) {
		InstantTrade trade = new InstantTrade(item, OfferType.SELL);
		trade.amount = amount;
		return trade;
	}
}
