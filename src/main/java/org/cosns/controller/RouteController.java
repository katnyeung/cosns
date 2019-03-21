package org.cosns.controller;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RouteController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@GetMapping(path = "/")
	public String index() {

		return "index";
	}

	@GetMapping(path = "writePost")
	public String writePost() {

		return "writePost2";
	}

}
