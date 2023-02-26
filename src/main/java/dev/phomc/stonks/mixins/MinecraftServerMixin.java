package dev.phomc.stonks.mixins;

import java.net.Proxy;
import java.text.DecimalFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixer;

import dev.phomc.stonks.Stonks;
import dev.phomc.stonks.bridges.MinecraftServerBridge;
import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.services.memory.MemoryServiceProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerBridge {
	@Unique private Market market;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(Thread thread, LevelStorageSource.LevelStorageAccess worldStorage, PackRepository packs, WorldStem worldStem,
			Proxy proxy, DataFixer df, Services services, ChunkProgressListenerFactory chunkProgress, CallbackInfo ci) {
		long initStart = System.nanoTime();
		market = Market.createDefaultMarket(new MemoryServiceProvider());
		Stonks.LOGGER.info("Stonks initialized in {}ms", new DecimalFormat("#,##0.##").format((System.nanoTime() - initStart) / 1_000_000D));
	}

	@Override
	public Market getMarket() {
		return market;
	}
}
