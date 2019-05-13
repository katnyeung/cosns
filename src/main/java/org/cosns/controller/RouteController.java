package org.cosns.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cosns.repository.User;
import org.cosns.service.ImageService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.DefaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RouteController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

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

	@GetMapping(path = "@{userName}/{postKey}")
	public String viewPost(@PathVariable("userName") String userName, @PathVariable("postKey") String postKey, HttpSession session, HttpServletRequest request, Model model) throws DefaultException {
		User loggedUser = (User) session.getAttribute("user");

		try {
			model.addAttribute("referrer", URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		logger.info("searching from redis : " + ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + postKey + " ->" + ConstantsUtil.REDIS_POST_ID);

		logger.info("hasKey : " + redisService.hasKey(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + postKey));

		if (!redisService.hasKey(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + postKey)) {
			logger.info("redis : " + ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + postKey + " -> " + ConstantsUtil.REDIS_POST_ID + ", redis not found");

			try {
				logger.info("postId string : " + postKey);
				Long postId = Long.parseLong(postKey);
				logger.info("try to parse ID : " + postId);
				model.addAttribute("postId", postId);
			} catch (NumberFormatException nfe) {
				logger.info("post not found");
				throw new DefaultException("post not found");
			}
		} else {

			String postIdString = (String) redisService.getHashValue(ConstantsUtil.REDIS_POST_NAME_GROUP + ":" + userName + "/" + postKey, ConstantsUtil.REDIS_POST_ID);

			model.addAttribute("postId", postIdString);
		}

		return "viewPost";
	}

	@GetMapping(path = "h/{hashTag}/{orderByType}")
	public String viewHashTag(@PathVariable("hashTag") String hashTag, @PathVariable("orderByType") String orderByType, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		model.addAttribute("orderByType", orderByType);
		model.addAttribute("hashTag", hashTag);

		return "viewTag";
	}

	@GetMapping(path = "h/{hashTag}")
	public String viewHashTag(@PathVariable("hashTag") String hashTag, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}
		model.addAttribute("orderByType", "date");
		model.addAttribute("hashTag", hashTag);

		return "viewTag";
	}

	@GetMapping(path = "@")
	public String viewProfile(HttpSession session, HttpServletRequest request, Model model) {
		try {
			model.addAttribute("referrer", URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
			model.addAttribute("targetUser", loggedUser);
		} else {
			return "redirect:/";
		}

		return "viewProfile";
	}

	@GetMapping(path = "@{userName}")
	public String viewProfile(@PathVariable("userName") String userName, HttpSession session, HttpServletRequest request, Model model) {
		try {
			model.addAttribute("referrer", URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
				targetUser = userService.getUserByUniqueName(userName);
			}

			if (targetUser != null) {
				model.addAttribute("targetUser", targetUser);
			} else {
				logger.info("not found uniquename " + userName);
				targetUser = userService.getUserById(Long.parseLong(userName));
				if (targetUser != null) {
					logger.info("found id " + userName);
					if (targetUser.getUniqueName() != null) {
						return "redirect:/@" + targetUser.getUniqueName();
					} else {
						model.addAttribute("targetUser", targetUser);
					}
				} else {

					logger.info("either not found id " + userName);
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

	@GetMapping(path = "e/{eventKey}")
	public String viewEvent(@PathVariable("eventKey") String eventKey, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		model.addAttribute("eventKey", eventKey);

		return "viewEvent";
	}

	@GetMapping(path = "writeEvent")
	public String addEvent(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		} else {
			return "redirect:/";
		}

		return "writeEvent";
	}

	@GetMapping(path = "writeEvent/{startEndDate}")
	public String addEvent(@PathVariable("startEndDate") String startEndDate, HttpSession session, Model model) {
		String[] startEndDateArr = startEndDate.split("&");

		String startDate = startEndDateArr[0];
		String endDate = startEndDateArr[1];

		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);

		User loggedUser = (User) session.getAttribute("user");
		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		} else {
			return "redirect:/";
		}

		return "writeEvent";
	}
}
