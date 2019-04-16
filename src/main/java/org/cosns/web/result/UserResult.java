package org.cosns.web.result;

import java.util.List;

import org.cosns.repository.User;

public class UserResult extends DefaultResult {
	User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
