package dev.phomc.stonks.utils;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Mono;

public class ReactorCoreUtils {
	public static <T> CompletableFuture<T> monoToCompletable(Mono<T> mono) {
		CompletableFuture<T> cf = new CompletableFuture<>();
		mono.subscribe(new Subscriber<T>() {
			private Subscription s;
			private boolean submitted = false;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(1);
			}

			@Override
			public void onNext(T t) {
				s.cancel();
				if (submitted) return;
				cf.complete(t);
			}

			@Override
			public void onError(Throwable t) {
				s.cancel();
				if (submitted) return;
				cf.completeExceptionally(t);
			}

			@Override
			public void onComplete() {
				s.cancel();
				if (submitted) return;
				cf.complete(null);
			}
		});
		return cf;
	}
}
