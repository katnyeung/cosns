package org.cosns.controller;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@RequestMapping(method = RequestMethod.GET)
	public String index() {

		return "index";
	}


}
