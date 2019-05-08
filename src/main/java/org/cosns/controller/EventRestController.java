package org.cosns.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.cosns.repository.Event;
import org.cosns.repository.HashTag;
import org.cosns.repository.Post;
import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.EventFormDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.EventResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Autowired
	RedisService redisService;

	@Autowired
	PostService postService;

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

	@PostMapping(path = "/getEventDetail")
	public DefaultResult getEvents(@RequestBody EventFormDTO eventDTO, HttpSession session) throws ParseException {
		List<String> postTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_ALL_POST.split(","));
		EventResult er = new EventResult();

		Set<Event> eventSet = eventService.getEventByEventKey(eventDTO.getEventName());
		for (Event event : eventSet) {
			List<String> postIdSet = hashTagService.searchPostByHashTagSet(event.getHashtags().stream().map(HashTag::getHashTag).collect(Collectors.toSet()), postTypeList);
			if (postIdSet.size() > 0) {
				List<Post> postList = postService.getPostByIds(postIdSet);
				event.setPostList(postList);
			}
		}

		logger.info("getting event : " + eventSet.size());
		er.setStatus(ConstantsUtil.RESULT_SUCCESS);
		er.setEvents(eventSet);

		return er;
	}

	@GetMapping(path = "/getRelatedTag/{query}")
	public Set<String> getRelatedTag(@PathVariable("query") String query, HttpSession session) throws ParseException {

		List<String> eventTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_EVENT.split(","));

		Set<String> keyList = hashTagService.getRelatedTag(query, eventTypeList);

		return keyList;
	}

	@PostMapping(path = "/addEvent")
	public DefaultResult addEvent(@RequestBody EventFormDTO eventDTO, HttpSession session) throws ParseException {
		DefaultResult dr = new DefaultResult();

		Set<Event> eventSet = eventService.getEventByEventKey(eventDTO.getEventName());

		if (eventSet.size() > 0) {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks("Event already exist");
		} else {

			Set<String> hashTagSet = mapToKeySet(eventDTO.getKeyHashTag());

			logger.info("writing hash : " + hashTagSet);
			Event event = eventService.createEvent(eventDTO, hashTagSet);

			hashTagService.saveEventHash(event, hashTagSet);

			hashTagService.saveEventHashToRedis(event, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_EVENT);

			redisService.saveEventKeyToRedis(event);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		}
		return dr;
	}

	private Set<String> mapToKeySet(List<Map<String, String>> keyHashTag) {
		return keyHashTag.stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toSet());
	}
}
