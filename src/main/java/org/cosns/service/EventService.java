package org.cosns.service;

import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import org.cosns.dao.EventDAO;
import org.cosns.repository.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	EventDAO eventDAO;

	public Set<Event> getAllEvents(Date start, Date end) {
		return eventDAO.findActiveEvent(start, end);
	}
}
