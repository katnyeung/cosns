package org.cosns.web.result;

import java.util.Set;

import org.cosns.repository.Post;

public class PostListResult extends DefaultResult {
	Set<Post> postList;

	public PostListResult() {
		super();
	}

	public Set<Post> getPostList() {
		return postList;
	}

	public void setPostList(Set<Post> postList) {
		this.postList = postList;
	}

}
