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

import org.cosns.dao.PostDAO;
import org.cosns.dao.PostReactionDAO;
import org.cosns.repository.Post;
import org.cosns.repository.PostReaction;
import org.cosns.repository.User;
import org.cosns.repository.extend.LikeReaction;
import org.cosns.repository.extend.PhotoPost;
import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.RetweetPost;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	ImageService imageService;

	@Autowired
	PostDAO postDAO;

	@Autowired
	PostReactionDAO postReactionDAO;

	@Autowired
	HashTagService hashTagService;

	@Autowired
	RedisService redisService;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	public Post writePost(PostFormDTO postDTO, User user) {
		logger.info("Writing Post By User : " + user.getUserId());

		// create post
		PhotoPost post = new PhotoPost();
		post.setMessage(postDTO.getPostMessage());
		post.setReleaseDate(postDTO.getReleaseDate());
		post.setStatus(ConstantsUtil.POST_ACTIVE);
		post.setUser(user);

		post = (PhotoPost) postDAO.save(post);

		int count = 1;
		for (String file : postDTO.getFileList()) {
			List<PostImage> imageSet = imageService.findPendPostImageByFilename(file);
			for (PostImage image : imageSet) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setPost(post);
				image.setSeq(count++);
				imageService.savePostImage(image);
			}
		}

		return post;
	}

	private Post setLikeRetweetCount(Post post) {
		Long postId = post.getPostId();
		post.setLikeCount(redisService.getLikeCount(postId));
		post.setRetweetCount(redisService.getRetweetCount(postId));
		return post;
	}

	private Post setLikedRetweeted(Post post, Long userId) {
		Long postId = post.getPostId();
		post.setLiked(redisService.isLiked(postId, userId));
		post.setRetweeted(redisService.isRetweeted(postId, userId));
		return post;
	}

	private List<Post> setLikeRetweetCount(List<Post> postList) {
		return postList.stream().map(p -> setLikeRetweetCount(p)).collect(Collectors.toList());
	}

	private List<Post> setLikeRetweetedAndCount(List<Post> postList, Long userId) {
		return postList.stream().map(p -> setLikeRetweetCount(p)).map(p -> setLikedRetweeted(p, userId)).collect(Collectors.toList());
	}

	public List<Post> findRandomPosts(Long userId) {
		return setLikeRetweetedAndCount(postDAO.findRandomPost(), userId);
	}

	public List<Post> findRandomPosts() {
		return setLikeRetweetCount(postDAO.findRandomPost());
	}

	public List<Post> findTimelinePosts(Long userId) {
		return setLikeRetweetedAndCount(postDAO.findTimelinePosts(userId), userId);
	}

	public List<Post> getUserPosts(Long userId) {
		return setLikeRetweetedAndCount(postDAO.findPostByUserId(userId), userId);
	}

	public Optional<Post> getPost(Long postId) {
		return postDAO.findById(postId);
	}

	public List<Post> searchPosts(String query) {
		return searchPosts(query, null);
	}

	public List<Post> searchPosts(String query, User user) {

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
			if (user != null) {
				return setLikeRetweetedAndCount(postDAO.findPostByPostIdSet(sortByValue(hitBox, true).keySet()), user.getUserId());
			} else {
				return setLikeRetweetCount(postDAO.findPostByPostIdSet(sortByValue(hitBox, true).keySet()));
			}
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

	public PostReaction likePost(Long postId, User user) {

		Optional<Post> postOptional = postDAO.findById(postId);

		if (postOptional.isPresent()) {

			Post post = postOptional.get();

			Optional<PostReaction> optPR = postReactionDAO.findByPostIdUserId(post.getPostId(), user.getUserId());

			if (optPR.isPresent()) {

				PostReaction reaction = optPR.get();
				if (reaction.getStatus().equals(ConstantsUtil.POST_REACTION_ACTIVE)) {
					redisService.decrLike(post.getPostId(), user.getUserId());
					reaction.setStatus(ConstantsUtil.POST_REACTION_CANCEL);
				} else {
					redisService.incrLike(post.getPostId(), user.getUserId());
					reaction.setStatus(ConstantsUtil.POST_REACTION_ACTIVE);
				}

				postReactionDAO.save(reaction);

				return reaction;

			} else {
				redisService.incrLike(post.getPostId(), user.getUserId());

				LikeReaction reaction = new LikeReaction();
				reaction.setPost(post);
				reaction.setUser(user);
				reaction.setStatus(ConstantsUtil.POST_REACTION_ACTIVE);
				postReactionDAO.save(reaction);

				return reaction;
			}

		} else {
			return null;
		}
	}

	public Post retweetPost(Long postId, User user) {

		// find if this user already retweeted this post
		List<Post> postRetweeted = postDAO.findRetweetedPost(postId, user.getUserId());

		if (postRetweeted.iterator().hasNext()) {
			return null;
		} else {
			Optional<Post> postOptional = postDAO.findById(postId);

			if (postOptional.isPresent()) {

				Post post = postOptional.get();
				// cannot retweet by original poster
				if (post.getUser().getUserId() != user.getUserId()) {

					// how about if user want to cancel the retweet? delete retweet, other function
					RetweetPost retweetPost = new RetweetPost();
					retweetPost.setPost(post);
					retweetPost.setStatus(ConstantsUtil.POST_ACTIVE);
					retweetPost.setUser(user);

					redisService.incrRetweet(post.getPostId(), user.getUserId());

					retweetPost = (RetweetPost) postDAO.save(retweetPost);

					return retweetPost;
				} else {
					return null;
				}

			} else {
				return null;
			}
		}

	}

}
