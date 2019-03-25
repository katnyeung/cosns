package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.Image;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageDAO extends JpaRepository<Image, Long> {

	@Query("FROM Image WHERE filename = :filename and status = '" + ConstantsUtil.IMAGE_PEND + "'")
	public Set<Image> findPendImageByFilename(@Param("filename") String filename);
}
