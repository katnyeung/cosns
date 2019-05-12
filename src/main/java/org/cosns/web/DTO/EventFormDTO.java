package org.cosns.web.DTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventFormDTO {
	String eventName;
	String eventKey;
	List<Map<String, String>> keyHashTag;

	String description;

	String url;

	String location;

	Date startDate;
	Date endDate;

	List<String> fileList;

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Map<String, String>> getKeyHashTag() {
		return keyHashTag;
	}

	public void setKeyHashTag(List<Map<String, String>> keyHashTag) {
		this.keyHashTag = keyHashTag;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

}
