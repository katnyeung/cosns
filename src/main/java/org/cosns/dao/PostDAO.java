package org.cosns.dao;

import java.util.Date;
import java.util.Set;

import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostDAO extends JpaRepository<Post, Long> {

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.userId IN (SELECT u.followers.userId FROM User u WHERE u.userId = :userId ) AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findTimelinePosts(@Param("userId") Long userId);

	@Query("SELECT p FROM Post p WHERE TYPE(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findRandomPost();

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.uniqueName = :uniqueName AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findPostByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.userId = :userId AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public Set<Post> findPostByUserId(@Param("userId") Long userId);

	@Query("SELECT p FROM Post p WHERE p.postId IN :postIdSet")
	public Set<Post> findPostByPostIdSet(@Param("postIdSet") Set<Long> postIdSet);

	@Query("SELECT p FROM Post p WHERE TYPE(p) <> RetweetPost AND p.createdate >= :start AND p.createdate <= :end")
	public Set<Post> findPostByDateRange(@Param("start") Date start, @Param("end") Date end);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE TYPE(p) = RetweetPost AND (p.post.postId = :postId AND p.user.userId = :userId)")
	public Set<Post> findRetweetedPost(@Param("postId") Long postId, @Param("userId") Long userId);
}
