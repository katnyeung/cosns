package org.cosns.dao;

import java.util.List;
import java.util.Set;

import org.cosns.repository.image.ProfileImage;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileImageDAO extends JpaRepository<ProfileImage, Long> {

	@Query("SELECT i FROM ProfileImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_PEND + "'")
	public List<ProfileImage> findPendProfileImageByFilename(@Param("filename") String filename);

	@Query("SELECT i FROM ProfileImage i WHERE i.user.userId = :userId and i.status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	public List<ProfileImage> findActiveProfileImageByUserId(@Param("userId") Long userId);

	@Modifying
	@Query("UPDATE ProfileImage i SET i.status = '" + ConstantsUtil.IMAGE_DELETED + "' WHERE i.user.userId = :userId AND i.status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	public void disableAllProfileImageByUserId(@Param("userId") Long userId);
	
	@Query("SELECT i FROM ProfileImage i WHERE i.filename = :filename and i.status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	public Set<ProfileImage> findActiveImageByFilename(@Param("filename") String filename);
}
