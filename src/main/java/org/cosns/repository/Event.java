package org.cosns.repository;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long eventId;

	Date dateFrom;
	Date dateTo;

	@NotNull
	@Lob
	String eventName;
}
