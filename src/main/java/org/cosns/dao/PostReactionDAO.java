package org.cosns.dao;

import java.util.List;
import java.util.Optional;

import org.cosns.repository.PostReaction;
import org.cosns.repository.extend.DateCountReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostReactionDAO extends JpaRepository<PostReaction, Long> {

	@Query("SELECT pr FROM PostReaction pr INNER JOIN pr.user u INNER JOIN pr.post p WHERE u.userId = :userId AND p.postId = :postId")
	Optional<PostReaction> findByPostIdUserId(@Param("postId") Long postId, @Param("userId") Long userId);

	@Query("SELECT pr FROM PostReaction pr INNER JOIN pr.post p WHERE p.postId = :postId")
	List<PostReaction> findByPostId(@Param("postId") Long postId);

	@Query("SELECT pr FROM PostReaction pr WHERE TYPE(pr) = DateCountReaction AND day = :day AND month = :month AND year = :year AND pr.post.postId = :postId")
	Optional<DateCountReaction> findByDateAndPostId(@Param("day") int day, @Param("month") int month, @Param("year") int year, @Param("postId") Long postId);

}
