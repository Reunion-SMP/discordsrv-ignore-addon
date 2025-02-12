package com.github.jenbroek.discordsrv_ignore_addon.data;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class RetryingExecutorService extends ScheduledThreadPoolExecutor {

	private final Duration retryDelay;
	private final Function<Throwable, Boolean> shouldRetry;

	{
		super.setRemoveOnCancelPolicy(true);
	}

	public RetryingExecutorService(
		DiscordsrvIgnoreAddon plugin,
		int corePoolSize,
		Duration retryDelay,
		Function<Throwable, Boolean> shouldRetry
	) {
		super(corePoolSize, (r, e) -> silentlyReject(plugin, r, e));
		this.retryDelay = retryDelay;
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
		Runnable cmd = () -> {
			try {
				command.run();
			} catch (Throwable t) {
				if (shouldRetry.apply(t)) {
					schedule(command, retryDelay.toSeconds(), TimeUnit.SECONDS);
				}
			}
		};
		return super.schedule(cmd, delay, unit);
	}

	@Override
	public void execute(@NotNull Runnable command) {
		// Copied from superclass, but using our own `schedule()` instead
		schedule(command, 0, TimeUnit.NANOSECONDS);
	}

	private static void silentlyReject(DiscordsrvIgnoreAddon p, Runnable r, Executor e) {
		p.getLogger().warning("Silently rejecting task " + r.toString() + " from " + e.toString());
	}

}
