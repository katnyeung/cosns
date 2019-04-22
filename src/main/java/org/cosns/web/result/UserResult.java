package org.cosns.web.result;

import java.util.Map;

import org.cosns.repository.User;

public class UserResult extends DefaultResult {
	Map<Long, User> userMap;

	User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Map<Long, User> getUserMap() {
		return userMap;
	}

	public void setUserMap(Map<Long, User> userMap) {
		this.userMap = userMap;
	}

}
