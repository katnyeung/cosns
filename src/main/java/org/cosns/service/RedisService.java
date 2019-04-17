package org.cosns.service;

import java.util.Set;
import java.util.logging.Logger;

import org.cosns.util.ConstantsUtil;
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

	public void incrLike(Long postId) {
		logger.info("incr : " + ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like");
		stringRedisTemplate.opsForValue().increment(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", 1);
	}

	public void incrRetweet(Long postId) {

		stringRedisTemplate.opsForValue().increment(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", 1);
	}

	public void decrLike(Long postId) {
		logger.info("decr : " + ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like");
		stringRedisTemplate.opsForValue().decrement(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", 1);
	}

	public void decrRetweet(Long postId) {
		stringRedisTemplate.opsForValue().decrement(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", 1);
	}

	public int getLikeCount(Long postId) {

		String likeCountString = stringRedisTemplate.opsForValue().get(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like");

		if (likeCountString == null) {
			stringRedisTemplate.opsForValue().set(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", "0");
			likeCountString = "0";
			logger.info("setting like for postId : 0");
		}

		return Integer.parseInt(likeCountString);
	}

	public int getRetweetCount(Long postId) {
		String retweetCountString = stringRedisTemplate.opsForValue().get(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet");

		if (retweetCountString == null) {
			stringRedisTemplate.opsForValue().set(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", "0");
			retweetCountString = "0";
		}

		return Integer.parseInt(retweetCountString);
	}
}
