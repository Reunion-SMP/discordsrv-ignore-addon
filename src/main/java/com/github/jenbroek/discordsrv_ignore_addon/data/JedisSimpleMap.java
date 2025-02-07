package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

public class JedisSimpleMap<K, V> implements SimpleMap<K, V> {

	private final Map<K, V> delegate;
	private final Queue<Runnable> pendingOperations = new ConcurrentLinkedQueue<>();

	private final UnifiedJedis jedis;
	private final String key;
	private final Function<K, String> keySerializer;
	private final Function<V, String> valueSerializer;

	public JedisSimpleMap(
		Map<K, V> delegate,
		UnifiedJedis jedis,
		String key,
		Function<K, String> keySerializer,
		Function<String, K> keyDeserializer,
		Function<V, String> valueSerializer,
		Function<String, V> valueDeserializer
	) {
		this.delegate = delegate;

		this.jedis = jedis;
		this.key = key;
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;

		delegate.putAll(
			jedis.hgetAll(key).entrySet()
			     .stream()
			     .filter(e -> !e.getValue().isEmpty())
			     .collect(
				     Collectors.toMap(
					     e -> keyDeserializer.apply(e.getKey()),
					     e -> valueDeserializer.apply(e.getValue())
				     )
			     )
		);
	}

	public void sync() throws JedisException {
		var it = pendingOperations.iterator();
		while (it.hasNext()) {
			it.next().run();
			it.remove();
		}
	}

	@Override
	public void put(K key, V value) {
		var skey = keySerializer.apply(key);
		var svalue = valueSerializer.apply(value);

		pendingOperations.add(
			() -> {
				if (svalue.isEmpty()) {
					jedis.hdel(this.key, skey);
				} else {
					jedis.hset(this.key, skey, svalue);
				}
			}
		);

		delegate.put(key, value);
	}

	@Override
	public V get(K key) {
		return delegate.get(key);
	}

	@Override
	public V getOrDefault(K key, V defaultValue) {
		return delegate.getOrDefault(key, defaultValue);
	}

}
