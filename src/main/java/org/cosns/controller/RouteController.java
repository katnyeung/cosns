package org.cosns.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cosns.auth.Auth;
import org.cosns.repository.User;
import org.cosns.service.ImageService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.CookieUtil;
import org.cosns.util.DefaultException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Auth
	@GetMapping(path = "w")
	public String writePost(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		model.addAttribute("user", loggedUser);
		return "writePost";
	}

	@Auth
	@GetMapping(path = "w/{dateStart}")
	public String writePostWithDate(@PathVariable("dateStart") String dateStart, HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		model.addAttribute("user", loggedUser);
		model.addAttribute("releaseDate", dateStart);

		return "writePost";

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

		model.addAttribute("DOMAIN", ConstantsUtil.DOMAIN);

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

		model.addAttribute("DOMAIN", ConstantsUtil.DOMAIN);

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
				logger.debug("not found uniquename " + userName);
				targetUser = userService.getUserById(Long.parseLong(userName));
				if (targetUser != null) {
					logger.debug("found id " + userName);
					if (targetUser.getUniqueName() != null) {
						return "redirect:/@" + targetUser.getUniqueName();
					} else {
						model.addAttribute("targetUser", targetUser);
					}
				} else {

					logger.debug("either not found id " + userName);
					return "redirect:/";
				}
			}

		} catch (NumberFormatException ne) {
			ne.printStackTrace();
		}

		return "viewProfile";
	}

	@GetMapping(path = "@{userName}/{postKey}")
	public String viewPost(@PathVariable("userName") String userName, @PathVariable("postKey") String postKey, HttpSession session, HttpServletRequest request, Model model) throws DefaultException {
		User loggedUser = (User) session.getAttribute("user");

		model.addAttribute("DOMAIN", ConstantsUtil.DOMAIN);
		
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

	@Auth
	@GetMapping(path = "writeEvent")
	public String addEvent(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		model.addAttribute("user", loggedUser);

		return "writeEvent";
	}

	@Auth
	@GetMapping(path = "writeEvent/{startEndDate}")
	public String addEvent(@PathVariable("startEndDate") String startEndDate, HttpSession session, Model model) {
		String[] startEndDateArr = startEndDate.split("&");

		String startDate = startEndDateArr[0];
		String endDate = startEndDateArr[1];

		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);

		User loggedUser = (User) session.getAttribute("user");

		model.addAttribute("user", loggedUser);

		return "writeEvent";
	}

	@GetMapping(path = "loginWithFB")
	public String loginWithFB(HttpServletRequest request, HttpServletResponse response) {

		StringBuilder sb = new StringBuilder("redirect:")
				.append("https://www.facebook.com/v3.3/dialog/oauth")
				.append("?client_id=").append(ConstantsUtil.FB_CLIENT_ID)
				.append("&redirect_uri=").append(ConstantsUtil.DOMAIN + "/validateLogin")
				.append("&scope=").append("email")
				.append("&state=").append("test");

		return sb.toString();

	}

	@GetMapping(path = "validateLogin")
	public String validateLogin(String code, HttpServletRequest request, HttpServletResponse response, Model model, HttpSession session) {
		ObjectMapper om = new ObjectMapper();
		Map<String, Object> fbInfoMap = new HashMap<>();

		String fbResponse = "";
		try {
			StringBuilder sb = new StringBuilder("https://graph.facebook.com/v3.3/oauth/access_token")
					.append("?client_id=").append(ConstantsUtil.FB_CLIENT_ID)
					.append("&redirect_uri=").append(ConstantsUtil.DOMAIN + "/validateLogin")
					.append("&client_secret=").append(ConstantsUtil.FB_SECRET)
					.append("&code=").append(code);



			fbResponse = Jsoup.connect(sb.toString()).timeout(60000).ignoreContentType(true).method(Connection.Method.GET).execute().body();

			logger.info(fbResponse);

			Map<String, Object> responseMap = om.readValue(fbResponse, new TypeReference<Map<String, Object>>() {
			});

			fbInfoMap.putAll(responseMap);

			sb = new StringBuilder("").append("https://graph.facebook.com/v3.3/me")
					.append("?fields=").append("id%2Cname%2Cemail")
					.append("&access_token=").append(responseMap.get("access_token"));

			fbResponse = Jsoup.connect(sb.toString()).timeout(60000).ignoreContentType(true).method(Connection.Method.GET).execute().body();

			responseMap = om.readValue(fbResponse, new TypeReference<Map<String, Object>>() {
			});

			fbInfoMap.putAll(responseMap);

			if (fbInfoMap.get("id") != null) {
				User user = userService.getUserByFbId((String) fbInfoMap.get("id"));

				if (user != null) {

					session.setAttribute("user", user);

					CookieUtil.handleCookie(request, response, user);
					return "redirect:/";

				} else {

					model.addAttribute("fbId", (String) fbInfoMap.get("id"));
					return "fbRegister";
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/";

	}
}
