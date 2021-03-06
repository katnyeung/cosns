package org.cosns.repository.event;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.repository.User;
import org.cosns.repository.hashtag.EventHashTag;
import org.cosns.repository.image.EventImage;
import org.cosns.repository.post.Post;
import org.cosns.repository.postreaction.EventCommentReaction;
import org.cosns.util.Auditable;
import org.cosns.util.EventMessage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "event", indexes = { @Index(name = "INDEX_EVENT_UNIQUENAME", columnList = "eventName") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public abstract class Event extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long eventId;

	@JsonFormat(timezone = "GMT+08:00", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	Date start;
	@JsonFormat(timezone = "GMT+08:00", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	Date end;

	@NotNull
	@Size(max = 255)
	String eventName;

	@NotNull
	@Size(max = 255)
	String eventKey;

	@NotNull
	@Lob
	String url;

	@Lob
	@Column(nullable = true)
	String description;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	String status = "P";

	@Lob
	@Column(nullable = true)
	String location;

	@Transient
	String color;

	@Transient
	String image;

	@Transient
	String title;

	Long totalViewCount = (long) 0;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	private List<EventHashTag> hashtags;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	@OrderBy("seq ASC")
	private List<EventImage> eventImages;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_user_id", referencedColumnName = "userId")
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by_user_id", referencedColumnName = "userId")
	private User approvedBy;

	@Transient
	private List<Post> postList;

	@Transient
	private List<EventMessage> messageList;

	@JsonIgnore
	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	private Set<EventCommentReaction> commentReaction;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<EventHashTag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<EventHashTag> hashtags) {
		this.hashtags = hashtags;
	}

	public List<EventImage> getEventImages() {
		return eventImages;
	}

	public void setEventImages(List<EventImage> eventImages) {
		this.eventImages = eventImages;
	}

	public List<Post> getPostList() {
		return postList;
	}

	public void setPostList(List<Post> postList) {
		this.postList = postList;
	}

	public List<EventMessage> getMessageList() {
		return messageList;
	}

	public void setMessageList(List<EventMessage> messageList) {
		this.messageList = messageList;
	}

	public Long getTotalViewCount() {
		return totalViewCount;
	}

	public void setTotalViewCount(Long totalViewCount) {
		this.totalViewCount = totalViewCount;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public User getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(User approvedBy) {
		this.approvedBy = approvedBy;
	}

}
