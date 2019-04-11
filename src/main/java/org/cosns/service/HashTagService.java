package org.cosns.service;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cosns.dao.HashTagDAO;
import org.cosns.repository.HashTag;
import org.cosns.repository.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HashTagService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	HashTagDAO hashTagDAO;

	@Autowired
	private RedisService redisService;

	public Set<String> parseHash(Post post) {
		Pattern pattern = Pattern.compile("#(\\w+)");
		String message = post.getMessage();
		Matcher matcher = pattern.matcher(message);

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

	public void saveHashToRedis(Post post, Set<String> hashTagSet, String prefix) {
		for (String hashTag : hashTagSet) {
			redisService.addSetItem(hashTag, prefix + ":" + post.getPostId());
		}
	}

	public Set<String> queryKeySet(String query) {
		return redisService.findKeys("*" + query + "*");
	}

	public Set<String> getMembers(String key) {
		return redisService.getSetItems(key);
	}
}
