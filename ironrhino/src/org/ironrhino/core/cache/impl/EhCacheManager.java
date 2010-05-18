package org.ironrhino.core.cache.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;

public class EhCacheManager implements CacheManager {

	private net.sf.ehcache.CacheManager ehCacheManager;

	public EhCacheManager(net.sf.ehcache.CacheManager ehCacheManager) {
		this.ehCacheManager = ehCacheManager;
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
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			ehCacheManager.addCache(namespace);
		cache = ehCacheManager.getCache(namespace);
		cache.put(new Element(key, value, null, timeToIdle > 0 ? Integer
				.valueOf(timeToIdle) : null,
				timeToIdle <= 0 && timeToLive > 0 ? Integer.valueOf(timeToLive)
						: null));
	}

	public Serializable get(String key, String namespace) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		Element element = cache.get(key);
		return element != null ? element.getValue() : null;

	}

	public void delete(String key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			cache.remove(key);
	}

	public void mput(Map<String, Object> map, int timeToIdle, int timeToLive,
			String namespace) {
		if (map == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			ehCacheManager.addCache(namespace);
		cache = ehCacheManager.getCache(namespace);
		for (Map.Entry<String, Object> entry : map.entrySet())
			cache.put(new Element(entry.getKey(), entry.getValue(), null,
					timeToIdle > 0 ? Integer.valueOf(timeToIdle) : null,
					timeToIdle <= 0 && timeToLive > 0 ? Integer
							.valueOf(timeToLive) : null));
	}

	public Map<String, Object> mget(Collection<String> keys, String namespace) {
		if (keys == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		return cache.getAllWithLoader(keys, null);
	}

	public void mdelete(Collection<String> keys, String namespace) {
		if (keys == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			for (Serializable key : keys)
				cache.remove(key);
	}

	public boolean supportsTimeToIdle() {
		return true;
	}

}
