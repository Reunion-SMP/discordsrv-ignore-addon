package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import redis.clients.jedis.UnifiedJedis;

public class JedisSimpleMap<K, V> extends JedisBacked implements SimpleMap<K, V> {

	private final Map<K, V> delegate;
	private final Function<K, String> keySerializer;
	private final Function<V, String> valueSerializer;

	public JedisSimpleMap(
		UnifiedJedis jedis,
		String key,
		ExecutorService executor,
		Map<K, V> delegate,
		Function<K, String> keySerializer,
		Function<String, K> keyDeserializer,
		Function<V, String> valueSerializer,
		Function<String, V> valueDeserializer
	) {
		super(jedis, key, executor);

		this.delegate = delegate;
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

	@Override
	public void put(K key, V value) {
		var skey = keySerializer.apply(key);
		var svalue = valueSerializer.apply(value);

		if (svalue.isEmpty()) {
			executor.execute(() -> jedis.hdel(super.key, skey));
		} else {
			executor.execute(() -> jedis.hset(super.key, skey, svalue));
		}

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
