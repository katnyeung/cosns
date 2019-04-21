package org.cosns.web.result;

import java.util.Map;

import org.cosns.repository.User;

public class UserMapResult extends DefaultResult {
	Map<Long, User> userMap;

	public Map<Long, User> getUserMap() {
		return userMap;
	}

	public void setUserMap(Map<Long, User> userMap) {
		this.userMap = userMap;
	}

}
