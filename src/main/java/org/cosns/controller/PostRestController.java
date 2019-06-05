package org.cosns.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.cosns.auth.Auth;
import org.cosns.repository.User;
import org.cosns.repository.event.Event;
import org.cosns.repository.post.Post;
import org.cosns.repository.postreaction.PostReaction;
import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.service.ImageService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.cosns.web.DTO.PostMessageDTO;
import org.cosns.web.DTO.PostReactionDTO;
import org.cosns.web.DTO.SearchPostDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.PostListResult;
import org.cosns.web.result.PostReactionResult;
import org.cosns.web.result.PostUserEventListResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/post")
public class PostRestController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PostService postService;

	@Autowired
	EventService eventService;

	@Autowired
	UserService userService;

	@Autowired
	ImageService imageService;

	@Autowired
	HashTagService hashTagService;

	@Autowired
	RedisService redisService;

	@GetMapping(path = "/getPosts")
	public DefaultResult getPosts(HttpSession session) {

		User user = (User) session.getAttribute("user");

		PostListResult plr = new PostListResult();

		if (user != null) {
			plr.setPostList(user.getPosts());
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setStatus(ConstantsUtil.RESULT_ERROR);
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}
		return plr;
	}

	@GetMapping(path = "/getPost/{postId}")
	public DefaultResult getPost(@PathVariable("postId") Long postId, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.getPost(postId, user.getUserId());
		} else {
			postList = postService.getPost(postId);
		}
		/*
		 * try {// JSON example String json = Jsoup.connect(
		 * "https://like.co/api/like/likebutton/chungyeung/total?referrer=http%3A%2F%2Flocalhost%3A8080%2F%40chungyeung%2Fhkbu-2019"
		 * ).ignoreContentType(true).execute().body(); logger.info(json); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		return plr;
	}

	@PostMapping(path = "/searchPosts")
	public DefaultResult searchPost(@RequestBody SearchPostDTO searchPost, HttpSession session) {
		PostUserEventListResult puelr = new PostUserEventListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;
		List<Event> eventList = null;
		List<User> userList = null;

		List<String> postTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_ALL_POST.split(","));
		List<String> eventTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_EVENT.split(","));
		List<String> userTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_USER.split(","));

		Map<String, Map<Long, Integer>> masterHitbox = hashTagService.searchAllByHashTag(searchPost.getKeyword().toLowerCase(), postTypeList, eventTypeList, userTypeList);

		if (user != null) {
			postList = postService.searchPosts(masterHitbox.get("post"), searchPost.getOrderBy(), user);
			eventList = eventService.searchEvents(masterHitbox.get("event"), searchPost.getOrderBy());
			userList = userService.searchUsers(masterHitbox.get("user"), searchPost.getOrderBy());
		} else {
			postList = postService.searchPosts(masterHitbox.get("post"), searchPost.getOrderBy());
			eventList = eventService.searchEvents(masterHitbox.get("event"), searchPost.getOrderBy());
			userList = userService.searchUsers(masterHitbox.get("user"), searchPost.getOrderBy());
		}

		puelr.setPostList(postList);
		puelr.setEventList(eventList);
		puelr.setUserList(userList);

		puelr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return puelr;
	}

	@GetMapping(path = "/getTopPost/{type}/{date}")
	public DefaultResult getTopPost(@PathVariable("type") String type, @PathVariable("date") String date, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.findTopPost(type, date, user.getUserId());
		} else {
			postList = postService.findTopPost(type, date, null);

		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return plr;
	}

	@GetMapping(path = "/getLatestPost")
	public DefaultResult getLatestPost(HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.findLatestPosts(user.getUserId());
		} else {
			postList = postService.findLatestPosts();
		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return plr;
	}

	@GetMapping(path = "/getTimelinePosts/{startFrom}")
	public DefaultResult getTimelinePosts(@PathVariable("startFrom") int startFrom, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			List<Post> postList = postService.findTimelinePosts(user.getUserId(), startFrom);
			plr.setPostList(postList);
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
			plr.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return plr;
	}

	@GetMapping(path = "/getUserPosts/{userId}")
	public DefaultResult getUserPost(@PathVariable("userId") Long userId, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.getUserPosts(userId, user.getUserId());

		} else {
			postList = postService.getUserPosts(userId);
		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		return plr;
	}

	@Auth
	@PostMapping(path = "/writePost")
	@Transactional
	public DefaultResult writePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		Set<String> hashTagSet = hashTagService.parseHashTagByMessage(postDTO.getPostMessage());

		Post post = postService.writePhotoPost(postDTO, user, hashTagSet);

		hashTagService.savePostHashTag(post, hashTagSet);

		hashTagService.savePostHashTagToRedis(post, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_PHOTO);

		redisService.savePostKeyToRedis(post);

		redisService.addPostRecord(post.getPostId());

		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return dr;
	}

	@Auth
	@PostMapping(path = "/updatePost")
	@Transactional
	public DefaultResult updatePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		Set<String> hashTagSet = hashTagService.parseHashTagByMessage(postDTO.getPostMessage());

		Post post = postService.updatePhotoPost(postDTO, user, hashTagSet);

		hashTagService.savePostHashTag(post, hashTagSet);

		hashTagService.savePostHashTagToRedis(post, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_PHOTO);

		redisService.savePostKeyToRedis(post);

		redisService.addPostRecord(post.getPostId());

		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return dr;
	}

	@Auth
	@PostMapping(path = "/likePost")
	public DefaultResult likePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		PostReaction postReaction = postService.likePost(postReactionDTO.getPostId(), user);

		if (postReaction.getStatus().equals(ConstantsUtil.POST_REACTION_ACTIVE)) {
			prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
		} else {
			prr.setType(ConstantsUtil.POST_REACTION_TYPE_DECREASE);
		}

		prr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return prr;
	}

	@Auth
	@PostMapping(path = "/retweetPost")
	public DefaultResult retweet(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		Post post = postService.retweetPost(postReactionDTO.getPostId(), user);
		if (post != null) {
			prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
			prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks("Retweet Failed");
		}

		return prr;
	}

	@Auth
	@PostMapping(path = "/removePost")
	public DefaultResult removePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {

		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		Post post = postService.deletePost(postReactionDTO.getPostId(), user);

		if (post != null) {
			logger.info("removing post : " + post.getPostId());

			// remove hashtag linkage
			hashTagService.deletePostHashTagInRedis(post);

			redisService.deletePostInRedis(post);

			prr.setType(ConstantsUtil.POST_REACTION_CANCEL);
			prr.setStatus(ConstantsUtil.RESULT_SUCCESS);

			prr.setRemarks("Remove OK");
		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks("Remove Failed");
		}

		return prr;
	}

	@GetMapping(path = "/getRelatedTag/{query}")
	public List<Map<String, String>> getRelatedTag(@PathVariable("query") String query, HttpSession session) throws ParseException {

		List<String> eventTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_EVENT_PHOTO.split(","));

		Set<String> resultSet = hashTagService.getRelatedTag(query, eventTypeList);

		List<Map<String, String>> returnMapList = new ArrayList<Map<String, String>>();
		for (String result : resultSet) {
			HashMap<String, String> returnMap = new HashMap<>();
			returnMap.put("label", result);
			returnMapList.add(returnMap);
		}

		return returnMapList;
	}

	@Auth
	@PostMapping(path = "/addComment")
	public DefaultResult addComment(@RequestBody PostMessageDTO messageDTO, HttpSession session) throws ParseException, JsonProcessingException {

		DefaultResult dr = new DefaultResult();

		List<Post> postList = postService.getPost(messageDTO.getPostId());

		User user = (User) session.getAttribute("user");

		if (postList.size() > 0) {
			while (postList.iterator().hasNext()) {
				Post post = postList.iterator().next();
				postService.addComment(messageDTO.getMessage(), post, user);
			}
			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setStatus("no events");
		}
		return dr;
	}

}
