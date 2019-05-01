package org.cosns.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.cosns.dao.PostDAO;
import org.cosns.dao.PostReactionDAO;
import org.cosns.repository.Post;
import org.cosns.repository.PostReaction;
import org.cosns.repository.User;
import org.cosns.repository.extend.DateCountReaction;
import org.cosns.repository.extend.LikeReaction;
import org.cosns.repository.extend.PhotoPost;
import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.RetweetPost;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	public Post removePost(Long postId, User user) {
		logger.info("Remove Post By User : " + user.getUserId());

		List<Post> postList = this.getPost(postId);

		if (postList.iterator().hasNext()) {

			Post post = postList.iterator().next();

			logger.info("removing post : " + post.getPostId());
			if (post.getUser().getUserId() != user.getUserId()) {
				// final check
				return null;
			}

			List<PostImage> imageList = post.getPostImages();
			for (PostImage pi : imageList) {
				pi.setStatus(ConstantsUtil.IMAGE_DELETED);
			}

			post.setStatus(ConstantsUtil.POST_DELETED);
			post = postDAO.save(post);

			return post;
		} else {
			return null;
		}

	}

	public Post writePhotoPost(PostFormDTO postDTO, User user, Set<String> hashTagSet) {
		logger.info("Writing Post By User : " + user.getUserId());

		// create post
		PhotoPost post = new PhotoPost();
		post.setMessage(postDTO.getPostMessage());
		post.setReleaseDate(removeTime(postDTO.getReleaseDate()));
		post.setStatus(ConstantsUtil.POST_ACTIVE);
		post.setUser(user);

		// assign key to post
		assignKey(post, hashTagSet);

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

	public Post assignKey(Post post, Set<String> hashTagSet) {

		StringBuilder sb = new StringBuilder();
		String postMessage = post.getMessage();

		if (hashTagSet.size() > 0) {

			// exclude all hash
			postMessage = postMessage.replaceAll(ConstantsUtil.HASHTAG_PATTERN, "");

			// String firstTenChar = post.getMessage().substring(0,
			// Math.min(postMessage.length(), ConstantsUtil.POST_KEY_MESSAGE_LENGTH));
			// sb.append(firstTenChar.replaceAll(" ", "_"));

			String creator = post.getUser().getUniqueName();

			if (creator == null) {
				creator = "" + post.getUser().getUserId();
			}

			sb.append(creator);

			int count = 0;
			sb.append("_");

			String prepend = "";
			for (String hashTag : hashTagSet) {
				if (count++ < ConstantsUtil.POST_KEY_HASHTAG_NUMBER) {
					// first 3 hashTag
					String hashTagFirstTenChar = hashTag.substring(0, Math.min(hashTag.length(), ConstantsUtil.POST_KEY_HASHTAG_LENGTH));

					sb.append(prepend);
					sb.append(hashTagFirstTenChar.replaceAll(" ", "_"));
					prepend = "_";
				}
			}

			// check redis exist or not
			String checkWith = ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + sb.toString();

			int checkCount = 0;
			while (redisService.hasKey(checkWith)) {
				checkWith = checkWith + "_" + checkCount++;
			}

			// no duplicated
		} else {
			sb.append(post.getPostId() + "" + uniqueCurrentTimeMS());
		}

		post.setPostKey(sb.toString());

		logger.info("saved postId : " + post.getPostId() + " to : " + post.getPostKey());

		return post;
	}

	private static final AtomicLong LAST_TIME_MS = new AtomicLong();

	public static long uniqueCurrentTimeMS() {
		long now = System.currentTimeMillis();
		while (true) {
			long lastTime = LAST_TIME_MS.get();
			if (lastTime >= now)
				now = lastTime + 1;
			if (LAST_TIME_MS.compareAndSet(lastTime, now))
				return now;
		}
	}

	public Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Post setLikeRetweetCount(Post post) {
		Long postId = post.getPostId();

		post = setAndIncreaseCount(post);

		post.setTotalViewCount(redisService.getTotalPostView(postId));
		post.setTodayViewCount(redisService.getTodayPostView(postId));
		post.setLikeCount(redisService.getLikeCount(postId));
		post.setRetweetCount(redisService.getRetweetCount(postId));
		return post;
	}

	private Post setLikedRetweeted(Post post, Long userId) {
		Long postId = post.getPostId();

		post.setTotalViewCount(redisService.getTotalPostView(postId));
		post.setTodayViewCount(redisService.getTodayPostView(postId));
		post.setLiked(redisService.isLiked(postId, userId));
		post.setRetweeted(redisService.isRetweeted(postId, userId));
		return post;
	}

	private Post setAndIncreaseCount(Post post) {
		Long postId = post.getPostId();

		redisService.incrTotalPostView(postId);
		redisService.incrTodayPostView(postId);

		return post;
	}

	private List<Post> groupRetweetPost(List<Post> postList) {

		// group by the retweetPost
		Map<Long, List<Post>> processedMap = new LinkedHashMap<>();

		for (Post post : postList) {
			logger.info("grouping : " + post.getPostId());
			if (post instanceof RetweetPost) {
				logger.info("is retweetPost");
				RetweetPost retweetedPost = ((RetweetPost) post);

				List<Post> savedList = processedMap.get(retweetedPost.getPost().getPostId());
				if (savedList != null) {
					savedList.add(retweetedPost);
				} else {
					savedList = new ArrayList<>();
					savedList.add(retweetedPost);
				}

				processedMap.put(retweetedPost.getPost().getPostId(), savedList);
			}
		}

		// filter with the photo post only
		List<Post> returnPost = new ArrayList<>();

		for (Post post : postList) {
			if (post instanceof PhotoPost) {
				if (processedMap.get(post.getPostId()) != null) {
					List<Post> retweetedBy = processedMap.get(post.getPostId());
					// find retweet latest time
					Date latestTime = null;

					for (Post retweetPost : retweetedBy) {
						if (latestTime != null) {
							if (retweetPost.getCreatedate().compareTo(latestTime) > 0) {
								latestTime = retweetPost.getCreatedate();
							}
						} else {
							latestTime = retweetPost.getCreatedate();
						}
					}
					post.setCreatedate(latestTime);
					post.setRetweetedBy(retweetedBy);
				}
				returnPost.add(post);
			}
		}

		return sortByCreatedate(returnPost);
	}

	public List<Post> sortByCreatedate(List<Post> post) {
		return post.stream().sorted(Comparator.comparing(Post::getCreatedate).reversed()).collect(Collectors.toList());
	}

	private List<Post> setLikeRetweetCount(List<Post> postList) {
		return postList.stream().map(p -> setLikeRetweetCount(p)).collect(Collectors.toList());
	}

	private List<Post> setLikeRetweetedAndCount(List<Post> postList, Long userId) {
		return postList.stream().map(p -> setLikeRetweetCount(p)).map(p -> setLikedRetweeted(p, userId)).collect(Collectors.toList());
	}

	public List<Post> findLatestPosts(Long userId) {
		return setLikeRetweetedAndCount(postDAO.findLatestPosts(PageRequest.of(0, 10)), userId);
	}

	public List<Post> findLatestPosts() {
		return setLikeRetweetCount(postDAO.findLatestPosts(PageRequest.of(0, 10)));
	}

	public List<Post> findTimelinePosts(Long userId, int startFrom) {
		return groupRetweetPost(setLikeRetweetedAndCount(postDAO.findTimelinePosts(userId, PageRequest.of(startFrom, 5)), userId));
	}

	public List<Post> getUserPosts(Long userId, Long loggedUserId) {
		return groupRetweetPost(setLikeRetweetedAndCount(postDAO.findPostByUserId(userId), loggedUserId));
	}

	public List<Post> getUserPosts(Long userId) {
		return groupRetweetPost(setLikeRetweetCount(postDAO.findPostByUserId(userId)));
	}

	public List<Post> getPost(Long postId) {
		return setLikeRetweetCount(postDAO.findPostByPostId(postId));
	}

	public List<Post> getPost(Long postId, Long userId) {
		return setLikeRetweetedAndCount(postDAO.findPostByPostId(postId), userId);
	}

	public List<Post> searchPosts(String query, String postType, String orderBy) {
		return searchPosts(query, postType, orderBy, null);
	}

	public List<Post> searchPosts(String query, String postType, String orderBy, User user) {

		Map<Long, Integer> hitBox = new HashMap<>();

		Set<String> keyList = hashTagService.queryKeySet(ConstantsUtil.REDIS_POST_TAG_GROUP, query);
		logger.info("searched key set : " + keyList);

		hashTagService.incrHashTagSearchCount(keyList);

		for (String key : keyList) {
			// set hit rate

			Set<String> postItemSet = hashTagService.getMembers(key);
			for (String postItemString : postItemSet) {

				String[] postIdArray = postItemString.split(":");

				if (postIdArray.length > 1) {
					String postTypeItem = postIdArray[0];
					String postIdString = postIdArray[1];

					logger.info("search for : " + postType);
					if (postTypeItem.equals(postType) || postType.equals("all")) {
						Long postId = Long.parseLong(postIdString);
						Integer count = hitBox.get(postId);
						if (count == null) {
							hitBox.put(postId, 0);
						} else {
							hitBox.put(postId, count + 1);
						}
					}
				}

			}
		}

		logger.info(hitBox.toString());

		String orderByString = "createdate";

		if (orderBy != null) {
			switch (orderBy) {
			case "date":
				orderByString = "createdate";
				break;
			case "view":
				orderByString = "totalViewCount";
				break;
			}
		}

		if (hitBox.size() > 0) {
			if (user != null) {
				return setLikeRetweetedAndCount(postDAO.findPostByPostIdSet(sortByValue(hitBox, true).keySet(), Sort.by(orderByString).descending()), user.getUserId());
			} else {
				return setLikeRetweetCount(postDAO.findPostByPostIdSet(sortByValue(hitBox, true).keySet(), Sort.by(orderByString).descending()));
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

	public void incrTotalPostView(Long postId) {
		redisService.incrTotalPostView(postId);
	}

	public Long getTotalPostView(Long postId) {
		return redisService.getTotalPostView(postId);
	}

	public void syncPostCountToDB() {
		Set<String> keySet = redisService.findKeys(ConstantsUtil.REDIS_POST_VIEW_GROUP + ":*");

		for (String key : keySet) {

			String[] keyArray = key.split(":");

			if (keyArray.length > 1) {

				Long postId = Long.parseLong(keyArray[1]);

				Long totalPostView = redisService.getTotalPostView(postId);
				Long todayPostView = redisService.getTodayPostView(postId);

				logger.info("Got postId #" + postId + " : today " + todayPostView + ", total " + totalPostView);

				List<Post> postList = postDAO.findPostByPostId(postId);

				if (postList.iterator().hasNext()) {
					Post post = postList.iterator().next();

					post.setTotalViewCount(totalPostView);

					int year = Calendar.getInstance().get(Calendar.YEAR);
					int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
					int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

					Optional<DateCountReaction> optionalDCR = postReactionDAO.findByDateAndPostId(day, month, year, post.getPostId());

					if (optionalDCR.isPresent()) {
						DateCountReaction presentDCR = optionalDCR.get();
						presentDCR.setViewCount(presentDCR.getViewCount() + todayPostView);

						postReactionDAO.save(presentDCR);
					} else {
						DateCountReaction dcr = new DateCountReaction();
						dcr.setPost(post);
						dcr.setYear(year);
						dcr.setMonth(month);
						dcr.setDay(day);
						dcr.setViewCount(todayPostView);
						dcr.setStatus(ConstantsUtil.POST_REACTION_ACTIVE);

						postReactionDAO.save(dcr);

					}
					postDAO.save(post);

					redisService.resetTodayPostView(postId);

					logger.info("saved to DB");
				}
			}
		}
	}

	public List<Post> findTopPost(String type, String date, Long userId) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			Date dateObject = sdf.parse(date);

			calendar.setTime(dateObject);

			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR);

			if (type.equalsIgnoreCase("day")) {
				List<String> keys = redisService.getSortedKeysByToday();

				if (userId != null) {
					return setLikeRetweetedAndCount(getPostByIds(keys), userId);
				} else {
					return setLikeRetweetCount(getPostByIds(keys));
				}

			} else if (type.equalsIgnoreCase("month")) {
				if (userId != null) {
					return setLikeRetweetedAndCount(postDAO.findTopMonthPosts(month, year, PageRequest.of(0, 10)), userId);
				} else {
					return setLikeRetweetCount(postDAO.findTopMonthPosts(month, year, PageRequest.of(0, 10)));
				}
			} else if (type.equalsIgnoreCase("year")) {
				if (userId != null) {
					return setLikeRetweetedAndCount(postDAO.findTopYearPosts(year, PageRequest.of(0, 10)), userId);
				} else {
					return setLikeRetweetCount(postDAO.findTopYearPosts(year, PageRequest.of(0, 10)));
				}
			} else if (type.equalsIgnoreCase("all")) {
				if (userId != null) {
					return setLikeRetweetedAndCount(postDAO.findTopPosts(PageRequest.of(0, 10)), userId);
				} else {
					return setLikeRetweetCount(postDAO.findTopPosts(PageRequest.of(0, 10)));
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Post> getPostByIds(List<String> keys) {
		Set<Long> idList = keys.stream().limit(10).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet());
		return postDAO.findPostByPostIdSet(idList);
	}
}
