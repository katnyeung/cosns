package org.cosns.dao;

import java.util.List;
import java.util.Set;

import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDAO extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u WHERE u.uniqueName = :uniqueName and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT u FROM User u WHERE u.email = :email and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByEmail(@Param("email") String email);

	@Query("SELECT u FROM User u WHERE u.userId = :userId and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserById(@Param("userId") Long userId);

	// @Query("SELECT u FROM User u WHERE u.userId IN :userIdList and u.status = '"
	// + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	@Query(value = "SELECT u.* FROM user u INNER JOIN (SELECT sum(p.total_view_count) as total, p.user_id as user_id FROM post p GROUP by p.user_id) sp ON sp.user_id = u.user_id WHERE u.user_id IN :userIdList AND u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "' ORDER BY sp.total DESC", nativeQuery = true)
	public List<User> findActiveUserByIdListOrderByView(@Param("userIdList") Set<Long> userIdList);

	@Query(value = "SELECT u.* FROM user u WHERE u.user_id IN :userIdList AND u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "' ORDER BY u.lastupdatedate DESC", nativeQuery = true)
	public List<User> findActiveUserByIdListOrderByDate(@Param("userIdList") Set<Long> userIdList);

	@Query("SELECT u FROM User u WHERE u.uniqueName = :uniqueName and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findAllUserByUniqueName(@Param("uniqueName") String uniqueName);

	@Query("SELECT u FROM User u WHERE u.fbId = :fbId and u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public User findActiveUserByFbId(String fbId);

	@Query("SELECT u FROM User u WHERE u.status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUsers();

}
