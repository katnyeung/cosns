package org.cosns.web.result;

import org.cosns.repository.PostReaction;

public class PostReactionResult extends DefaultResult {
	PostReaction postReaction;

	public PostReaction getPostReaction() {
		return postReaction;
	}

	public void setPostReaction(PostReaction postReaction) {
		this.postReaction = postReaction;
	}

}
