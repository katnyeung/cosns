package org.cosns.dao;

import org.cosns.repository.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashTagDAO extends JpaRepository<HashTag, Long> {
}
