package dev.phomc.stonks.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bson.types.ObjectId;

import dev.phomc.stonks.offers.OrderOffer;
import net.minecraft.server.level.ServerPlayer;

public interface StonksServiceProvider {
	CompletableFuture<OrderOffer> getOffer(ObjectId offerId);
	CompletableFuture<OrderOffer[]> getOffers(UUID player);
	CompletableFuture<Void> makeOffer(OrderOffer offer);
	CompletableFuture<Void> cancelOffer(ObjectId offerId);

	default CompletableFuture<OrderOffer[]> getOffers(ServerPlayer player) {
		return getOffers(player.getUUID());
	}
}
