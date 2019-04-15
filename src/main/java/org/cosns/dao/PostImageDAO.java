package org.cosns.dao;

import java.util.Set;

import org.cosns.repository.extend.PostImage;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostImageDAO extends JpaRepository<PostImage, Long> {

	@Query("SELECT i FROM PostImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_PEND + "'")
	public Set<PostImage> findPendImageByFilename(@Param("filename") String filename);

	@Query("SELECT i FROM PostImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	public Set<PostImage> findActiveImageByFilename(@Param("filename") String file);
}
