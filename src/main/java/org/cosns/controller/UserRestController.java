package org.cosns.controller;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.FriendRequest;
import org.cosns.repository.User;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.DefaultException;
import org.cosns.web.DTO.RegistNameDTO;
import org.cosns.web.DTO.UserFormDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	UserService userService;

	@PostMapping(path = "/login")
	public DefaultResult login(@RequestBody UserFormDTO userDTO, HttpSession session) throws DefaultException {
		UserResult ur = new UserResult();

		User user = userService.verifyUser(userDTO);

		if (user != null) {
			session.setAttribute("user", user);
			ur.setUser(user);
			ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			throw new DefaultException(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return ur;
	}

	@PostMapping(path = "/checkUniqueName")
	public DefaultResult registUniqueName(@RequestBody RegistNameDTO registNameDTO, HttpSession session) throws DefaultException {
		User loggedUser = (User) session.getAttribute("user");

		UserResult ur = new UserResult();

		if (loggedUser != null) {

			User user = userService.getUserByUniqueName(registNameDTO.getUniqueName());
			if (user == null) {
				ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}
		} else {
			throw new DefaultException(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return ur;
	}

	@GetMapping(path = "/logout")
	public DefaultResult logout(HttpSession session) throws DefaultException {
		UserResult ur = new UserResult();

		session.setAttribute("user", null);

		ur.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return ur;
	}

	@PostMapping(path = "/register")
	public DefaultResult register(@RequestBody UserFormDTO userDTO, HttpSession session) throws DefaultException {
		UserResult ur = new UserResult();

		User user = userService.registerUser(userDTO);

		if (user != null) {
			session.setAttribute("user", user);
			ur.setUser(user);
			ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			throw new DefaultException("Register Error : user already exist");
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
				} else {
					ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_ADD_FRIEND_FAIL);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
				}

			} else {
				ur.setRemarks("Friend request exist");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}

		} else {
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
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
				user = userService.follow(userInDB, targetUserId);

				if (user != null) {
					ur.setUser(user);
					ur.setStatus(ConstantsUtil.RESULT_SUCCESS);
				} else {
					ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_ADD_FRIEND_FAIL);
					ur.setStatus(ConstantsUtil.RESULT_ERROR);
				}

			} else {
				ur.setRemarks("Friend request exist");
				ur.setStatus(ConstantsUtil.RESULT_ERROR);
			}

		} else {
			ur.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
			ur.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return ur;
	}
}
