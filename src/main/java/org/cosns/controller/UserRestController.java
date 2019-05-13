package org.cosns.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.cosns.repository.FriendRequest;
import org.cosns.repository.User;
import org.cosns.service.HashTagService;
import org.cosns.service.ImageService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.RegistNameDTO;
import org.cosns.web.DTO.UserFormDTO;
import org.cosns.web.DTO.UserSettingDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserRestController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	UserService userService;

	@Autowired
	PostService postService;

	@Autowired
	RedisService redisService;

	@Autowired
	ImageService imageService;

	@Autowired
	HashTagService hashTagService;

	@Value("${cosns.image.profile.uploadFolder}")
	String uploadFolder;

	@Value("${cosns.image.profile.uploadPattern}")
	String uploadPattern;

	@PostMapping(path = "/login")
	public DefaultResult login(@RequestBody UserFormDTO userDTO, HttpSession session) {
		UserResult ur = new UserResult();

		if (userDTO.getPassword() != null && userDTO.getEmail() != null) {
			User user = userService.verifyUser(userDTO);

			if (user != null) {
				session.setAttribute("user", user);
				ur.setUser(user);
				ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
				ur.setRemarks("Good day, " + user.getEmail());
			} else {
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
				ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_FAIL);
			}

		} else {
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_FAIL);
		}

		return ur;
	}

	@PostMapping(path = "/checkUniqueName")
	public DefaultResult registUniqueName(@RequestBody RegistNameDTO registNameDTO, HttpSession session) {
		User loggedUser = (User) session.getAttribute("user");

		UserResult ur = new UserResult();

		if (loggedUser != null) {

			// User user = userService.getUserByUniqueName(registNameDTO.getUniqueName());

			String value = (String) redisService.getHashValue(ConstantsUtil.REDIS_USER_GROUP + ":" + registNameDTO.getUniqueName(), ConstantsUtil.REDIS_USER_TYPE_ID);

			if (value == null) {
				ur.setRemarks("OK");
				ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				ur.setRemarks("Not Available");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}
		} else {
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return ur;
	}

	@Transactional
	@PostMapping(path = "/updateSetting")
	public DefaultResult updateSetting(@RequestBody UserSettingDTO userSettingDTO, HttpSession session) {
		User loggedUser = (User) session.getAttribute("user");

		UserResult ur = new UserResult();

		if (loggedUser != null) {
			User user = userService.getUserById(loggedUser.getUserId());

			if (userService.passwordCheck(userSettingDTO.getPassword(), user.getPassword())) {

				Set<String> hashTagSet = mapToKeySet(userSettingDTO.getKeyHashTag());

				Object returnValue = userService.updateSetting(user, userSettingDTO);

				hashTagService.deleteUserHashTagInRedis(user);

				hashTagService.deleteUserHashTagByUserId(user.getUserId());

				hashTagService.saveUserHash(user, hashTagSet);

				hashTagService.saveUserHashToRedis(user, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_USER);

				if (returnValue instanceof User) {

					ur.setRemarks("Update Success");
					ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
					ur.setUser((User) returnValue);

					session.setAttribute("user", (User) returnValue);

				} else if (returnValue instanceof String) {

					ur.setRemarks((String) returnValue);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);

					return ur;
				}

			} else {
				ur.setRemarks("Password error");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
				return ur;
			}
		} else {
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return ur;
	}

	@GetMapping(path = "/logout")
	public DefaultResult logout(HttpSession session) {
		UserResult ur = new UserResult();

		session.setAttribute("user", null);

		ur.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return ur;
	}

	@PostMapping(path = "/register")
	public DefaultResult register(@RequestBody UserFormDTO userDTO, HttpSession session) {
		UserResult ur = new UserResult();

		User user = userService.registerUser(userDTO);

		if (user != null) {
			session.setAttribute("user", user);
			ur.setUser(user);
			ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
			ur.setRemarks("Account created, please login");
		} else {
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
			ur.setRemarks("Already registered, forgot password?");
		}

		return ur;
	}

	@GetMapping(path = "/getUser/{userId}")
	public DefaultResult getUser(@PathVariable("userId") Long userId, HttpSession session) {
		UserResult ur = new UserResult();

		User user = userService.getUserById(userId);

		if (user != null) {
			Map<Long, User> followerMap = userService.getFollowerMapByUser(user);

			ur.setUserMap(followerMap);

			ur.setUser(user);
			ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}

	@GetMapping(path = "/addFriend/{userId}")
	public DefaultResult addFriendRequest(@PathVariable("userId") Long userId, HttpSession session) {
		UserResult ur = new UserResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			FriendRequest fr = userService.findFriendRequest(user, userId);

			if (fr == null) {
				user = userService.addFriendRequest(user, userId);

				if (user != null) {
					ur.setUser(user);
					ur.setStatus(ConstantsUtil.RESULT_SUCCESS);

					session.setAttribute("user", user);
				} else {
					ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_ADD_FRIEND_FAIL);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
				}

			} else {
				ur.setRemarks("Friend request exist");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}

		} else {
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}

	@GetMapping(path = "/follow/{targetUserId}")
	public DefaultResult follow(@PathVariable("targetUserId") Long targetUserId, HttpSession session) {
		UserResult ur = new UserResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			User userInDB = userService.getUserById(user.getUserId());

			if (userInDB != null) {

				if (!isFollowed(userInDB, targetUserId)) {

					user = userService.follow(userInDB, targetUserId);

					if (user != null) {
						ur.setUser(user);
						ur.setStatus(ConstantsUtil.RESULT_SUCCESS);

						session.setAttribute("user", user);

					} else {
						ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_USER_NOT_FOUND);
						ur.setStatus(ConstantsUtil.RESULT_ERROR);
					}

				} else {
					ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_USER_ALREADY_FOLLOWED);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
				}

			} else {
				ur.setRemarks("Friend request exist");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}

		} else {
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}

	@GetMapping(path = "/unfollow/{targetUserId}")
	public DefaultResult unfollow(@PathVariable("targetUserId") Long targetUserId, HttpSession session) {
		UserResult ur = new UserResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			User userInDB = userService.getUserById(user.getUserId());

			if (userInDB != null) {

				if (isFollowed(userInDB, targetUserId)) {

					user = userService.unfollow(userInDB, targetUserId);

					if (user != null) {
						ur.setUser(user);
						ur.setStatus(ConstantsUtil.RESULT_SUCCESS);

						session.setAttribute("user", user);

					} else {
						ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_USER_NOT_FOUND);
						ur.setStatus(ConstantsUtil.RESULT_ERROR);
					}

				} else {
					ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_USER_ALREADY_FOLLOWED);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
				}

			} else {
				ur.setRemarks("Friend request exist");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}

		} else {
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}

	private boolean isFollowed(User userInDB, Long targetUserId) {
		List<User> followerList = userInDB.getFollowers();

		for (User followingUser : followerList) {

			if (followingUser.getUserId() == targetUserId) {
				return true;
			}
		}

		return false;
	}

	private Set<String> mapToKeySet(List<Map<String, String>> keyHashTag) {
		return keyHashTag.stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toSet());
	}
}
