package org.cosns.web.result;

import java.util.List;

import org.cosns.repository.post.Post;

public class PostListResult extends DefaultResult {
	List<Post> postList;
	
	public PostListResult() {
		super();
	}

	public List<Post> getPostList() {
		return postList;
	}

	public void setPostList(List<Post> postList) {
		this.postList = postList;
	}

}
