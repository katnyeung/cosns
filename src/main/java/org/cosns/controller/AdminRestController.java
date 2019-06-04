package org.cosns.controller;

import javax.servlet.http.HttpSession;

import org.cosns.auth.Auth;
import org.cosns.scheduled.SupportTaskScheduler;
import org.cosns.service.EventService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.result.DefaultResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminRestController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	SupportTaskScheduler taskScheduler;

	@Autowired
	EventService eventService;

	@Auth(type = "ADMIN")
	@GetMapping(path = "/sync")
	public DefaultResult sync(HttpSession session) {
		DefaultResult dr = new DefaultResult();
		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		taskScheduler.scheduleDailySyncPostCount();

		return dr;
	}

	@Auth(type = "ADMIN")
	@GetMapping(path = "/resetRedis")
	public DefaultResult reset(HttpSession session) {
		DefaultResult dr = new DefaultResult();
		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		taskScheduler.resetKeysInRedis();

		return dr;
	}

	@Auth(type = "ADMIN")
	@GetMapping(path = "/removeEvent/{eventId}")
	public DefaultResult removeEvent(@PathVariable("eventId") Long eventId, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		dr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		eventService.removeEvent(eventId);

		return dr;
	}
}
