package org.cosns.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.cosns.dao.PostDAO;
import org.cosns.dao.PostReactionDAO;
import org.cosns.repository.User;
import org.cosns.repository.image.PostImage;
import org.cosns.repository.post.PhotoPost;
import org.cosns.repository.post.Post;
import org.cosns.repository.post.RetweetPost;
import org.cosns.repository.postreaction.DateCountReaction;
import org.cosns.repository.postreaction.LikeReaction;
import org.cosns.repository.postreaction.PostReaction;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PostService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

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
		
		int weight = 100;
		if(hashTagSet.size() > 5) {
			weight = weight - ((hashTagSet.size() - 5) * 3);
			if(weight < 0) {
				weight = 0;
			}
		}
		
		post.setWeight(weight);
		
		// assign key to post
		post.setPostKey(getPostKey(post, hashTagSet));

		post = (PhotoPost) postDAO.save(post);

		int count = 1;
		for (String file : postDTO.getFileList()) {
			List<PostImage> imageList = imageService.findPendPostImageByFilename(file);
			for (PostImage image : imageList) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setPost(post);
				image.setSeq(count++);
				imageService.savePostImage(image);
			}
		}

		return post;
	}

	public Post updatePhotoPost(PostFormDTO postDTO, User user, Set<String> hashTagSet) {
		logger.info("Updating Post #" +postDTO.getPostId() + " By User : " + user.getUserId());
		Post post = null;

		List<Post> postList = postDAO.findPostByPostId(postDTO.getPostId());
		if (postList.iterator().hasNext()) {
			post = postList.iterator().next();
			
			//remove hashtag linkage
			hashTagService.deletePostHashTagInRedis(post);
			
			hashTagService.deletePostHashTag(post);

			redisService.deletePostRecordInRedis(post);
			
			post.setMessage(postDTO.getPostMessage());
			post.setReleaseDate(removeTime(postDTO.getReleaseDate()));
			post.setStatus(ConstantsUtil.POST_ACTIVE);
			post.setUser(user);

			// assign key to post
			post.setPostKey(getPostKey(post, hashTagSet));

			post = (PhotoPost) postDAO.save(post);

			//deactive all photo
			List<PostImage> postImageList = post.getPostImages();
			for (PostImage postImage : postImageList) {
				if (!postDTO.getFileList().contains(postImage.getFilename())) {
					postImage.setStatus(ConstantsUtil.IMAGE_PEND);
					imageService.savePostImage(postImage);
				}
			}
			
			logger.info("fileList : " +  postDTO.getFileList());
			int count = 1;
			for (String file : postDTO.getFileList()) {
				logger.info("processing file : " + file + ", seq : " + count);
				List<PostImage> imageList = imageService.findPostImageByFilename(file);
				for (PostImage image : imageList) {
					image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
					image.setPost(post);
					image.setSeq(count++);
					imageService.savePostImage(image);
				}
			}
			
		}

		return post;
	}

	public String getPostKey(Post post, Set<String> hashTagSet) {

		String userName = post.getUser().getUniqueName() != null ? post.getUser().getUniqueName() : "" + post.getUser().getUserId();

		StringBuilder sb = new StringBuilder();

		if (hashTagSet.size() > 0) {

			int count = 0;

			String prepend = "";
			for (String hashTag : hashTagSet) {
				if (count++ < ConstantsUtil.POST_KEY_HASHTAG_NUMBER) {
					// first 3 hashTag
					String hashTagFirstTenChar = hashTag.substring(0, Math.min(hashTag.length(), ConstantsUtil.POST_KEY_HASHTAG_LENGTH));

					sb.append(prepend);
					sb.append(hashTagFirstTenChar.trim().replaceAll(" ", "-"));
					prepend = "-";
				}
			}

			// check redis exist or not

			StringBuilder checkWithRaw = new StringBuilder(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + sb.toString());
			StringBuilder checkWith = new StringBuilder(checkWithRaw);
			int checkCount = 0;
			while (redisService.hasKey(checkWith.toString())) {
				// reset the checkWith
				checkWith = new StringBuilder(checkWithRaw);
				checkWith.append("-");
				checkWith.append(++checkCount);
			}

			// no duplicated
			if (checkCount > 0) {
				sb.append("-");
				sb.append(checkCount);
			}
		} else {
			sb.append(post.getPostId() + "" + uniqueCurrentTimeMS());
		}
		
		String postKey = userName + "/" + sb.toString();
		
		return postKey;
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

		redisService.incrTotalPostView(postId, post.getWeight());
		redisService.incrTodayPostView(postId, post.getWeight());

		return post;
	}

	private List<Post> groupRetweetPost(List<Post> postList) {

		// group by the retweetPost
		Map<Long, List<Post>> processedMap = new LinkedHashMap<>();

		for (Post post : postList) {
			if (post instanceof RetweetPost) {
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

	public List<Post> setLikeRetweetCount(List<Post> postList) {
		return postList.stream().map(p -> setLikeRetweetCount(p)).collect(Collectors.toList());
	}

	public List<Post> setLikeRetweetedAndCount(List<Post> postList, Long userId) {
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

	public List<Post> searchPosts(Map<Long, Integer> hitBox, String orderBy) {
		return searchPosts(hitBox, orderBy, null);
	}

	public List<Post> searchPosts(Map<Long, Integer> hitBox, String orderBy, User user) {

		String orderByString = getOrderByString(orderBy);

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

	private String getOrderByString(String orderBy) {
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

		return orderByString;

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

			Optional<LikeReaction> optPR = postReactionDAO.findLikeReactionByPostIdUserId(post.getPostId(), user.getUserId());

			if (optPR.isPresent()) {

				LikeReaction reaction = optPR.get();
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
				if (keys.size() > 0) {
					if (userId != null) {
						return setLikeRetweetedAndCount(getPostByIds(keys), userId);
					} else {
						return setLikeRetweetCount(getPostByIds(keys));
					}
				} else {
					return null;
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

	public List<Post> getPostByIds(List<String> keys) {
		Set<Long> idList = keys.stream().limit(10).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet());
		return postDAO.findPostByPostIdSet(idList);
	}

	public void resetPostKeyToRedis() {

		Iterable<Post> postIter = postDAO.findAll();
		Iterator<Post> iter = postIter.iterator();
		
		while (iter.hasNext()) {
			Post post = iter.next();
			logger.info("Processing reset post : " + post.getPostId());
			if (post instanceof PhotoPost) {
				if (post.getPostKey() != null) {
					//remove hashtag linkage
					hashTagService.deletePostHashTagInRedis(post);
					
					redisService.deletePostRecordInRedis(post);

					redisService.savePostKeyToRedis(post);

					redisService.resetTotalPostView(post.getPostId());
					redisService.resetTodayPostView(post.getPostId());
					
					List<LikeReaction> likeReactionList = postReactionDAO.findLikeReactionByPostId(post.getPostId());
					for (LikeReaction pr : likeReactionList) {
						redisService.incrLike(pr.getPost().getPostId(), pr.getUser().getUserId());
					}

				}
			} else if (post instanceof RetweetPost) {
				redisService.incrRetweet(post.getPostId(), post.getUser().getUserId());
			}

		}
	}

}
