package org.cosns.dao;

import org.cosns.repository.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostDAO extends JpaRepository<Post, Long> {

}
