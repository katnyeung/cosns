package org.cosns.repository.postreaction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.post.Post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "like")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LikeReaction extends PostReaction {

	@JsonIgnore
	@ManyToOne(targetEntity=Post.class)
	@JoinColumn(name = "post_id", referencedColumnName = "postId")
	private Post post;

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

}