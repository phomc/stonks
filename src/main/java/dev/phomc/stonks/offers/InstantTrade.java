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
