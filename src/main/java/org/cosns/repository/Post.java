package org.cosns.repository;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.util.Auditable;

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
	private Set<Image> images;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<HashTag> hashtags;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<PostReaction> postReaction;

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

	public Set<Image> getImages() {
		return images;
	}

	public void setImages(Set<Image> images) {
		this.images = images;
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

}