package org.cosns.repository.postreaction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.event.Event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "event_comment")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EventCommentReaction extends PostReaction {

	String message;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "event_id", referencedColumnName = "eventId")
	private Event event;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}