package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostDAO extends JpaRepository<Post, Long> {

	@Query("FROM Post WHERE status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY createdate DESC")
	public Set<Post> findRandomPost();
}
