package org.cosns.dao;

import java.util.List;
import java.util.Set;

import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDAO extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u WHERE u.uniqueName = :uniqueName and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT u FROM User u WHERE u.email = :email and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByEmail(@Param("email") String email);

	@Query("SELECT u FROM User u WHERE u.userId = :userId and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserById(@Param("userId") Long userId);

	@Query("SELECT u FROM User u WHERE u.userId IN :userIdList and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByIdList(@Param("userIdList") List<Long> userIdList);

	@Query("SELECT u FROM User u WHERE u.uniqueName = :uniqueName and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findAllUserByUniqueName(@Param("uniqueName") String uniqueName);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "DELETE FROM followers WHERE user_id = :userId AND follower_id = :followerId", nativeQuery = true)
	public void deleteFollower(@Param("userId") Long userId, @Param("followerId") Long followerId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "DELETE FROM followed_by WHERE user_id = :userId AND follower_id = :followerId", nativeQuery = true)
	public void deleteFollowedBy(@Param("userId") Long userId, @Param("followerId") Long followerId);

}
