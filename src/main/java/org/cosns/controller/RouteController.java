package org.cosns.controller;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.User;
import org.cosns.service.UserService;
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
	UserService userService;

	@GetMapping(path = "/")
	public String index(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		return "index";
	}

	@GetMapping(path = "w/")
	public String writePost(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		return "writePost";
	}

	@GetMapping(path = "c/")
	public String viewCalendar(HttpSession session, Model model) {
		User loggedUser = (User) session.getAttribute("user");

		if (loggedUser != null) {
			model.addAttribute("user", loggedUser);
		}

		return "viewCalendar";
	}

	@GetMapping(path = "u/{username}")
	public String viewProfile(@PathVariable("username") String username, HttpSession session, Model model) {
		try {
			User loggedUser = (User) session.getAttribute("user");
			if (loggedUser != null) {
				model.addAttribute("user", loggedUser);
			}

			User targetUser = userService.getUserByUniqueName(username);

			if (targetUser != null) {
				model.addAttribute("targetUser", targetUser);
			} else {
				logger.info("not found uniquename " + username);
				targetUser = userService.getUserById(Long.parseLong(username));
				if (targetUser != null) {
					logger.info("found id " + username);
					if (targetUser.getUniqueName() != null) {
						return "redirect:/viewProfile/" + targetUser.getUniqueName();
					} else {
						model.addAttribute("user", targetUser);
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

}
