package org.cosns.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cosns.dao.HashTagDAO;
import org.cosns.dao.UserHashTagDAO;
import org.cosns.repository.Event;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.repository.extend.EventHashTag;
import org.cosns.repository.extend.PostHashTag;
import org.cosns.repository.extend.UserHashTag;
import org.cosns.util.ConstantsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HashTagService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	HashTagDAO hashTagDAO;

	@Autowired
	UserHashTagDAO userHashTagDAO;

	@Autowired
	private RedisService redisService;

	public Set<String> parseHashByMessage(String postMessage) {
		Pattern pattern = Pattern.compile(ConstantsUtil.HASHTAG_PATTERN);
		Matcher matcher = pattern.matcher(postMessage);

		Set<String> hashTagSet = new HashSet<>();

		while (matcher.find()) {
			String key = matcher.group(1);
			hashTagSet.add(key);
		}

		return hashTagSet;
	}

	public Set<String> parseHashByComma(String postMessage) {
		Pattern pattern = Pattern.compile(ConstantsUtil.HASHTAG_PATTERN);
		Matcher matcher = pattern.matcher(postMessage);

		Set<String> hashTagSet = new HashSet<>();

		while (matcher.find()) {
			String key = matcher.group(1);
			hashTagSet.add(key);
		}

		return hashTagSet;
	}

	public void savePostHash(Post post, Set<String> hashTagSet) {
		for (String hashTag : hashTagSet) {
			PostHashTag hashObject = new PostHashTag();

			hashObject.setHashTag(hashTag);
			hashObject.setPost(post);

			hashTagDAO.save(hashObject);
		}
	}

	public void saveEventHash(Event event, Set<String> hashTagSet) {
		for (String hashTag : hashTagSet) {
			EventHashTag hashObject = new EventHashTag();

			hashObject.setHashTag(hashTag);
			hashObject.setEvent(event);

			hashTagDAO.save(hashObject);
		}
	}

	public void saveUserHash(User user, Set<String> hashTagSet) {
		for (String hashTag : hashTagSet) {
			UserHashTag hashObject = new UserHashTag();

			hashObject.setHashTag(hashTag);
			hashObject.setUser(user);

			hashTagDAO.save(hashObject);

		}
	}

	public void savePostHashToRedis(Post post, Set<String> hashTagSet, String tagPrefix, String typePrefix) {
		for (String hashTag : hashTagSet) {
			redisService.addSetItem(tagPrefix + ":" + hashTag.toLowerCase().trim(), typePrefix + ":" + post.getPostId());
		}
	}

	public void saveEventHashToRedis(Event event, Set<String> hashTagSet, String tagPrefix, String typePrefix) {
		for (String hashTag : hashTagSet) {
			redisService.addSetItem(tagPrefix + ":" + hashTag.toLowerCase().trim(), typePrefix + ":" + event.getEventId());
		}
	}

	public void saveUserHashToRedis(User user, Set<String> hashTagSet, String tagPrefix, String typePrefix) {
		for (String hashTag : hashTagSet) {
			redisService.addSetItem(tagPrefix + ":" + hashTag.toLowerCase().trim(), typePrefix + ":" + user.getUserId());
		}
	}

	public Set<String> queryKeySet(String postTagPrefix, String query) {
		return redisService.findKeys(postTagPrefix + ":*" + query + "*");
	}

	public Set<String> getMembers(String key) {
		return redisService.getSetItems(key);
	}

	public void incrHitRate(List<String> keyContentList) {
		hashTagDAO.updateHitRateByKeys(keyContentList);
	}

	@Transactional
	public void incrHashTagSearchCount(Set<String> keySet) {
		List<String> keyContentList = new ArrayList<String>();

		for (String key : keySet) {
			String[] keyArray = key.split(":");

			if (keyArray.length > 1) {
				String keyContent = keyArray[1];
				keyContentList.add(keyContent);
			}
		}

		if (keyContentList.size() > 0) {
			incrHitRate(keyContentList);
		}
	}

	@Transactional
	public Map<String, Map<Long, Integer>> searchAllByHashTag(String queryString, List<String> postTypeList, List<String> eventTypeList, List<String> userTypeList) {
		Map<String, Map<Long, Integer>> masterHitbox = new HashMap<>();

		Map<Long, Integer> postHitbox = new HashMap<>();
		Map<Long, Integer> eventHitbox = new HashMap<>();
		Map<Long, Integer> userHitbox = new HashMap<>();

		masterHitbox.put("post", postHitbox);
		masterHitbox.put("event", eventHitbox);
		masterHitbox.put("user", userHitbox);

		String[] queryList = queryString.split(" ");
		for (String query : queryList) {
			Set<String> keyList = queryKeySet(ConstantsUtil.REDIS_TAG_GROUP, query);
			logger.info("searched key set : " + keyList);

			incrHashTagSearchCount(keyList);

			for (String key : keyList) {
				// set hit rate

				Set<String> itemSet = getMembers(key);
				for (String itemString : itemSet) {

					String[] idArr = itemString.split(":");

					if (idArr.length > 1) {
						String typeItem = idArr[0];
						String idString = idArr[1];

						if (postTypeList.contains(typeItem)) {
							putToHitbox(idString, postHitbox);
						}
						if (eventTypeList.contains(typeItem)) {
							putToHitbox(idString, eventHitbox);
						}
						if (userTypeList.contains(typeItem)) {
							putToHitbox(idString, userHitbox);
						}
					}
				}
			}
		}

		return masterHitbox;
	}

	private void putToHitbox(String idString, Map<Long, Integer> hitbox) {
		Long id = Long.parseLong(idString);
		Integer count = hitbox.get(id);
		if (count == null) {
			hitbox.put(id, 0);
		} else {
			hitbox.put(id, count + 1);
		}
	}

	public Set<String> getRelatedTag(String query, List<String> eventTypeList) {
		Set<String> keyList = queryKeySet(ConstantsUtil.REDIS_TAG_GROUP, query);
		Set<String> eventKeyList = new HashSet<>();

		for (String key : keyList) {
			Set<String> itemSet = getMembers(key);
			for (String itemString : itemSet) {

				String[] idArr = itemString.split(":");

				if (idArr.length > 1) {
					String typeItem = idArr[0];

					if (eventTypeList.contains(typeItem)) {
						eventKeyList.add(key.replaceAll(ConstantsUtil.REDIS_TAG_GROUP + ":", ""));
					}
				}
			}
		}

		return eventKeyList;
	}

	public List<String> searchPostByHashTagSet(Set<String> querySet, List<String> eventTypeList) {
		List<String> eventKeyList = new ArrayList<>();

		for (String query : querySet) {
			Set<String> keyList = queryKeySet(ConstantsUtil.REDIS_TAG_GROUP, query);
			for (String key : keyList) {
				Set<String> itemSet = getMembers(key);
				for (String itemString : itemSet) {

					String[] idArr = itemString.split(":");

					if (idArr.length > 1) {
						String typeItem = idArr[0];
						String idString = idArr[1];

						if (eventTypeList.contains(typeItem)) {
							eventKeyList.add(idString);
						}
					}
				}
			}

		}

		return eventKeyList;
	}

	public void deleteUserHashTagByUserId(Long userId) {
		userHashTagDAO.deleteHashTagByUserId(userId);
	}

	public void deleteUserHashTagInRedis(User user) {
		List<UserHashTag> hashTagList = user.getHashtags();
		for (UserHashTag hashTag : hashTagList) {
			logger.info("removing hash : " + hashTag.getHashTag() + " from user : " + user.getUserId());
			redisService.removeSetItem(ConstantsUtil.REDIS_TAG_GROUP + ":" + hashTag.getHashTag(), ConstantsUtil.REDIS_USER_GROUP + ":" + user.getUserId());
		}
	}
}
