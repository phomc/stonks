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
import dev.phomc.stonks.services.StonksService;
import dev.phomc.stonks.services.memory.MemoryService;
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

		StonksService service = Stonks.FIND_SERVICE.invoker().findServiceProvider(null);
		if (service == null) {
			Stonks.LOGGER.warn("Stonks couldn't find any service provider. Do you have any providers installed? Failback to memory-based service...");
			Stonks.LOGGER.warn("Memory-based service will erases everything when the world is unloaded.");
			service = new MemoryService();
		}

		market = new Market(service);
		Stonks.CONFIGURE_MARKET.invoker().configure(market);
		if (market.categories.size() == 0) {
			Stonks.LOGGER.warn("No categories for market. Using default categories set.");
			Market.setupDefaults(market);
		}

		Stonks.LOGGER.info("Stonks initialized in {}ms", new DecimalFormat("#,##0.##").format((System.nanoTime() - initStart) / 1_000_000D));
	}

	@Override
	public Market getMarket() {
		return market;
	}
}
