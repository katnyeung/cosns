package org.cosns.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cosns.dao.EventDAO;
import org.cosns.dao.PostDAO;
import org.cosns.repository.Event;
import org.cosns.repository.HashTag;
import org.cosns.repository.Image;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.repository.extend.PhotoEvent;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.EventDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PostDAO postDAO;

	@Autowired
	EventDAO eventDAO;

	public Set<Event> getAllEvents(Date start, Date end) {
		Set<Event> eventSet = eventDAO.findActiveEvent(start, end);

		eventSet.stream().forEach(u -> {
			u.setColor("red");
			u.setTitle(u.getEventName());
		});

		return eventSet;
	}

	public Set<Event> getEventByUniqueName(String uniqueName) {
		Set<Event> eventSet = eventDAO.getEventByUniqueName(uniqueName);

		eventSet.stream().forEach(u -> {
			u.setColor("red");
			u.setTitle(u.getEventName());
		});

		return eventSet;
	}

	public Set<Event> getPostSchedule(Date start, Date end) {
		Set<Event> eventSet = new HashSet<>();

		List<Post> postSet = postDAO.findPostByDateRange(start, end);

		for (Post post : postSet) {
			Event event = new PhotoEvent();
			if (post.getReleaseDate() != null) {
				event.setStart(post.getReleaseDate());
				event.setEnd(post.getReleaseDate());
			} else {
				event.setStart(post.getCreatedate());
				event.setEnd(post.getCreatedate());
			}

			String userName;

			if (post.getUser().getDisplayName() != null) {
				userName = post.getUser().getDisplayName();
			} else if (post.getUser().getUniqueName() != null) {
				userName = post.getUser().getUniqueName();
			} else {
				userName = "@" + post.getUser().getUserId();
			}

			StringBuilder sb = new StringBuilder(userName);
			for (HashTag hashTag : post.getHashtags()) {
				sb.append("#");
				sb.append(hashTag.getHashTag());
				sb.append(" ");
			}

			event.setUrl("/@" + post.getPostKey());
			event.setColor("blue");

			event.setTitle(post.getPostKey());

			for (Image image : post.getPostImages()) {
				event.setImage("/images/" + image.getThumbnailFilename());
				break;
			}

			eventSet.add(event);
		}

		return eventSet;
	}

	public Event createEvent(EventDetailDTO eventDTO) {
		Event event = new PhotoEvent();
		event.setEventName(eventDTO.getEventName());
		event.setDescription(eventDTO.getDescription());
		event.setUrl(eventDTO.getUrl());
		event.setStart(eventDTO.getStartDate());
		event.setEnd(eventDTO.getEndDate());
		event.setStatus(ConstantsUtil.EVENT_ACTIVE);

		event = eventDAO.save(event);
		return event;
	}

	public List<Event> searchEvents(Map<Long, Integer> map, String orderBy, User user) {
		return eventDAO.getEventByIdList(map.keySet());
	}
}
