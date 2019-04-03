package org.cosns.web.result;

import java.util.Set;

import org.cosns.repository.Event;

public class EventResult extends DefaultResult {

	Set<Event> events;

	public Set<Event> getEvents() {
		return events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

}
