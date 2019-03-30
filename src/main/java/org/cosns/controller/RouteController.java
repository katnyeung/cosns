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
	public String index() {

		return "index";
	}

	@GetMapping(path = "w/")
	public String writePost() {

		return "writePost";
	}

	@GetMapping(path = "u/{username}")
	public String viewProfile(@PathVariable("username") String username, HttpSession session, Model model) {
		try {
			User loggedUser = (User) session.getAttribute("user");
			if (loggedUser != null) {
				model.addAttribute("checkId", loggedUser.getUserId());
			}

			User user = userService.getUserByUniqueName(username);

			if (user != null) {
				model.addAttribute("user", user);
			} else {
				logger.info("not found uniquename " + username);
				user = userService.getUserById(Long.parseLong(username));
				if (user != null) {
					logger.info("found id " + username);
					if (user.getUniqueName() != null) {
						return "redirect:/viewProfile/" + user.getUniqueName();
					} else {
						model.addAttribute("user", user);
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
