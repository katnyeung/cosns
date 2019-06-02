package org.cosns.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.cosns.auth.Auth;
import org.cosns.repository.User;
import org.cosns.repository.event.Event;
import org.cosns.repository.hashtag.HashTag;
import org.cosns.repository.post.Post;
import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.service.PostService;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.EventMessage;
import org.cosns.web.DTO.EventFormDTO;
import org.cosns.web.DTO.EventMessageDTO;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Autowired
	UserService userService;

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

	@Transactional
	@PostMapping(path = "/getEventDetail")
	public DefaultResult getEvents(@RequestBody EventFormDTO eventDTO, HttpSession session) throws ParseException {
		List<String> postTypeList = Arrays.asList(ConstantsUtil.REDIS_TAG_TYPE_ALL_POST.split(","));
		User user = (User) session.getAttribute("user");

		EventResult er = new EventResult();

		Set<Event> eventSet = eventService.getEventByEventKey(eventDTO.getEventKey());
		if (eventSet.iterator().hasNext()) {
			Event event = eventSet.iterator().next();

			List<String> postIdSet = hashTagService.searchPostByHashTagSet(event.getHashtags().stream().map(HashTag::getHashTag).collect(Collectors.toSet()), postTypeList);
			if (postIdSet.size() > 0) {

				List<Post> postList = null;
				if (user != null) {
					postList = postService.setLikeRetweetedAndCount(postService.getPostByIds(postIdSet), user.getUserId());
				} else {
					postList = postService.setLikeRetweetCount(postService.getPostByIds(postIdSet));
				}
				event.setPostList(postList);

			}
			List<EventMessage> emList = redisService.getMessageList(ConstantsUtil.REDIS_EVENT_NAME_GROUP + ":" + eventDTO.getEventKey() + ":message");
			Set<Long> userIdList = emList.stream().map(EventMessage::getUserId).collect(Collectors.toSet());

			List<User> userList = userService.getUserByUserIdList(userIdList);

			er.setUserList(userList);

			event.setMessageList(emList);

			eventService.incrViewCount(event.getEventId());
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

	@Auth
	@PostMapping(path = "/addEvent")
	public DefaultResult addEvent(@RequestBody EventFormDTO eventDTO, HttpSession session) throws ParseException {
		DefaultResult dr = new DefaultResult();

		User user = (User) session.getAttribute("user");

		Set<String> hashTagSet = mapToKeySet(eventDTO.getKeyHashTag());

		String eventKey = eventService.genEventKey(eventDTO, hashTagSet);

		Set<Event> eventSet = eventService.getEventByEventKey(eventKey);

		if (eventSet.size() > 0) {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks("Event already exist");
		} else {

			logger.info("writing hash : " + hashTagSet);
			Event event = eventService.createEvent(eventKey, eventDTO, hashTagSet, user);

			hashTagService.saveEventHashTag(event, hashTagSet);

			hashTagService.saveEventHashTagToRedis(event, hashTagSet, ConstantsUtil.REDIS_TAG_GROUP, ConstantsUtil.REDIS_TAG_TYPE_EVENT);

			redisService.saveEventKeyToRedis(event);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		}

		return dr;
	}

	@Auth
	@PostMapping(path = "/addMessage")
	public DefaultResult addMessage(@RequestBody EventMessageDTO messageDTO, HttpSession session) throws ParseException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		DefaultResult dr = new DefaultResult();

		Set<Event> eventSet = eventService.getEventByEventKey(messageDTO.getEventKey());

		User user = (User) session.getAttribute("user");

		if (eventSet.size() > 0) {

			EventMessage em = new EventMessage();
			em.setDate(Calendar.getInstance().getTime());
			em.setMessage(messageDTO.getMessage());
			em.setUserId(user.getUserId());

			logger.info("writing message : " + mapper.writeValueAsString(em));

			redisService.addEventMessage(ConstantsUtil.REDIS_EVENT_NAME_GROUP + ":" + messageDTO.getEventKey() + ":message", mapper.writeValueAsString(em));

			dr.setRemarks(mapper.writeValueAsString(em));
			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setStatus("no events");
		}
		return dr;
	}

	@Auth
	@PostMapping(path = "/addComment")
	public DefaultResult addComment(@RequestBody EventMessageDTO messageDTO, HttpSession session) throws ParseException, JsonProcessingException {

		DefaultResult dr = new DefaultResult();

		Set<Event> eventSet = eventService.getEventByEventKey(messageDTO.getEventKey());

		User user = (User) session.getAttribute("user");

		if (eventSet.size() > 0) {
			
			
			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setStatus("no events");
		}
		return dr;
	}
	private Set<String> mapToKeySet(List<Map<String, String>> keyHashTag) {
		return keyHashTag.stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toSet());
	}
}
