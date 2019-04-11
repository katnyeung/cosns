package org.cosns.service;

import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public void addSetItem(String key, String value) {
		stringRedisTemplate.opsForSet().add(key, value);
	}

	public Set<String> findKeys(String query) {
		return stringRedisTemplate.keys("*" + query + "*");
	}

	public Set<String> getSetItems(String key) {
		return stringRedisTemplate.opsForSet().members(key);
	}

	public void setValue(String key, String value) {
		stringRedisTemplate.opsForValue().set(key, value);
	}

	public String getValue(String key) {
		return stringRedisTemplate.opsForValue().get(key);
	}
}
