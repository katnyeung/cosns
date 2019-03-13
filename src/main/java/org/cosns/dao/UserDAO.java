package org.cosns.dao;

import java.util.List;

import org.cosns.repository.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDAO extends JpaRepository<User, Long> {

	@Query("FROM User WHERE email = :email")
	public List<User> findByEmail(@Param("email") String email);
}
