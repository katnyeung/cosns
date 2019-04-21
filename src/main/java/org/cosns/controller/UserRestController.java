package org.cosns.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.cosns.repository.FriendRequest;
import org.cosns.repository.User;
import org.cosns.service.ImageService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.DTO.RegistNameDTO;
import org.cosns.web.DTO.UserFormDTO;
import org.cosns.web.DTO.UserIdListDTO;
import org.cosns.web.DTO.UserSettingDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UploadImageResult;
import org.cosns.web.result.UserMapResult;
import org.cosns.web.result.UserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	UserService userService;

	@Autowired
	PostService postService;

	@Autowired
	RedisService redisService;

	@Autowired
	ImageService imageService;

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

			String value = redisService.getValue(ConstantsUtil.REDIS_USER_UNIQUENAME_PREFIX + ":" + registNameDTO.getUniqueName());

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

	@PostMapping(path = "/getUserList")
	public DefaultResult getUserList(@RequestBody UserIdListDTO userIdListDTO, HttpSession session) {

		UserMapResult ur = new UserMapResult();

		Map<Long,User> userMap = userService.getUserMapByIdList(userIdListDTO.getUserIdList());

		if (userMap != null) {
			ur.setUserMap(userMap);
			ur.setRemarks("OK");
			ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			ur.setRemarks("No user(s) found");
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}

	@PostMapping(path = "/updateSetting")
	public DefaultResult updateSetting(@RequestBody UserSettingDTO userSettingDTO, HttpSession session) {
		User loggedUser = (User) session.getAttribute("user");

		UserResult ur = new UserResult();

		if (loggedUser != null) {
			User user = userService.getUserById(loggedUser.getUserId());

			if (user.getPassword().equals(userSettingDTO.getPassword())) {

				user = userService.updateSetting(user, userSettingDTO);

				if (user == null) {
					ur.setRemarks("Unique Name already in use");
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
					return ur;
				} else {
					ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
					session.setAttribute("user", user);
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

	@PostMapping(value = "/uploadProfileImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadProfileImage(ImageUploadDTO imageInfo, HttpSession session) throws IOException, NullPointerException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			try {

				String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "");

				logger.info("inside upload image");

				MultipartFile fromFile = imageInfo.getFile();

				String fileName = uuidPrefix + "." + FilenameUtils.getExtension(fromFile.getOriginalFilename());

				String targetPath = uploadFolder + fileName;

				imageService.uploadImage(fromFile, targetPath, 200);

				imageService.saveProfileImage(uploadFolder, fileName, fromFile.getSize(), user);

				result.setFilePath(fileName);
				result.setStatus(ConstantsUtil.RESULT_SUCCESS);

			} catch (Exception ex) {
				ex.printStackTrace();

				result.setStatus(ConstantsUtil.RESULT_ERROR);
				result.setRemarks(ex.getLocalizedMessage());

			}

		} else {
			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return result;
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
					;
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
}
