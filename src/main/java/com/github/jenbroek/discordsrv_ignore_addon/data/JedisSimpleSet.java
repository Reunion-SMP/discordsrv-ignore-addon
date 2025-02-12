package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import redis.clients.jedis.UnifiedJedis;

public class JedisSimpleSet<E> extends JedisBacked implements SimpleSet<E> {

	private final Set<E> delegate;
	private final Function<E, String> serializer;

	public JedisSimpleSet(
		UnifiedJedis jedis,
		String key,
		ExecutorService executor,
		Set<E> delegate,
		Function<E, String> serializer,
		Function<String, E> deserializer
	) {
		super(jedis, key, executor);

		this.delegate = delegate;
		this.serializer = serializer;

		delegate.addAll(jedis.smembers(key).stream().map(deserializer).toList());
	}

	@Override
	public boolean add(E e) {
		var svalue = serializer.apply(e);
		executor.execute(() -> jedis.sadd(super.key, svalue));
		return delegate.add(e);
	}

	@Override
	public void remove(E e) {
		var svalue = serializer.apply(e);
		executor.execute(() -> jedis.srem(super.key, svalue));
		delegate.remove(e);
	}

	@Override
	public boolean contains(E e) {
		return delegate.contains(e);
	}

}
