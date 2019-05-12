package org.cosns.dao;

import java.util.List;

import org.cosns.repository.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HashTagDAO extends JpaRepository<HashTag, Long> {

	@Modifying
	@Query("UPDATE HashTag ht SET totalCount = totalCount + 1 WHERE hashTag IN :keyContentList")
	void updateHitRateByKeys(@Param("keyContentList") List<String> keyContentList);

}
