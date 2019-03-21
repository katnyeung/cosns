package org.cosns.repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.util.Auditable;

@Entity
public class Post extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postId;

	@NotNull
	@Lob
	private String title;

	@NotNull
	@Lob
	private String contentHtml;

	@NotNull
	private int likeCount;

	@NotNull
	@Size(max = 1)
	private String status;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentHtml() {
		return contentHtml;
	}

	public void setContentHtml(String contentHtml) {
		this.contentHtml = contentHtml;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
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

}