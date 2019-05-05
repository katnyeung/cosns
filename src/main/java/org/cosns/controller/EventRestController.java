package org.cosns.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.cosns.repository.Event;
import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.EventDetailDTO;
import org.cosns.web.result.DefaultResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class EventRestController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	EventService eventService;

	@Autowired
	HashTagService hashTagService;

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

	@GetMapping(path = "/getEventDetail")
	public Set<Event> getEvents(@RequestBody EventDetailDTO eventDTO, HttpSession session) throws ParseException {
		Set<Event> eventSet = eventService.getEventByUniqueName(eventDTO.getEventName());
		return eventSet;
	}

	@PostMapping(path = "/addEvent")
	public DefaultResult addEvent(@RequestBody EventDetailDTO eventDTO, HttpSession session) throws ParseException {
		DefaultResult dr = new DefaultResult();

		Set<Event> eventSet = eventService.getEventByUniqueName(eventDTO.getEventName());
		if (eventSet.size() > 0) {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks("Event already exist");
		} else {

			Set<String> hashTagSet = hashTagService.parseHash(eventDTO.getDescription());

			logger.info("writing hash : " + hashTagSet);
			Event event = eventService.createEvent(eventDTO);

			hashTagService.saveEventHash(event, hashTagSet);

			hashTagService.saveEventHashToRedis(event, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_EVENT);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		}
		return dr;
	}
}
