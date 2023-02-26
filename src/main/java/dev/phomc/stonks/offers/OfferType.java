package dev.phomc.stonks.offers;

import net.minecraft.network.chat.Component;

public enum OfferType {
	BUY(Component.translatableWithFallback("stonks.offers.buy", "Buy offer")),
	SELL(Component.translatableWithFallback("stonks.offers.sell", "Sell offer"));

	public final Component display;

	OfferType(Component display) {
		this.display = display;
	}
}
