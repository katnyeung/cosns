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

	public void incrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", "" + userId);
	}

	public void incrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", "" + userId);
	}

	public void decrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", "" + userId);
	}

	public void decrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", "" + userId);
	}

	public Long getLikeCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like");
	}

	public Long getRetweetCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet");
	}

	public boolean isLiked(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":like", "" + userId);
	}

	public boolean isRetweeted(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_UNIQUENAME_PREFIX + ":" + postId + ":retweet", "" + userId);
	}

	public void incrPostView(Long postId) {
		stringRedisTemplate.opsForValue().increment(ConstantsUtil.REDIS_POST_VIEW_PREFIX + ":" + postId);
	}

	public Long getPostView(Long postId) {
		String viewCountString = stringRedisTemplate.opsForValue().get(ConstantsUtil.REDIS_POST_VIEW_PREFIX + ":" + postId);
		if (viewCountString == null) {
			return (long) 0;
		} else {
			return Long.parseLong(viewCountString);
		}

	}
}
