package org.cosns.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.cosns.repository.Post;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PostDAO extends PagingAndSortingRepository<Post, Long> {

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE (u.userId IN (SELECT f.userId FROM User u INNER JOIN u.followers f WHERE u.userId = :userId) OR u.userId = :userId OR p.postId IN (SELECT rp.post.postId FROM Post rp WHERE TYPE(rp) = RetweetPost AND rp.user.userId = :userId OR rp.user.userId IN (SELECT f.userId FROM User u INNER JOIN u.followers f WHERE u.userId = :userId))) AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findTimelinePosts(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE TYPE(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findLatestPosts(Pageable pageable);

	@Query("SELECT p FROM Post p WHERE Type(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' AND p.postId IN ( SELECT pr.post.postId FROM PostReaction pr WHERE pr.month = :month AND pr.year = :year GROUP BY pr.month, pr.post.postId ORDER BY SUM(pr.year) DESC )")
	public List<Post> findTopMonthPosts(@Param("month") int month, @Param("year") int year, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE Type(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' AND p.postId IN ( SELECT pr.post.postId FROM PostReaction pr WHERE pr.year = :year GROUP BY pr.year, pr.post.postId ORDER BY SUM(pr.year) DESC )")
	public List<Post> findTopYearPosts(@Param("year") int year, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE Type(p) = PhotoPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.totalViewCount DESC")
	public List<Post> findTopPosts(PageRequest of);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.uniqueName = :uniqueName AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findPostByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE u.userId = :userId AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' ORDER BY p.createdate DESC")
	public List<Post> findPostByUserId(@Param("userId") Long userId);

	@Query("SELECT p FROM Post p WHERE TYPE(p) = PhotoPost AND p.postId IN :postIdSet AND p.status = '" + ConstantsUtil.POST_ACTIVE + "'")
	public List<Post> findPostByPostIdSet(@Param("postIdSet") Set<Long> postIdSet, Sort sort);

	@Query("SELECT p FROM Post p WHERE TYPE(p) = PhotoPost AND p.postId IN :postIdSet AND p.status = '" + ConstantsUtil.POST_ACTIVE + "'")
	public List<Post> findPostByPostIdSet(@Param("postIdSet") Set<Long> postIdSet);

	@Query("SELECT p FROM Post p WHERE p.postId = :postId")
	public List<Post> findPostByPostId(@Param("postId") Long postId);

	@Query("SELECT p FROM Post p WHERE TYPE(p) <> RetweetPost AND p.status = '" + ConstantsUtil.POST_ACTIVE + "' AND p.createdate >= :start AND p.createdate <= :end")
	public List<Post> findPostByDateRange(@Param("start") Date start, @Param("end") Date end);

	@Query("SELECT p FROM Post p INNER JOIN p.user u WHERE TYPE(p) = RetweetPost AND (p.post.postId = :postId AND p.user.userId = :userId)")
	public List<Post> findRetweetedPost(@Param("postId") Long postId, @Param("userId") Long userId);

}
