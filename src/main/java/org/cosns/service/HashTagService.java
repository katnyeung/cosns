package org.cosns.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cosns.dao.HashTagDAO;
import org.cosns.repository.HashTag;
import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HashTagService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	HashTagDAO hashTagDAO;

	@Autowired
	private RedisService redisService;

	public Set<String> parseHash(String postMessage) {
		Pattern pattern = Pattern.compile(ConstantsUtil.HASHTAG_PATTERN);
		Matcher matcher = pattern.matcher(postMessage);

		Set<String> hashTagSet = new HashSet<>();

		while (matcher.find()) {
			String key = matcher.group(1);
			hashTagSet.add(key);
		}

		return hashTagSet;
	}

	public void saveHash(Post post, Set<String> hashTagSet) {
		for (String hashTag : hashTagSet) {
			HashTag hashObject = new HashTag();

			hashObject.setHashTag(hashTag);
			hashObject.setPost(post);

			hashTagDAO.save(hashObject);
		}
	}

	public void savePostKeyToRedis(Post post) {
		redisService.setHashValue(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + post.getPostKey(), ConstantsUtil.REDIS_POST_ID, "" + post.getPostId());
	}

	public void saveHashToRedis(Post post, Set<String> hashTagSet, String postTagPrefix, String postTypePrefix) {
		for (String hashTag : hashTagSet) {
			redisService.addSetItem(postTagPrefix + ":" + hashTag.toLowerCase().trim(), postTypePrefix + ":" + post.getPostId());
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

}
