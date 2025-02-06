package com.github.jenbroek.discordsrv_ignore_addon.data;

public interface SimpleMap<K, V> {

	void put(K key, V value);

	V get(K key);

	V getOrDefault(K key, V defaultValue);

}
