package dev.phomc.stonks.services.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;

import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.modules.ItemsComparator;
import dev.phomc.stonks.offers.InstantTrade;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.services.StonksService;
import dev.phomc.stonks.utils.Async;

/**
 * <p>A special service that destroy all data when server stops. Mainly for testing.</p>
 * @author nahkd
 *
 */
public class MemoryService implements StonksService {
	private final Map<ObjectId, OrderOffer> offers = new HashMap<>();
	private final Map<UUID, List<OrderOffer>> offersPerPlayer = new HashMap<>();

	public ItemsComparator comparator = ItemsComparator.DEFAULT_COMPARATOR;

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
			if (offer.offerId == null) offer.offerId = ObjectId.get();

			if (offers.containsKey(offer.offerId)) throw new IllegalArgumentException("offer id " + offer.offerId + " already exists!");
			List<OrderOffer> playerOffers = offersPerPlayer.get(offer.playerId);
			if (playerOffers == null) offersPerPlayer.put(offer.playerId, playerOffers = new ArrayList<>());

			offers.put(offer.offerId, offer);
			playerOffers.add(offer);
		});
	}

	@Override
	public CompletableFuture<OrderOffer> cancelOffer(ObjectId offerId) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(offerId);

			OrderOffer offer = offers.get(offerId);
			offers.remove(offerId);

			List<OrderOffer> playerOffers = offersPerPlayer.get(offer.playerId);
			if (playerOffers == null) offersPerPlayer.put(offer.playerId, playerOffers = new ArrayList<>());
			playerOffers.removeIf(v -> v.offerId.equals(offerId));
			return offer;
		});
	}

	@Override
	public CompletableFuture<Integer> claimItems(ObjectId offerId) {
		return Async.toAsync(() -> {
			Preconditions.checkNotNull(offerId);

			OrderOffer offer = offers.get(offerId);
			if (offer == null) throw new NullPointerException("Offer with id = " + offerId + " does not exists");

			int delta = offer.filled - offer.claimed;
			offer.claimed = offer.filled;

			if (offer.claimed == offer.amount) {
				offers.remove(offerId);

				List<OrderOffer> playerOffers = offersPerPlayer.get(offer.playerId);
				if (playerOffers == null) offersPerPlayer.put(offer.playerId, playerOffers = new ArrayList<>());
				playerOffers.removeIf(v -> v.offerId.equals(offerId));
			}

			return delta;
		});
	}

	@Override
	public CompletableFuture<InstantTrade> executeInstantTrade(InstantTrade trade) {
		return Async.toAsync(() -> {
			OfferType targetOffersWithType = trade.instantMode == OfferType.BUY? OfferType.SELL : OfferType.BUY;
			List<OrderOffer> entries = new ArrayList<>();
			entries.addAll(offers.values().stream()
					.filter(v -> v.type == targetOffersWithType && (v.amount > v.filled) && comparator.isSimilar(trade.item.item, v.item))
					.toList());

			if (targetOffersWithType == OfferType.BUY) {
				// Fill buy offers with highest PPU first
				entries.sort((a, b) -> Double.compare(b.pricePerUnit, a.pricePerUnit));
			} else {
				// Fill sell offers with lowest PPU first
				entries.sort((a, b) -> Double.compare(a.pricePerUnit, b.pricePerUnit));
			}

			for (OrderOffer entry : entries) {
				if (trade.instantMode == OfferType.BUY) {
					int canPay = (int) Math.floor(trade.budget / entry.pricePerUnit); // how many items we can pay for
					int itemsNeeded = Math.min(entry.amount - entry.filled, trade.amount); // how many items we needed from this offer
					int chooseToPay = Math.min(canPay, itemsNeeded); // how many items we can get

					trade.amount -= chooseToPay;
					trade.budget -= chooseToPay * entry.pricePerUnit;
					entry.filled += chooseToPay;

					if (trade.amount == 0) break;
					if (chooseToPay != itemsNeeded) break; // we ran out of money?
				} else {
					int itemsSold = Math.min(entry.amount - entry.filled, trade.amount);
					trade.amount -= itemsSold;
					trade.budget += itemsSold * entry.pricePerUnit;
					entry.filled += itemsSold;

					if (trade.amount == 0) break;
				}
			}

			return trade;
		});
	}

	@Override
	public CompletableFuture<Void> updateProductQuickInfo(MarketItem item) {
		if (item.isUpdating) return CompletableFuture.completedFuture(null);

		return Async.toAsync(() -> {
			item.isUpdating = true;

			// TODO: better way to update values
			// We likely are going to propagate data right inside MongoDB server
			List<OrderOffer> entries = offers.values().stream().filter(v -> comparator.isSimilar(item.item, v.item)).toList();
			List<OrderOffer> buyEntries = new ArrayList<>();
			List<OrderOffer> sellEntries = new ArrayList<>();

			buyEntries.addAll(entries.stream().filter(v -> v.type == OfferType.BUY).toList());
			buyEntries.sort((a, b) -> Double.compare(b.pricePerUnit, a.pricePerUnit));

			sellEntries.addAll(entries.stream().filter(v -> v.type == OfferType.SELL).toList());
			sellEntries.sort((a, b) -> Double.compare(a.pricePerUnit, b.pricePerUnit));

			int instantBuyCount = 0, instantSellCount = 0;
			double instantBuySum = 0, instantSellSum = 0;
			double instantBuyTop = Double.POSITIVE_INFINITY, instantSellTop = 0;

			for (int i = 0; i < Math.min(10, buyEntries.size()); i++) {
				OrderOffer entry = buyEntries.get(i);
				instantSellCount += entry.amount;
				instantSellSum += entry.amount * entry.pricePerUnit;
				if (entry.pricePerUnit > instantSellTop) instantSellTop = entry.pricePerUnit;
			}

			for (int i = 0; i < Math.min(10, sellEntries.size()); i++) {
				OrderOffer entry = sellEntries.get(i);
				instantBuyCount += entry.amount;
				instantBuySum += entry.amount * entry.pricePerUnit;
				if (entry.pricePerUnit < instantBuyTop) instantBuyTop = entry.pricePerUnit;
			}

			item.instantBuy = instantBuyCount == 0? null : instantBuyTop;
			item.instantSell = instantSellCount == 0? null : instantSellTop;
			item.avgInstantBuy = instantBuyCount == 0? null : instantBuySum / instantBuyCount;
			item.avgInstantSell = instantSellCount == 0? null : instantSellSum / instantSellCount;

			item.isUpdating = false;
		});
	}
}
