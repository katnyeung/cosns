package org.cosns.dao;

import java.util.Optional;

import org.cosns.repository.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostReactionDAO extends JpaRepository<PostReaction, Long> {

	@Query("SELECT pr FROM PostReaction pr INNER JOIN pr.user u INNER JOIN pr.post p WHERE u.userId = :userId AND p.postId = :postId")
	Optional<PostReaction> findByPostIdUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
