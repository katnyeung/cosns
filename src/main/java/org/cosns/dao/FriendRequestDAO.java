package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRequestDAO extends JpaRepository<FriendRequest, Long> {

	@Query("From FriendRequest fr WHERE fr.fromUser.userId = :userId and fr.targetUser.userId = :targetUserId")
	public Set<FriendRequest> findRequest(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
