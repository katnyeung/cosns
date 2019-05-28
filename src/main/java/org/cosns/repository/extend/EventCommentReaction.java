package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.Event;
import org.cosns.repository.PostReaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "event_comment")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EventCommentReaction extends PostReaction {

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

}