package org.cosns.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cosns.repository.hashtag.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HashTagDAO extends JpaRepository<HashTag, Long> {

	@Modifying
	@Query("UPDATE HashTag ht SET ht.totalCount = ht.totalCount + 1 WHERE ht.hashTag IN :keyContentList")
	void updateHitRateByKeys(@Param("keyContentList") List<String> keyContentList);

	@Query(value = "SELECT hash_tag,sum(total_count) as total_count FROM hash_tag WHERE post_id IN :postIdList GROUP BY hash_tag ORDER BY sum(total_count) DESC", nativeQuery = true)
	List<Map<String, String>> getTopRelatedHashTag(@Param("postIdList") Set<String> postIdList);

	@Modifying
	@Query("DELETE HashTag ht WHERE ht.hashId IN :hashTagIdList")
	void deleteByIdList(@Param("hashTagIdList") List<Long> hashTagIdList);

}
