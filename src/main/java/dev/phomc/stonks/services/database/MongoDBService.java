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

package dev.phomc.stonks.services.database;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;

import dev.phomc.stonks.Stonks;
import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.markets.MarketItem;
import dev.phomc.stonks.modules.ItemIdsConverter;
import dev.phomc.stonks.offers.InstantTrade;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.services.StonksService;
import dev.phomc.stonks.utils.GeneralConverter;
import dev.phomc.stonks.utils.ReactorCoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MongoDBService implements StonksService {
	public static final String QUICK_INFO_AGGREGATION_PIPELINE = """
			[
			  {
			    $match: {
			      $expr: {
			        $and: [
			          { $gte: ["$expireOn", 123] },
			          { $lt: ["$filled", "$amount"] },
			        ]
			      }
			    }
			  },
			  {
			    $group: {
			      _id: "$item",
			      offers: {
			        $addToSet: {
			          type: "$type",
			          amount: "$amount",
			          pricePerUnit: "$pricePerUnit",
			          player: "$player",
			          offerId: "$_id",
			        },
			      },
			    },
			  },
			  {
			    $project: {
			      _id: 1,
			      buyOffers: {
			        $sortArray: {
			          input: {
			            $filter: {
			              input: "$offers",
			              as: "obj",
			              cond: { $eq: ["$$obj.type", "BUY"] },
			            },
			          },
			          sortBy: -1,
			        },
			      },
			      sellOffers: {
			        $sortArray: {
			          input: {
			            $filter: {
			              input: "$offers",
			              as: "obj",
			              cond: { $eq: ["$$obj.type", "SELL"] },
			            },
			          },
			          sortBy: 1,
			        },
			      },
			    },
			  },
			  {
			    $set: {
			      topBuy: { $arrayElemAt: ["$buyOffers", 0] },
			      topSell: { $arrayElemAt: ["$sellOffers", 0] },
			      averageBuy: {
			        $avg: {
			          $map: {
			            input: "$buyOffers",
			            as: "obj",
			            in: "$$obj.pricePerUnit",
			          },
			        },
			      },
			      averageSell: {
			        $avg: {
			          $map: {
			            input: "$sellOffers",
			            as: "obj",
			            in: "$$obj.pricePerUnit",
			          },
			        },
			      },
			    },
			  },
			]
			""";

	public final MongoCollection<Document> collection;
	protected Runnable onClose;

	private long updateInterval = 10_000;
	private ItemIdsConverter itemIds = ItemIdsConverter.DEFAULT_CONVERTER;
	private List<Bson> quickInfoAggregationPipeline;

	public MongoDBService(MongoCollection<Document> collection) {
		this.collection = collection;
		this.quickInfoAggregationPipeline = BsonArray.parse(QUICK_INFO_AGGREGATION_PIPELINE).stream().map(v -> (Bson) v.asDocument()).toList();
	}

	@Override
	public void onAttached(Market market) {
		itemIds = market.itemIds;
		updateInterval = market.updateInterval;
	}

	@Override
	public CompletableFuture<OrderOffer> getOffer(ObjectId offerId) {
		if (offerId == null) return CompletableFuture.failedFuture(new NullPointerException("'offerId' is null"));

		return Flux.from(collection.find(new Document("_id", offerId)))
			.singleOrEmpty()
			.map(doc -> new OrderOffer(doc, itemIds))
			.as(ReactorCoreUtils::monoToCompletable);
	}

	@Override
	public CompletableFuture<OrderOffer[]> getOffers(UUID player) {
		if (player == null) return CompletableFuture.failedFuture(new NullPointerException("'player' is null"));

		return Flux.from(collection.find(new Document("player", GeneralConverter.uuidToDashless(player))))
			.map(doc -> new OrderOffer(doc, itemIds))
			.collectList()
			.map(list -> list.toArray(OrderOffer[]::new))
			.as(ReactorCoreUtils::monoToCompletable);
	}

	@Override
	public CompletableFuture<Void> makeOffer(OrderOffer offer) {
		if (offer == null) return CompletableFuture.failedFuture(new NullPointerException("'offer' is null"));
		if (offer.offerId == null) offer.offerId = ObjectId.get();

		return Flux.from(collection.find(new Document("_id", offer.offerId)))
			.singleOrEmpty()
			.hasElement()
			.map(alreadyExists -> {
				if (alreadyExists) return CompletableFuture.failedFuture(new IllegalArgumentException("offer id " + offer.offerId + " already exists!"));
				Document doc = offer.createNewOfferDocument(itemIds);
				return Mono.from(collection.insertOne(doc)).as(ReactorCoreUtils::monoToCompletable);
			})
			.as(ReactorCoreUtils::monoToCompletable)
			.thenCompose(obj -> obj.thenRun(() -> {}));
	}

	@Override
	public CompletableFuture<OrderOffer> cancelOffer(ObjectId offerId) {
		if (offerId == null) return CompletableFuture.failedFuture(new NullPointerException("'offerId' is null"));

		return Flux.from(collection.find(new Document("_id", offerId)))
			.map(doc -> new OrderOffer(doc, itemIds))
			.single(OrderOffer.EMPTY)
			.map(offer -> {
				if (offer == OrderOffer.EMPTY) return CompletableFuture.completedFuture((OrderOffer) null);
				return Mono.from(collection.deleteOne(new Document("_id", offerId)))
					.map(result -> offer)
					.as(ReactorCoreUtils::monoToCompletable);
			})
			.as(ReactorCoreUtils::monoToCompletable)
			.thenCompose(cf -> cf);
	}

	@Override
	public CompletableFuture<Integer> claimItems(ObjectId offerId) {
		if (offerId == null) return CompletableFuture.failedFuture(new NullPointerException("'offerId' is null"));

		return Flux.from(collection.find(new Document("_id", offerId)))
			.map(doc -> new OrderOffer(doc, itemIds))
			.single(OrderOffer.EMPTY)
			.map(offer -> {
				if (offer == OrderOffer.EMPTY) return CompletableFuture.failedFuture(new NullPointerException("Offer with id = " + offerId + " does not exists"));

				int delta = offer.filled - offer.claimed;
				offer.claimed = offer.filled;

				if (offer.claimed == offer.amount) {
					// Delete
					return Mono.from(collection.deleteOne(new Document("_id", offerId)))
						.map(result -> delta)
						.as(ReactorCoreUtils::monoToCompletable);
				} else {
					// Set new
					return Mono.from(collection.replaceOne(new Document("_id", offerId), offer.createNewOfferDocument(itemIds)))
						.map(result -> delta)
						.as(ReactorCoreUtils::monoToCompletable);
				}
			})
			.map(cf -> cf.thenApply(obj -> (int) obj))
			.as(ReactorCoreUtils::monoToCompletable)
			.thenCompose(cf -> cf);
	}

	@Override
	public CompletableFuture<InstantTrade> executeInstantTrade(InstantTrade trade) {
		CompletableFuture<InstantTrade> cf = new CompletableFuture<>();

		Flux.from(collection.aggregate(Arrays.asList(
				Aggregates.match(Filters.expr(Filters.and(
						new Document("$gte", Arrays.asList("$expireOn", System.currentTimeMillis())),
						new Document("$lt", Arrays.asList("$filled", "$amount")),
						new Document("$eq", Arrays.asList("$type", (trade.instantMode == OfferType.BUY? OfferType.SELL : OfferType.BUY).toString()))
						))),
				Aggregates.sort(new Document("pricePerUnit", trade.instantMode == OfferType.BUY? 1 : -1))
				)))
			.map(doc -> new OrderOffer(doc, itemIds))
			.subscribe(new Subscriber<OrderOffer>() {
				private Subscription s;
				private boolean cancelled = false;

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(1);
				}

				@Override
				public void onNext(OrderOffer entry) {
					if (cancelled) return;

					if (trade.instantMode == OfferType.BUY) {
						int canPay = (int) Math.floor(trade.budget / entry.pricePerUnit); // how many items we can pay for
						int itemsNeeded = Math.min(entry.amount - entry.filled, trade.amount); // how many items we needed from this offer
						int chooseToPay = Math.min(canPay, itemsNeeded); // how many items we can get

						trade.amount -= chooseToPay;
						trade.budget -= chooseToPay * entry.pricePerUnit;
						entry.filled += chooseToPay;

						if (trade.amount == 0 || chooseToPay != itemsNeeded) cancelled = true;
					} else {
						int itemsSold = Math.min(entry.amount - entry.filled, trade.amount);
						trade.amount -= itemsSold;
						trade.budget += itemsSold * entry.pricePerUnit;
						entry.filled += itemsSold;

						if (trade.amount == 0) cancelled = true;
					}

					Mono.from(collection.replaceOne(new Document("_id", entry.offerId), entry.createNewOfferDocument(itemIds)))
						.as(ReactorCoreUtils::monoToCompletable)
						.thenRun(() -> {
							if (cancelled) {
								s.cancel();
								cf.complete(trade);
							} else {
								s.request(1);
							}
						});
				}

				@Override
				public void onError(Throwable t) {
					cancelled = true;
					s.cancel();
					cf.completeExceptionally(t);
				}

				@Override
				public void onComplete() {
					cancelled = true;
					cf.complete(trade);
				}
			});

		return cf;
	}

	// Update stage
	private long quickInfoLastUpdate = 0;
	private Map<String, Double[]> quickInfoValues = new HashMap<>();

	@Override
	public CompletableFuture<Void> updateProductQuickInfo(MarketItem item) {
		long currentTime = System.currentTimeMillis();
		boolean shouldUpdate = currentTime >= (quickInfoLastUpdate + updateInterval);
		if (shouldUpdate) quickInfoValues.clear();

		CompletableFuture<Void> cf = shouldUpdate? Flux.from(collection.aggregate(quickInfoAggregationPipeline))
			.map(doc -> {
				String itemId = doc.getString("_id");
				Double averageBuy = doc.getDouble("averageBuy");
				Double averageSell = doc.getDouble("averageSell");
				Document topBuyOffer = doc.get("topBuy", Document.class);
				Document topSellOffer = doc.get("topSell", Document.class);
				Double topBuy = topBuyOffer != null? topBuyOffer.getDouble("pricePerUnit") : null;
				Double topSell = topSellOffer != null? topSellOffer.getDouble("pricePerUnit") : null;
				quickInfoValues.put(itemId, new Double[] { averageBuy, averageSell, topBuy, topSell });
				return 1;
			})
			.all(v -> true)
			.as(ReactorCoreUtils::monoToCompletable)
			.thenRun(() -> {}) : CompletableFuture.completedFuture(null);
		return cf.thenRun(() -> {
			String id = itemIds.fromItemStack(item.item);
			Double[] values = quickInfoValues.getOrDefault(id, new Double[4]);
			item.avgInstantSell = values[0];
			item.avgInstantBuy = values[1];
			item.instantSell = values[2];
			item.instantBuy = values[3];
		});
	}

	@Override
	public void close() throws IOException {
		if (onClose != null) {
			Stonks.LOGGER.info("Closing MongoDBService...");
			onClose.run();
		}
	}
}
