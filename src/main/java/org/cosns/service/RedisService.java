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

	public Object getHashItems(String key, String type) {
		return stringRedisTemplate.opsForHash().get(key, type);
	}

	public void setValue(String key, String type, Object value) {
		stringRedisTemplate.opsForHash().put(key, type, value);
	}

	public Object getValue(String key, String type) {
		return stringRedisTemplate.opsForHash().get(key, type);
	}

	public void incrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_LIKE, "" + userId);
	}

	public void incrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_RETWEET, "" + userId);
	}

	public void decrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_LIKE, "" + userId);
	}

	public void decrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_RETWEET, "" + userId);
	}

	public Long getLikeCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_LIKE);
	}

	public Long getRetweetCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_RETWEET);
	}

	public boolean isLiked(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_LIKE, "" + userId);
	}

	public boolean isRetweeted(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_GROUP + ":" + postId + ":" + ConstantsUtil.REDIS_POST_TYPE_RETWEET, "" + userId);
	}

	public void incrTotalPostView(Long postId) {
		stringRedisTemplate.opsForHash().increment(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY, (long) 1);
	}

	public Long getTotalPostView(Long postId) {
		Long viewCount = (Long) stringRedisTemplate.opsForHash().get(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TOTAL);
		if (viewCount == null) {
			return (long) 0;
		} else {
			return viewCount;
		}
	}

	public void incrTodayPostView(Long postId) {
		logger.info("increasing viewcount : " + postId);
		stringRedisTemplate.opsForHash().increment(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY, (long) 1);
	}

	public Long getTodayPostView(Long postId) {
		Long viewCount = (Long) stringRedisTemplate.opsForHash().get(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY);
		if (viewCount == null) {
			return (long) 0;
		} else {
			return viewCount;
		}
	}

	public void resetTodayPostView(Long postId) {
		stringRedisTemplate.opsForHash().put(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY, (long) 0);
	}

	public void deleteKey(String key) {
		stringRedisTemplate.delete(key);
	}

}
