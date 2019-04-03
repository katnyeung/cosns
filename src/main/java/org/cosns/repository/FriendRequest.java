package org.cosns.repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.util.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class FriendRequest extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long requestId;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	User fromUser;

	@OneToOne
	@JoinColumn(name = "target_user_id", referencedColumnName = "userId")
	User targetUser;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	String status = "P";

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public User getFromUser() {
		return fromUser;
	}

	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}

}