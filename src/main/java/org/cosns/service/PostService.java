package org.cosns.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.cosns.dao.ImageDAO;
import org.cosns.dao.PostDAO;
import org.cosns.dao.PostReactionDAO;
import org.cosns.repository.Image;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.repository.extend.LikeReaction;
import org.cosns.repository.extend.PhotoPost;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	ImageDAO imageDAO;

	@Autowired
	PostDAO postDAO;

	@Autowired
	PostReactionDAO postReactionDAO;

	@Autowired
	HashTagService hashTagService;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	public void saveImage(String prefix, MultipartFile file, User user) {
		Image image = new Image();
		image.setFilename(prefix + file.getOriginalFilename());
		image.setSize(file.getSize());
		image.setStatus(ConstantsUtil.IMAGE_PEND);
		image.setUser(user);

		imageDAO.save(image);
	}

	public Post writePost(PostFormDTO postDTO, User user) {
		logger.info("Writing Post By User : " + user.getUserId());

		// create post
		Post post = new PhotoPost();
		post.setMessage(postDTO.getPostMessage());
		post.setStatus(ConstantsUtil.POST_ACTIVE);
		post.setUser(user);

		post = postDAO.save(post);

		int count = 1;
		for (String file : postDTO.getFileList()) {
			Set<Image> imageSet = imageDAO.findPendImageByFilename(file);
			for (Image image : imageSet) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setPost(post);
				image.setSeq(count++);
				imageDAO.save(image);
			}
		}

		return post;
	}

	public Set<Post> findRandomPosts() {
		return postDAO.findRandomPost();
	}

	public Set<Post> findTimelinePosts(User user) {
		return postDAO.findTimelinePosts(user.getUserId());
	}

	public Set<Post> getUserPosts(String uniqueName) {
		return postDAO.findPostByUniqueName(uniqueName);
	}

	public Set<Post> getUserPosts(Long userId) {
		return postDAO.findPostByUserId(userId);
	}

	public Optional<Post> getPost(Long postId) {
		return postDAO.findById(postId);
	}

	public Set<Post> searchPosts(String query) {

		Map<Long, Integer> hitBox = new HashMap<>();

		Set<String> keyList = hashTagService.queryKeySet(query);

		for (String key : keyList) {
			Set<String> postIdSet = hashTagService.getMembers(key);
			for (String postIdString : postIdSet) {
				Long postId = Long.parseLong(postIdString);
				Integer count = hitBox.get(postId);
				if (count == null) {
					hitBox.put(postId, 0);
				} else {
					hitBox.put(postId, count + 1);
				}
			}
		}

		logger.info(hitBox.toString());
		if (hitBox.size() > 0) {
			return postDAO.findPostByPostIdSet(sortByValue(hitBox, true).keySet());
		} else {
			return null;
		}

	}

	private static Map<Long, Integer> sortByValue(Map<Long, Integer> unsortMap, final boolean order) {
		List<Entry<Long, Integer>> list = new LinkedList<>(unsortMap.entrySet());

		// Sorting the list based on values
		list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0 ? o1.getKey().compareTo(o2.getKey()) : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0 ? o2.getKey().compareTo(o1.getKey()) : o2.getValue().compareTo(o1.getValue()));
		return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));

	}

	public Post likePost(Long postId) {

		Optional<Post> postOptional = postDAO.findById(postId);

		if (postOptional.isPresent()) {

			Post post = postOptional.get();

			LikeReaction likeReaction = new LikeReaction();
			likeReaction.setPost(post);

			postReactionDAO.save(likeReaction);

			return post;
		} else {
			return null;
		}

	}
}
