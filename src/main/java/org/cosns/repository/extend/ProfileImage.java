package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.Image;
import org.cosns.repository.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "profile")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProfileImage extends Image {

	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")	
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}