package org.cosns.dao;

import java.util.List;

import org.cosns.repository.extend.EventImage;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventImageDAO extends JpaRepository<EventImage, Long> {

	@Query("SELECT i FROM EventImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_PEND + "'")
	public List<EventImage> findPendEventImageByFilename(@Param("filename") String filename);

	@Query("SELECT i FROM EventImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	public List<EventImage> findActiveEventImageByFilename(@Param("filename") String filename);
}
