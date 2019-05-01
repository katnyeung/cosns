package org.cosns.service;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
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

	public boolean hasKey(String query) {
		return stringRedisTemplate.hasKey(query);
	}

	public Set<String> getSetItems(String key) {
		return stringRedisTemplate.opsForSet().members(key);
	}

	public Object getHashValue(String key, String type) {
		return stringRedisTemplate.opsForHash().get(key, type);
	}

	public void setHashValue(String key, String type, String value) {
		stringRedisTemplate.opsForHash().put(key, type, value);
	}

	public void addPostRecord(Long postId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_GROUP, "" + postId);
	}

	public void removePostRecord(Post post) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_GROUP, "" + post.getPostId());
		stringRedisTemplate.delete(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + post.getPostKey());
		stringRedisTemplate.delete(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + post.getPostKey());
		stringRedisTemplate.delete(ConstantsUtil.REDIS_POST_LIKE_GROUP + ":" + post.getPostKey());
	}

	public void incrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_LIKE_GROUP + ":" + postId, "" + userId);
	}

	public void incrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().add(ConstantsUtil.REDIS_POST_RETWEET_GROUP + ":" + postId, "" + userId);
	}

	public void decrLike(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_LIKE_GROUP + ":" + postId, "" + userId);
	}

	public void decrRetweet(Long postId, Long userId) {
		stringRedisTemplate.opsForSet().remove(ConstantsUtil.REDIS_POST_RETWEET_GROUP + ":" + postId, "" + userId);
	}

	public Long getLikeCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_LIKE_GROUP + ":" + postId);
	}

	public Long getRetweetCount(Long postId) {
		return stringRedisTemplate.opsForSet().size(ConstantsUtil.REDIS_POST_RETWEET_GROUP + ":" + postId);
	}

	public boolean isLiked(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_LIKE_GROUP + ":" + postId, "" + userId);
	}

	public boolean isRetweeted(Long postId, Long userId) {
		return stringRedisTemplate.opsForSet().isMember(ConstantsUtil.REDIS_POST_RETWEET_GROUP + ":" + postId, "" + userId);
	}

	public void incrTotalPostView(Long postId) {
		stringRedisTemplate.opsForHash().increment(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TOTAL, (long) 1);
	}

	public Long getTotalPostView(Long postId) {
		String viewCountString = (String) stringRedisTemplate.opsForHash().get(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TOTAL);
		if (viewCountString == null) {
			return (long) 0;
		} else {
			return Long.parseLong(viewCountString);
		}
	}

	public void incrTodayPostView(Long postId) {
		logger.info("increasing viewcount : " + postId);
		stringRedisTemplate.opsForHash().increment(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY, (long) 1);
	}

	public Long getTodayPostView(Long postId) {
		String viewCountString = (String) stringRedisTemplate.opsForHash().get(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY);
		if (viewCountString == null) {
			return (long) 0;
		} else {
			return Long.parseLong(viewCountString);
		}
	}

	public void resetTodayPostView(Long postId) {
		stringRedisTemplate.opsForHash().put(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":" + postId, ConstantsUtil.REDIS_POST_VIEW_TYPE_TODAY, "0");
	}

	public void deleteKey(String key) {
		stringRedisTemplate.delete(key);
	}

	public List<String> getSortedKeysByToday() {

		SortQuery<String> query = SortQueryBuilder.sort(ConstantsUtil.REDIS_POST_GROUP).by(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":*->today").order(Order.DESC).build();

		return stringRedisTemplate.sort(query);

	}

}
