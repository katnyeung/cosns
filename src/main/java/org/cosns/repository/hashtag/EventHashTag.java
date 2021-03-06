package org.cosns.repository.hashtag;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.event.Event;
import org.cosns.util.ConstantsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "event")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EventHashTag extends HashTag {

	@JsonIgnore
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", referencedColumnName = "eventId")
	private Event event;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	@Override
	public String getRedisValue() {
		return ConstantsUtil.REDIS_TAG_TYPE_EVENT + ":" + event.getEventId();
	}

}