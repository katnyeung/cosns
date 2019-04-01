package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.cosns.repository.Post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "retweet")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class RetweetPost extends Post {

	@OneToOne
	@JoinColumn(name = "post_id", referencedColumnName = "postId")
	Post post;

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

}