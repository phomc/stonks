package dev.phomc.stonks.services.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;

import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.services.StonksServiceProvider;
import dev.phomc.stonks.utils.Async;

/**
 * <p>A special service that destroy all data when server stops. Mainly for testing.</p>
 * @author nahkd
 *
 */
public class MemoryServiceProvider implements StonksServiceProvider {
	private final Map<ObjectId, OrderOffer> offers = new HashMap<>();
	private final Map<UUID, List<OrderOffer>> offersPerPlayer = new HashMap<>();

	@Override
	public CompletableFuture<OrderOffer> getOffer(ObjectId offerId) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(offerId);

			return offers.get(offerId);
		});
	}

	@Override
	public CompletableFuture<OrderOffer[]> getOffers(UUID player) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(player);

			List<OrderOffer> offers = offersPerPlayer.get(player);
			if (offers == null) offersPerPlayer.put(player, offers = new ArrayList<>());
			return offers.toArray(OrderOffer[]::new);
		});
	}

	@Override
	public CompletableFuture<Void> makeOffer(OrderOffer offer) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(offer);

			if (offers.containsKey(offer.offerId)) throw new IllegalArgumentException("offer id " + offer.offerId + " already exists!");
			List<OrderOffer> playerOffers = offersPerPlayer.get(offer.playerId);
			if (playerOffers == null) offersPerPlayer.put(offer.playerId, playerOffers = new ArrayList<>());

			offers.put(offer.offerId, offer);
			playerOffers.add(offer);
		});
	}

	@Override
	public CompletableFuture<Void> cancelOffer(ObjectId offerId) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(offerId);

			OrderOffer offer = offers.get(offerId);
			offers.remove(offerId);

			List<OrderOffer> playerOffers = offersPerPlayer.get(offer.playerId);
			if (playerOffers == null) offersPerPlayer.put(offer.playerId, playerOffers = new ArrayList<>());
			playerOffers.removeIf(v -> v.offerId.equals(offerId));
		});
	}
}
