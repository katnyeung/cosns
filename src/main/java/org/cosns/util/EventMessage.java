package org.cosns.util;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EventMessage {
	@JsonFormat(timezone = "GMT+08:00", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	Date date;
	String message;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
