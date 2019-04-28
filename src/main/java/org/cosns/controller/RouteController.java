package org.cosns.controller;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.User;
import org.cosns.service.ImageService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RouteController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	RedisService redisService;

	@Autowired
	UserService userService;

	@Autowired
	ImageService imageService;

	@GetMapping(path = "/")
	public String index(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		return "index";
	}

	@GetMapping(path = "w")
	public String writePost(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
			return "writePost";
		}

		return "redirect:/";
	}

	@GetMapping(path = "w/{dateStart}")
	public String writePostWithDate(@PathVariable("dateStart") String dateStart, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
			model.addAttribute("releaseDate", dateStart);
			logger.info("releaseDate : " + dateStart);
			return "writePost";
		}

		return "redirect:/";
	}

	@GetMapping(path = "c")
	public String viewCalendar(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		return "viewCalendar";
	}

	@GetMapping(path = "t")
	public String viewTimeline(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		} else {
			return "redirect:/";
		}

		return "viewTimeline";
	}

	@GetMapping(path = "p/{postId}")
	public String viewPost(@PathVariable("postId") Long postId, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		model.addAttribute("postId", postId);

		return "viewPost";
	}

	@GetMapping(path = "ht/{hashTag}")
	public String viewHashTag(@PathVariable("hashTag") String hashTag, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		model.addAttribute("hashTag", hashTag);

		return "viewTag";
	}

	@GetMapping(path = "u")
	public String viewProfile(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
			model.addAttribute("targetUser", loggedUser);
		} else {
			return "redirect:/";
		}

		return "viewProfile";
	}

	@GetMapping(path = "u/{username}")
	public String viewProfile(@PathVariable("username") String username, HttpSession session, Model model) {
		try {
			User loggedUser = (User) session.getAttribute("user");
			if (loggedUser != null) {
				model.addAttribute("user", loggedUser);
			}
			String userIdString = (String) redisService.getHashValue(ConstantsUtil.REDIS_USER_GROUP, ConstantsUtil.REDIS_USER_TYPE_ID);

			User targetUser = null;

			if (userIdString != null) {
				Long userId = Long.parseLong(userIdString);
				targetUser = userService.getUserById(userId);
			} else {
				targetUser = userService.getUserByUniqueName(username);
			}

			if (targetUser != null) {
				model.addAttribute("targetUser", targetUser);
			} else {
				logger.info("not found uniquename " + username);
				targetUser = userService.getUserById(Long.parseLong(username));
				if (targetUser != null) {
					logger.info("found id " + username);
					if (targetUser.getUniqueName() != null) {
						return "redirect:/u/" + targetUser.getUniqueName();
					} else {
						model.addAttribute("targetUser", targetUser);
					}
				} else {

					logger.info("either not found id " + username);
					return "redirect:/";
				}
			}

		} catch (NumberFormatException ne) {
			ne.printStackTrace();
		}

		return "viewProfile";
	}

	@GetMapping(path = "setting")
	public String viewSetting(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
			model.addAttribute("targetUser", loggedUser);
		} else {
			return "redirect:/";
		}

		return "viewSetting";
	}
}
