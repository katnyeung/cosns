package org.cosns.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.cosns.dao.EventDAO;
import org.cosns.dao.PostDAO;
import org.cosns.repository.Event;
import org.cosns.repository.HashTag;
import org.cosns.repository.Post;
import org.cosns.repository.extend.PhotoEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	PostDAO postDAO;

	@Autowired
	EventDAO eventDAO;

	public Set<Event> getAllEvents(Date start, Date end) {
		Set<Event> eventSet = eventDAO.findActiveEvent(start, end);

		eventSet.stream().forEach(u -> {
			u.setColor("red");
		});

		return eventSet;
	}

	public Set<Event> getPostSchedule(Date start, Date end) {
		Set<Event> eventSet = new HashSet<>();

		Set<Post> postSet = postDAO.findPostByDateRange(start, end);

		for (Post post : postSet) {
			Event event = new PhotoEvent();
			event.setStart(post.getCreatedate());
			event.setEnd(post.getCreatedate());

			StringBuilder sb = new StringBuilder(post.getUser().getEmail());
			for (HashTag hashTag : post.getHashtags()) {
				sb.append("#");
				sb.append(hashTag.getHashTag());
			}
			event.setTitle(sb.toString());

			event.setUrl("/p/" + post.getPostId());
			event.setColor("blue");
			
			eventSet.add(event);
		}

		return eventSet;
	}
}
