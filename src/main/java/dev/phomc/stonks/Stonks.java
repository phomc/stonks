package dev.phomc.stonks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.phomc.stonks.commands.StonksCommand;
import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.services.StonksService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Stonks implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Stonks");

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(StonksCommand.market());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			Market.from(server).temporaryData.remove(handler.getPlayer().getUUID());
		});
	}

	/**
	 * <p>Configure player markets by adding your own listener to this event.</p>
	 */
	public static final Event<MarketConfigurator> CONFIGURE_MARKET = EventFactory.createArrayBacked(MarketConfigurator.class, listeners -> market -> {
		for (MarketConfigurator configurator : listeners) {
			configurator.configure(market);
		}
	});

	public static interface MarketConfigurator {
		void configure(Market market);
	}

	/**
	 * <p>Add your own service provider by adding listener that returns your service here. If the provider returns anything
	 * other than {@code null}, the services provider finder will stop and return that service.</p>
	 */
	public static final Event<MarketServiceProvider> FIND_SERVICE = EventFactory.createArrayBacked(MarketServiceProvider.class, listeners -> prev -> {
		for (MarketServiceProvider provider : listeners) {
			if (prev != null) break;
			prev = provider.findServiceProvider(prev);
		}

		return prev;
	});

	public static interface MarketServiceProvider {
		StonksService findServiceProvider(StonksService prev);
	}
}
