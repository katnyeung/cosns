package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDAO extends JpaRepository<User, Long> {
	@Query("FROM User WHERE uniqueName = :uniqueName and status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByUniqueName(@Param("uniqueName") String uniqueName);
	
	@Query("FROM User WHERE email = :email and status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserByEmail(@Param("email") String email);
	
	@Query("FROM User WHERE userId = :userId and status = '" + ConstantsUtil.USER_STATUS_ACTIVE + "'")
	public Set<User> findActiveUserById(@Param("userId") Long userId);
}
