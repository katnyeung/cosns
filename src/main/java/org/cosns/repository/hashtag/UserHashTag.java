package org.cosns.repository.hashtag;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "user")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UserHashTag extends HashTag {

	@JsonIgnore
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String getRedisValue() {
		return ConstantsUtil.REDIS_TAG_TYPE_USER + ":" + user.getUserId();
	}
}