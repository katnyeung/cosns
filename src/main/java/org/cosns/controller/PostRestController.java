package org.cosns.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.cosns.repository.Event;
import org.cosns.repository.Post;
import org.cosns.repository.PostReaction;
import org.cosns.repository.User;
import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.service.ImageService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
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

	@GetMapping(path = "/sync")
	public DefaultResult sync(HttpSession session) {
		DefaultResult dr = new DefaultResult();
		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		postService.syncPostCountToDB();

		return dr;
	}

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
			eventList = eventService.searchEvents(masterHitbox.get("event"), searchPost.getOrderBy(), user);
			userList = userService.searchEvents(masterHitbox.get("user"), searchPost.getOrderBy(), user);
		} else {
			postList = postService.searchPosts(masterHitbox.get("post"), searchPost.getOrderBy());
			eventList = eventService.searchEvents(masterHitbox.get("event"), searchPost.getOrderBy(), null);
			userList = userService.searchEvents(masterHitbox.get("user"), searchPost.getOrderBy(), null);
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

	@PostMapping(path = "/writePost")
	@Transactional
	public DefaultResult writePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Set<String> hashTagSet = hashTagService.parseHash(postDTO.getPostMessage());

			Post post = postService.writePhotoPost(postDTO, user, hashTagSet);

			logger.info("writing hash : " + hashTagSet);

			hashTagService.savePostHash(post, hashTagSet);

			hashTagService.savePostHashToRedis(post, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_PHOTO);

			redisService.savePostKeyToRedis(post);

			redisService.addPostRecord(post.getPostId());

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return dr;
	}

	@PostMapping(path = "/likePost")
	public DefaultResult likePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			PostReaction postReaction = postService.likePost(postReactionDTO.getPostId(), user);

			if (postReaction.getStatus().equals(ConstantsUtil.POST_REACTION_ACTIVE)) {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
			} else {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_DECREASE);
			}

			prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}

	@PostMapping(path = "/retweetPost")
	public DefaultResult retweet(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.retweetPost(postReactionDTO.getPostId(), user);
			if (post != null) {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
				prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				prr.setStatus(ConstantsUtil.RESULT_ERROR);
				prr.setRemarks("Retweet Failed");
			}

		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}

	@PostMapping(path = "/removePost")
	public DefaultResult removePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {

		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {

			Post post = postService.removePost(postReactionDTO.getPostId(), user);

			if (post != null) {
				logger.info("removing post : " + post.getPostId());

				redisService.removePostRecord(post);

				prr.setType(ConstantsUtil.POST_REACTION_CANCEL);
				prr.setStatus(ConstantsUtil.RESULT_SUCCESS);

				prr.setRemarks("Remove OK");
			} else {
				prr.setStatus(ConstantsUtil.RESULT_ERROR);
				prr.setRemarks("Remove Failed");
			}

		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}
}
