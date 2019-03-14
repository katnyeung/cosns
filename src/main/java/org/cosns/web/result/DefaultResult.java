package org.cosns.web.result;

import java.util.Calendar;
import java.util.Date;

public class DefaultResult {
	Date datetime;
	String status;
	String remarks;

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public DefaultResult() {
		datetime = Calendar.getInstance().getTime();
		status = null;
		remarks = null;
	}
}
