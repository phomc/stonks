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
