package org.cosns.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cosns.repository.Event;
import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RedisService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public void addSetItem(String key, String value) {
		stringRedisTemplate.opsForSet().add(key, value);
	}

	public Set<String> findKeys(String query) {
		return scanKeys(query);
	}

	public boolean hasKey(String query) {
		logger.info("redis : checking : " + query + ", exist ? " + stringRedisTemplate.hasKey(query));
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

	public void savePostKeyToRedis(Post post) {
		setHashValue(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + post.getPostKey(), ConstantsUtil.REDIS_POST_ID, "" + post.getPostId());
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

	public Set<String> scanKeys(String pattern) {
		Set<String> keySet = new HashSet<>();

		logger.info("Searching for pattern {}", pattern);
		Iterable<byte[]> byters = stringRedisTemplate.execute(new RedisCallback<Iterable<byte[]>>() {

			@Override
			public Iterable<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {

				List<byte[]> binaryKeys = new ArrayList<byte[]>();

				ScanOptions.ScanOptionsBuilder scanOptionsBuilder = new ScanOptions.ScanOptionsBuilder();
				scanOptionsBuilder.match(pattern);
				Cursor<byte[]> cursor = connection.scan(scanOptionsBuilder.build());
				while (cursor.hasNext()) {
					binaryKeys.add(cursor.next());
				}

				try {
					cursor.close();
				} catch (IOException e) {
					logger.info("Had a problem", e);
				}

				return binaryKeys;
			}
		});

		for (byte[] byteArr : byters) {
			logger.info(new String(byteArr));
			keySet.add(new String(byteArr));
		}
		return keySet;
	}

	public void saveEventKeyToRedis(Event event) {
		setHashValue(ConstantsUtil.REDIS_EVENT_NAME_GROUP + ":" + event.getEventKey(), ConstantsUtil.REDIS_EVENT_ID, "" + event.getEventId());

	}

	public void addEventMessage(String key, String message) {
		stringRedisTemplate.opsForList().rightPush(key, message);
	}

	public List<EventMessage> getMessageList(String key) {
		ObjectMapper mapper = new ObjectMapper();
		return stringRedisTemplate.opsForList().range(key, 0, -1).stream().map(u -> {
			try {
				return mapper.readValue(u, EventMessage.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
	}

	public void removeSetItem(String key, String value) {
		stringRedisTemplate.opsForSet().remove(key, value);
	}

}
