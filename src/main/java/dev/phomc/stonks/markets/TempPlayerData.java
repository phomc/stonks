package dev.phomc.stonks.markets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.phomc.stonks.offers.OrderOffer;

/**
 * <p>Store data temporary. Will be destroyed after player left the server.</p>
 * @author nahkd
 *
 */
public class TempPlayerData {
	public final UUID playerId;
	public MarketCategory selectedCategory;

	// Caching old data
	public long nextOffersListUpdate = 0;
	public boolean nextOffersListUpdateLock = false; // true == updating list
	public final List<OrderOffer> offers = new ArrayList<>();

	public TempPlayerData(UUID playerId) {
		this.playerId = playerId;
	}
}
