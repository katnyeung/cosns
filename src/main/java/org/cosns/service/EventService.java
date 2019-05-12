package org.cosns.service;

import java.util.Calendar;
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
import org.cosns.repository.extend.EventImage;
import org.cosns.repository.extend.PhotoEvent;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.EventFormDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EventService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PostDAO postDAO;

	@Autowired
	EventDAO eventDAO;

	@Autowired
	ImageService imageService;

	public Set<Event> getAllEvents(Date start, Date end) {
		Set<Event> eventSet = eventDAO.findActiveEvent(start, end);

		eventSet.stream().forEach(u -> {
			u.setColor("red");
			u.setTitle(u.getEventName());
			u.setDescription(u.getDescription() + "<br/>" + u.getUrl());
			u.setUrl("/e/" + u.getEventKey());
			if (u.getEventImages().iterator().hasNext()) {
				u.setImage("/eimages/" + u.getEventImages().iterator().next().getThumbnailFilename());
			}

		});

		return eventSet;
	}

	public Set<Event> getEventByEventKey(String eventKey) {
		Set<Event> eventSet = eventDAO.getEventByEventKey(eventKey);

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

			event.setTitle(sb.toString());

			for (Image image : post.getPostImages()) {
				event.setImage("/images/" + image.getThumbnailFilename());
				break;
			}

			eventSet.add(event);
		}

		return eventSet;
	}

	public Event createEvent(String eventKey, EventFormDTO eventDTO, Set<String> hashTagSet) {
		Event event = new PhotoEvent();
		event.setEventName(eventDTO.getEventName());
		event.setDescription(eventDTO.getDescription());
		event.setUrl(eventDTO.getUrl());
		event.setStart(eventDTO.getStartDate());
		event.setEnd(eventDTO.getEndDate());
		event.setLocation(eventDTO.getLocation());
		event.setStatus(ConstantsUtil.EVENT_ACTIVE);

		event.setEventKey(eventKey);

		event = eventDAO.save(event);

		int count = 1;
		for (String file : eventDTO.getFileList()) {
			List<EventImage> imageSet = imageService.findPendEventImageByFilename(file);
			for (EventImage image : imageSet) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setEvent(event);
				image.setSeq(count++);
				imageService.saveEventImage(image);
			}
		}

		return event;
	}

	public String genEventKey(EventFormDTO eventDTO, Set<String> hashTagSet) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(eventDTO.getStartDate());
		// create eventKey
		StringBuilder extendedProps = new StringBuilder();
		if (hashTagSet.size() > 0) {
			String prepend = "";
			for (String hashTag : hashTagSet) {
				extendedProps.append(prepend);
				extendedProps.append(hashTag);
				prepend = "-";
			}
		}

		return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + (cal.get(Calendar.DAY_OF_MONTH) + "-" + eventDTO.getEventName()) + extendedProps.toString();

	}

	public List<Event> searchEvents(Map<Long, Integer> map, String orderBy) {
		String orderByString = getOrderByString(orderBy);

		if (map.keySet().size() > 0) {
			return eventDAO.getEventByIdList(map.keySet(), Sort.by(orderByString).descending());
		} else {
			return null;
		}

	}

	private String getOrderByString(String orderBy) {
		String orderByString = "createdate";

		if (orderBy != null) {
			switch (orderBy) {
			case "date":
				orderByString = "createdate";
				break;
			case "view":
				orderByString = "totalViewCount";
				break;
			}
		}

		return orderByString;

	}

	public void incrViewCount(Long eventId) {
		eventDAO.incrEventViewCount(eventId);
	}
}
