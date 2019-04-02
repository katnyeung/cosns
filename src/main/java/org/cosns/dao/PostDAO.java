package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostDAO extends JpaRepository<Post, Long> {
	
	@Query("FROM Post WHERE status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY createdate DESC")
	public Set<Post> findTimelinePosts(Long userId);
	
	@Query("FROM Post WHERE post_type = 'photo' AND status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY createdate DESC")
	public Set<Post> findRandomPost();

	@Query("FROM Post p INNER JOIN p.user u WHERE u.uniqueName = :uniqueName AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findPostByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("FROM Post p INNER JOIN p.user u WHERE u.userId = :userId AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findPostByUserId(@Param("userId") Long userId);

	@Query("FROM Post p WHERE p.postId IN :postIdSet")
	public Set<Post> findPostByPostIdSet(@Param("postIdSet") Set<Long> postIdSet);
}
