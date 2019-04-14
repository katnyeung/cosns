package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.Image;
import org.cosns.repository.Post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "post")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PostImage extends Image {

	@JsonIgnore
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", referencedColumnName = "postId")
	private Post post;

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

}