package dev.phomc.stonks.markets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.phomc.stonks.Stonks;
import dev.phomc.stonks.bridges.MinecraftServerBridge;
import dev.phomc.stonks.modules.CurrencyHandler;
import dev.phomc.stonks.modules.ItemIdsConverter;
import dev.phomc.stonks.modules.ItemsComparator;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.services.StonksServiceProvider;
import dev.phomc.stonks.utils.Async;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class Market {
	public final List<MarketCategory> categories = new ArrayList<>();
	public final Map<UUID, TempPlayerData> temporaryData = new HashMap<>();
	public final StonksServiceProvider service;

	protected long updateInterval = 30 * 1000; // 30 seconds before next update become available

	protected ItemIdsConverter itemIds = ItemIdsConverter.DEFAULT_CONVERTER;
	protected ItemsComparator itemsComparator = ItemsComparator.DEFAULT_COMPARATOR;
	protected CurrencyHandler currency = CurrencyHandler.DEFAULT_HANDLER;

	public Market(StonksServiceProvider service) {
		this.service = service;
	}

	public void updateQuickInfo(MarketItem item, boolean forced) {
		if (!forced && !item.shouldUpdate()) return;

		// TODO: Aggregate quick info
		item.nextUpdate = System.currentTimeMillis() + updateInterval;
	}

	public TempPlayerData getTemporaryData(UUID playerId) {
		TempPlayerData data = temporaryData.get(playerId);
		if (data == null) temporaryData.put(playerId, data = new TempPlayerData(playerId));
		return data;
	}

	public TempPlayerData getTemporaryData(ServerPlayer player) {
		return getTemporaryData(player.getUUID());
	}

	public void updateOffersList(TempPlayerData data, boolean forced) {
		if (!forced && System.currentTimeMillis() <= data.nextOffersListUpdate) return;
		if (data.nextOffersListUpdateLock) return;

		data.nextOffersListUpdateLock = true;
		service.getOffers(data.playerId).thenAccept(offers -> {
			Stonks.LOGGER.info("update begin");
			data.offers.clear();
			data.offers.addAll(Arrays.asList(offers));
		})
		.thenCompose($_ -> Async.sleep(700)) // TODO: fake sleep, we are testing the lag thing, please remove in prod.
		.thenRun(() -> {
			// TODO: fake order
			data.offers.add(new OrderOffer(data.playerId, OfferType.BUY, Items.DIAMOND.getDefaultInstance(), 100, 15, System.currentTimeMillis() + 1000));

			Stonks.LOGGER.info("update done");
			data.nextOffersListUpdateLock = false;
			data.nextOffersListUpdate = System.currentTimeMillis() + updateInterval;
		});
	}

	public static Market createDefaultMarket(StonksServiceProvider service) {
		Market market = new Market(service);
		market.categories.add(new MarketCategory()
				.builder(e -> e
						.setItem(Items.IRON_ORE)
						.setName(Component.literal("Materials")))
				.add(new MarketItem(Items.IRON_INGOT.getDefaultInstance()))
				.add(new MarketItem(Items.GOLD_INGOT.getDefaultInstance()))
				.add(new MarketItem(Items.DIAMOND.getDefaultInstance()))
				.add(new MarketItem(Items.EMERALD.getDefaultInstance()))
				.add(new MarketItem(Items.AMETHYST_SHARD.getDefaultInstance()))
				.add(new MarketItem(Items.REDSTONE.getDefaultInstance()))
				.add(new MarketItem(Items.LAPIS_LAZULI.getDefaultInstance()))
				.add(new MarketItem(Items.QUARTZ.getDefaultInstance()))
				.add(new MarketItem(Items.ANCIENT_DEBRIS.getDefaultInstance()))
				.add(new MarketItem(Items.NETHERITE_INGOT.getDefaultInstance())));
		return market;
	}

	public static Market from(MinecraftServer server) {
		return ((MinecraftServerBridge) server).getMarket();
	}

	public static Market from(ServerPlayer player) {
		return from(player.getServer());
	}
}
