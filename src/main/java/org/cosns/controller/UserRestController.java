package org.cosns.controller;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.User;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.UserFormDTO;
import org.cosns.web.result.DefaultResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	UserService userService;

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public DefaultResult login(@RequestBody UserFormDTO userDTO, HttpSession session) {
		DefaultResult defaultResult = new DefaultResult();

		User user = userService.verifyUser(userDTO);

		if (user != null) {
			session.setAttribute("user", user);
			defaultResult.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			defaultResult.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return defaultResult;
	}

	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public DefaultResult register(@RequestBody UserFormDTO userDTO, HttpSession session) {
		DefaultResult defaultResult = new DefaultResult();

		User user = userService.registerUser(userDTO);

		if (user != null) {
			session.setAttribute("user", user);
			defaultResult.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			defaultResult.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return defaultResult;
	}
}
