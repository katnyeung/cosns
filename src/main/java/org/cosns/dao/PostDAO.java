package org.cosns.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostDAO extends JpaRepository<Post, Long> {

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE ((u.userId IN (SELECT f.userId FROM User u INNER JOIN u.followers f WHERE u.userId = :userId)) OR u.userId = :userId) AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findTimelinePosts(@Param("userId") Long userId);

	@Query(value="SELECT p.* FROM Post p WHERE p.user_id IN (SELECT f.follower_id FROM Followers f WHERE f.user_id = :userId) AND p.retweet_post_id NOT IN (SELECT p.post_id FROM Post p WHERE p.post_type = 'photo' AND p.user_id = :userId) OR p.user_id = :userId order by createdate desc;", nativeQuery = true)
	public List<Post> findTimelinePostBySQL(@Param("userId") Long userId);
	
	@Query("SELECT p FROM Post p WHERE TYPE(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findRandomPost();

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.uniqueName = :uniqueName AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findPostByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.userId = :userId AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findPostByUserId(@Param("userId") Long userId);

	@Query("SELECT p FROM Post p WHERE p.postId IN :postIdSet")
	public List<Post> findPostByPostIdSet(@Param("postIdSet") Set<Long> postIdSet);

	@Query("SELECT p FROM Post p WHERE p.postId = :postId")
	public List<Post> findPostByPostId(@Param("postId") Long postId);

	@Query("SELECT p FROM Post p WHERE TYPE(p) <> RetweetPost AND p.createdate >= :start AND p.createdate <= :end")
	public List<Post> findPostByDateRange(@Param("start") Date start, @Param("end") Date end);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE TYPE(p) = RetweetPost AND (p.post.postId = :postId AND p.user.userId = :userId)")
	public List<Post> findRetweetedPost(@Param("postId") Long postId, @Param("userId") Long userId);
}
