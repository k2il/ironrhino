package org.ironrhino.core.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisCacheManager implements CacheManager {

	private Logger log = LoggerFactory.getLogger(getClass());

	private RedisTemplate redisTemplate;

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void put(String key, Object value, int timeToLive, String namespace) {
		put(key, value, -1, timeToLive, namespace);
	}

	public void put(String key, Object value, int timeToIdle, int timeToLive,
			String namespace) {
		if (key == null || value == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			if (timeToLive > 0)
				redisTemplate.opsForValue().set(generateKey(key, namespace),
						value, timeToLive, TimeUnit.SECONDS);
			else
				redisTemplate.opsForValue().set(generateKey(key, namespace),
						value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object get(String key, String namespace) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			return redisTemplate.opsForValue().get(generateKey(key, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public Object get(String key, String namespace, int timeToLive) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		String actualKey = generateKey(key, namespace);
		redisTemplate.expire(actualKey, timeToLive, TimeUnit.SECONDS);
		return redisTemplate.opsForValue().get(actualKey);
	}

	public void delete(String key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			redisTemplate.delete(generateKey(key, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void mput(Map<String, Object> map, final int timeToLive,
			String namespace) {
		if (map == null)
			return;
		for (Map.Entry<String, Object> entry : map.entrySet())
			put(entry.getKey(), entry.getValue(), timeToLive, namespace);
		try {
			final Map<byte[], byte[]> actualMap = new HashMap<byte[], byte[]>();
			for (Map.Entry<String, Object> entry : map.entrySet())
				actualMap.put(
						redisTemplate.getKeySerializer().serialize(
								generateKey(entry.getKey(), namespace)),
						redisTemplate.getValueSerializer().serialize(
								entry.getValue()));
			redisTemplate.execute(new RedisCallback() {
				@Override
				public Object doInRedis(RedisConnection conn)
						throws DataAccessException {
					conn.multi();
					conn.mSet(actualMap);
					for (byte[] k : actualMap.keySet())
						conn.expire(k, timeToLive);
					conn.exec();
					return null;
				}
			});

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void mput(Map<String, Object> map, int timeToIdle, int timeToLive,
			String namespace) {
		if (map == null)
			return;
		mput(map, timeToLive, namespace);
	}

	public Map<String, Object> mget(Collection<String> keys, String namespace) {
		if (keys == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		final List<byte[]> _keys = new ArrayList<byte[]>();
		for (String key : keys)
			_keys.add(redisTemplate.getKeySerializer().serialize(
					generateKey(key, namespace)));
		try {
			List<byte[]> values = (List<byte[]>) redisTemplate
					.execute(new RedisCallback<List<byte[]>>() {
						@Override
						public List<byte[]> doInRedis(RedisConnection conn)
								throws DataAccessException {
							return conn.mGet(_keys.toArray(new byte[0][0]));
						}
					});
			Map<String, Object> map = new HashMap<String, Object>();
			int i = 0;
			for (String key : keys) {
				map.put(key,
						redisTemplate.getValueSerializer().deserialize(
								values.get(i)));
				i++;
			}
			return map;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void mdelete(final Collection<String> keys, String namespace) {
		if (keys == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		final String ns = namespace;
		try {
			redisTemplate.execute(new RedisCallback() {
				@Override
				public Object doInRedis(RedisConnection conn)
						throws DataAccessException {
					conn.multi();
					for (String key : keys)
						conn.del(redisTemplate.getKeySerializer().serialize(
								generateKey(key, ns)));
					conn.exec();
					return null;
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		for (String key : keys)
			delete(key, namespace);
	}

	public boolean containsKey(String key, String namespace) {
		if (key == null)
			return false;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			return redisTemplate.hasKey(generateKey(key, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean putIfAbsent(String key, Object value, int timeToLive,
			String namespace) {
		try {
			return redisTemplate.opsForValue().setIfAbsent(
					generateKey(key, namespace), value);
		} catch (Exception e) {
			return false;
		}
	}

	private String generateKey(String key, String namespace) {
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(':');
		sb.append(key);
		return sb.toString();
	}

	public boolean supportsTimeToIdle() {
		return false;
	}

	public boolean supportsUpdateTimeToLive() {
		return true;
	}

}
