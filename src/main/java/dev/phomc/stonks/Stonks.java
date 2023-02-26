package dev.phomc.stonks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.phomc.stonks.commands.StonksCommand;
import dev.phomc.stonks.markets.Market;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
}
