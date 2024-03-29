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
import dev.phomc.stonks.services.StonksService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class Market {
	// Shared constants
	public static final int ORDER_DURATION = 1000 * 60 * 60 * 24 * 12; // 12 days

	public final List<MarketCategory> categories = new ArrayList<>();
	public final Map<UUID, TempPlayerData> temporaryData = new HashMap<>();
	public final StonksService service;

	public long updateInterval = 10 * 1000; // 10 seconds before next update become available

	public ItemIdsConverter itemIds = ItemIdsConverter.DEFAULT_CONVERTER;
	public ItemsComparator itemsComparator = ItemsComparator.DEFAULT_COMPARATOR;
	public CurrencyHandler currency = CurrencyHandler.DEFAULT_HANDLER;

	public Market(StonksService service) {
		this.service = service;
		this.service.onAttached(this);
	}

	public void updateQuickInfo(MarketItem item, boolean forced) {
		if (!forced && !item.shouldUpdate()) return;
		if (item.isUpdating) return;

		Stonks.LOGGER.info("Updating quick info for {}", item.item.getHoverName().getString());

		service.updateProductQuickInfo(item).thenRun(() -> {
			item.nextUpdate = System.currentTimeMillis() + updateInterval;
		}).exceptionally(t -> {
			t.printStackTrace();
			item.isUpdating = false;
			return null;
		});
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
			Stonks.LOGGER.info("Updating offers list for {}", data.playerId);
			data.offers.clear();
			data.offers.addAll(Arrays.asList(offers));
			data.nextOffersListUpdateLock = false;
			data.nextOffersListUpdate = System.currentTimeMillis() + updateInterval;
		});
	}

	public static Market setupDefaults(Market market) {
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

		market.categories.add(new MarketCategory()
				.builder(e -> e
						.setItem(Items.WHEAT)
						.setName(Component.literal("Farming")))
				.add(new MarketItem(Items.WHEAT.getDefaultInstance()))
				.add(new MarketItem(Items.WHEAT_SEEDS.getDefaultInstance()))
				.add(new MarketItem(Items.POTATO.getDefaultInstance()))
				.add(new MarketItem(Items.CARROT.getDefaultInstance()))
				.add(new MarketItem(Items.SUGAR_CANE.getDefaultInstance()))
				.add(new MarketItem(Items.BEETROOT.getDefaultInstance()))
				.add(new MarketItem(Items.BEEF.getDefaultInstance()))
				.add(new MarketItem(Items.CHICKEN.getDefaultInstance())));

		return market;
	}

	public static Market from(MinecraftServer server) {
		return ((MinecraftServerBridge) server).getMarket();
	}

	public static Market from(ServerPlayer player) {
		return from(player.getServer());
	}
}
