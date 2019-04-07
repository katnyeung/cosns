package org.cosns.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.Event;
import org.cosns.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class EventRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	EventService eventService;

	@GetMapping(path = "/getEvents")
	public Set<Event> getEvents(@RequestParam("start") String start, @RequestParam("end") String end, HttpSession session) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

		Date dateStart = sdf.parse(start);
		Date dateEnd = sdf.parse(end);

		Set<Event> eventSet = eventService.getAllEvents(dateStart, dateEnd);

		Set<Event> postSet = eventService.getPostSchedule(dateStart, dateEnd);

		eventSet.addAll(postSet);

		return eventSet;
	}
}
