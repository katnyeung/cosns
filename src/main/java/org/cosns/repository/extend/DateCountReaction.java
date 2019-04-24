package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.cosns.repository.PostReaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(indexes = { @Index(name = "INDEX_DATE_COUNT_REACTION", columnList = "year,month,day") })
@DiscriminatorValue(value = "date_count")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DateCountReaction extends PostReaction {
	int year;
	int month;
	int day;

	Long viewCount;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

}