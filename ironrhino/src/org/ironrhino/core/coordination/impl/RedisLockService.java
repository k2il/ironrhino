package org.ironrhino.core.coordination.impl;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.coordination.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Singleton
@Named("lockService")
@Profile({ "dual","cloud" })
public class RedisLockService implements LockService {

	private static final String NAMESPACE = "{lock}";

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	@Override
	public boolean tryLock(String name) {
		String key = NAMESPACE + name;
		return stringRedisTemplate.opsForValue().setIfAbsent(key, "");
	}

	@Override
	public boolean tryLock(String name, long timeout, TimeUnit unit)
			throws InterruptedException {
		String key = NAMESPACE + name;
		boolean success = stringRedisTemplate.opsForValue()
				.setIfAbsent(key, "");
		long millisTimeout = unit.toMillis(timeout);
		long start = System.currentTimeMillis();
		while (!success) {
			Thread.sleep(1000);
			if ((System.currentTimeMillis() - start) >= millisTimeout)
				break;
			success = stringRedisTemplate.opsForValue().setIfAbsent(key, "");
		}
		return success;
	}

	@Override
	public void lock(String name) {
		String key = NAMESPACE + name;
		boolean success = stringRedisTemplate.opsForValue()
				.setIfAbsent(key, "");
		while (!success) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			success = stringRedisTemplate.opsForValue().setIfAbsent(key, "");
		}

	}

	@Override
	public void unlock(String name) {
		String key = NAMESPACE + name;
		stringRedisTemplate.delete(key);
	}

}
