package org.cosns.repository.hashtag;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cosns.repository.post.Post;
import org.cosns.util.ConstantsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "post")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PostHashTag extends HashTag {

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

	@Override
	public String getRedisValue() {
		return ConstantsUtil.REDIS_TAG_TYPE_PHOTO + ":" + post.getPostId();
	}

}