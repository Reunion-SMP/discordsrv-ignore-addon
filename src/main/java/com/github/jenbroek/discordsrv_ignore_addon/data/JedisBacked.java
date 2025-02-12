package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.concurrent.ExecutorService;
import redis.clients.jedis.UnifiedJedis;

public abstract class JedisBacked {

	protected final UnifiedJedis jedis;
	protected final String key;
	protected final ExecutorService executor;

	protected JedisBacked(UnifiedJedis jedis, String key, ExecutorService executor) {
		this.jedis = jedis;
		this.key = key;
		this.executor = executor;
	}

}
