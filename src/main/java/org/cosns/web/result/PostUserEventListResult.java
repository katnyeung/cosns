package org.cosns.web.result;

import java.util.List;

import org.cosns.repository.Event;
import org.cosns.repository.Post;
import org.cosns.repository.User;

public class PostUserEventListResult extends DefaultResult {
	List<Post> postList;
	List<User> userList;
	List<Event> eventList;

	public PostUserEventListResult() {
		super();
	}

	public List<Post> getPostList() {
		return postList;
	}

	public void setPostList(List<Post> postList) {
		this.postList = postList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public List<Event> getEventList() {
		return eventList;
	}

	public void setEventList(List<Event> eventList) {
		this.eventList = eventList;
	}

}
