package org.cosns.repository;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

import org.cosns.repository.extend.PostImage;
import org.cosns.service.RedisService;
import org.cosns.util.Auditable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "post")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "post_type")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public abstract class Post extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postId;

	private Date releaseDate;

	@Transient
	@Autowired
	RedisService redisService;

	@Lob
	@Column(nullable = true)
	private String message;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@OrderBy("seq ASC")
	private Set<PostImage> postImages;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<HashTag> hashtags;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<PostReaction> postReaction;

	@Transient
	Long likeCount;

	@Transient
	Long retweetCount;

	@Transient
	boolean isLiked;

	@Transient
	boolean isRetweeted;

	public String getType() {
		DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);

		return val == null ? null : val.value();
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<PostImage> getPostImages() {
		return postImages;
	}

	public void setPostImages(Set<PostImage> postImages) {
		this.postImages = postImages;
	}

	public Set<HashTag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<HashTag> hashtags) {
		this.hashtags = hashtags;
	}

	public Set<PostReaction> getPostReaction() {
		return postReaction;
	}

	public void setPostReaction(Set<PostReaction> postReaction) {
		this.postReaction = postReaction;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(Long retweetCount) {
		this.retweetCount = retweetCount;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean liked) {
		this.isLiked = liked;
	}

	public boolean isRetweeted() {
		return isRetweeted;
	}

	public void setRetweeted(boolean retweeted) {
		this.isRetweeted = retweeted;
	}

}