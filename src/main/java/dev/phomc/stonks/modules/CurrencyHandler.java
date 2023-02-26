package dev.phomc.stonks.modules;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dev.phomc.stonks.Stonks;

/**
 * <p>All things related to server currency.</p>
 * @author nahkd
 *
 */
public interface CurrencyHandler {
	CompletableFuture<Void> send(UUID recipient, double amount);
	CompletableFuture<Void> take(UUID recipient, double amount);
	CompletableFuture<Double> get(UUID recipient);

	public static final CurrencyHandler DEFAULT_HANDLER = new CurrencyHandler() {
		private void logTx(UUID recipient, double amount) {
			Stonks.LOGGER.warn("TRANSACTION: {} {} {}, but Market#currency is using default handler", recipient, amount > 0? "->" : "<-", new DecimalFormat("#,##0.##").format(amount));
		}

		@Override
		public CompletableFuture<Void> take(UUID recipient, double amount) {
			logTx(recipient, -amount);
			return CompletableFuture.completedFuture(null);
		}
		
		@Override
		public CompletableFuture<Void> send(UUID recipient, double amount) {
			logTx(recipient, amount);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Double> get(UUID recipient) {
			return CompletableFuture.completedFuture(100_000_000D);
		}
	};
}
