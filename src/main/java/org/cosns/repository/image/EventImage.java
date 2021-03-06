package org.cosns.repository.image;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.event.Event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "event")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EventImage extends Image {

	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "event_id", referencedColumnName = "eventId")
	private Event event;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

}