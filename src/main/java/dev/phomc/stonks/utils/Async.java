package dev.phomc.stonks.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

public class Async {
	public static <T> CompletableFuture<T> toAsync(Supplier<T> callback) {
		try {
			return CompletableFuture.completedFuture(callback.get());
		} catch (Throwable e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public static CompletableFuture<Void> toAsync(Runnable callback) {
		return toAsync(() -> {
			callback.run();
			return null;
		});
	}

	public static CompletableFuture<Void> sleep(long ms) {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		ForkJoinPool.commonPool().submit(() -> {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				cf.complete(null);
			}
		});
		return cf;
	}
}
