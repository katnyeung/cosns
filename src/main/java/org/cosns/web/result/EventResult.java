package org.cosns.web.result;

import java.util.List;
import java.util.Set;

import org.cosns.repository.Event;
import org.cosns.repository.User;

public class EventResult extends DefaultResult {

	List<User> userList;

	Set<Event> events;

	public Set<Event> getEvents() {
		return events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

}
