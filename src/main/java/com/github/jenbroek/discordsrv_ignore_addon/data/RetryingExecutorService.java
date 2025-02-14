package com.github.jenbroek.discordsrv_ignore_addon.data;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class RetryingExecutorService extends ScheduledThreadPoolExecutor {

	private final Duration retryDelay;
	private final int maxRetries;
	private final Function<Throwable, Boolean> shouldRetry;

	{
		super.setRemoveOnCancelPolicy(true);
	}

	public RetryingExecutorService(
		DiscordsrvIgnoreAddon plugin,
		int corePoolSize,
		Duration retryDelay,
		int maxRetries,
		Function<Throwable, Boolean> shouldRetry
	) {
		super(corePoolSize, (r, e) -> silentlyReject(plugin, r, e));
		this.retryDelay = retryDelay;
		this.maxRetries = maxRetries;
		this.shouldRetry = shouldRetry;
	}

	@Override
	public @NotNull ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
		// Need to wrap `command` because an `afterExecute()` hook wouldn't work; it becomes a
		// FutureTask internally, which cannot be restarted and whose `callable` gets cleared
		// after execution and isn't accessible anyways.
		//
		// Opted to not implemented `decorateTask` because ScheduledFutureTask cannot be extended,
		// and a lot of behavior would have to be re-implemented by ourselves otherwise otherwise.
		var task = new RetryingTask(command, maxRetries);
		return super.schedule(task, delay, unit);
	}

	@Override
	public void execute(@NotNull Runnable command) {
		// Copied from superclass, but using our own `schedule()` instead
		schedule(command, 0, TimeUnit.NANOSECONDS);
	}

	private static void silentlyReject(DiscordsrvIgnoreAddon p, Runnable r, ThreadPoolExecutor e) {
		if (e.isShutdown()) return;
		p.getLogger().warning("Silently rejecting task " + r.toString() + " from " + e);
	}

	public class RetryingTask implements Runnable {

		private final Runnable task;
		private int retriesLeft;

		public RetryingTask(Runnable task, int maxRetries) {
			this.task = task;
			this.retriesLeft = maxRetries;
		}

		@Override
		public void run() {
			try {
				task.run();
			} catch (Throwable t) {
				// Note we return and *then* decrement, otherwise the count is off by 1
				if (shouldRetry.apply(t) && (retriesLeft == -1 || retriesLeft-- > 0)) {
					schedule(this, retryDelay.toSeconds(), TimeUnit.SECONDS);
				}
			}
		}

	}

}
