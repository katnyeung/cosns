package org.cosns.controller;

import java.util.logging.Logger;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@RequestMapping(method = RequestMethod.GET)
	public String index() {

		return "index";
	}


}
