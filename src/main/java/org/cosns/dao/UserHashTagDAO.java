package org.cosns.dao;

import org.cosns.repository.extend.UserHashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserHashTagDAO extends JpaRepository<UserHashTag, Long> {

	@Modifying
	@Query("DELETE UserHashTag uht WHERE uht.user.userId = :userId")
	void deleteHashTagByUserId(@Param("userId") Long userId);

}
