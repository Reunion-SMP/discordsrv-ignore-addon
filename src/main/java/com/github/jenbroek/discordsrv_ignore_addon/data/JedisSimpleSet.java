package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

public class JedisSimpleSet<E> implements SimpleSet<E> {

	private final Set<E> delegate;
	private final Queue<Runnable> pendingOperations = new ConcurrentLinkedQueue<>();

	private final UnifiedJedis jedis;
	private final String key;
	private final Function<E, String> serializer;

	public JedisSimpleSet(
		Set<E> delegate,
		UnifiedJedis jedis,
		String key,
		Function<E, String> serializer,
		Function<String, E> deserializer
	) {
		this.delegate = delegate;

		this.jedis = jedis;
		this.key = key;
		this.serializer = serializer;

		delegate.addAll(jedis.smembers(key).stream().map(deserializer).toList());
	}

	public void sync() throws JedisException {
		var it = pendingOperations.iterator();
		while (it.hasNext()) {
			it.next().run();
			it.remove();
		}
	}

	@Override
	public boolean add(E e) {
		var value = serializer.apply(e);
		pendingOperations.add(() -> jedis.sadd(key, value));
		return delegate.add(e);
	}

	@Override
	public void remove(E e) {
		var value = serializer.apply(e);
		pendingOperations.add(() -> jedis.srem(key, value));
		delegate.remove(e);
	}

	@Override
	public boolean contains(E e) {
		return delegate.contains(e);
	}

}
