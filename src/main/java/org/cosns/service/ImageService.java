package org.cosns.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

import javax.imageio.ImageIO;

import org.cosns.dao.PostImageDAO;
import org.cosns.dao.ProfileImageDAO;
import org.cosns.repository.User;
import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.ProfileImage;
import org.cosns.util.ConstantsUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

@Service
public class ImageService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PostImageDAO postImageDAO;

	@Autowired
	ProfileImageDAO profileImageDAO;

	public void savePostImage(String storedPath, String targetFilename, String thumbnailFilename, Long fileSize, User user) {
		PostImage image = new PostImage();
		image.setStoredPath(storedPath);
		image.setFilename(targetFilename);
		image.setThumbnailFilename(thumbnailFilename);
		image.setSize(fileSize);
		image.setStatus(ConstantsUtil.IMAGE_PEND);

		postImageDAO.save(image);
	}

	public void saveProfileImage(String storedPath, String targetFileName, Long fileSize, User user) {
		ProfileImage image = new ProfileImage();
		image.setStoredPath(storedPath);
		image.setFilename(targetFileName);
		image.setThumbnailFilename(null);
		image.setSize(fileSize);
		image.setStatus(ConstantsUtil.IMAGE_PEND);
		image.setUser(user);

		profileImageDAO.save(image);
	}

	public File uploadImage(MultipartFile file, String targetPath) throws IllegalStateException, IOException, NullPointerException {

		File targetFile = new File(targetPath);

		file.transferTo(targetFile);

		logger.info("file : " + targetFile);

		return targetFile;
	}

	public void resizeImage(File fromFile, File outputFile, String ext, int size) throws IOException {
		logger.info("target file : " + fromFile);
		logger.info("Content Type : " + ext);
		logger.info("Size : " + size);

		// resize image
		BufferedImage in = ImageIO.read(fromFile);

		if (in.getWidth() > size || in.getHeight() > size) {
			ResampleOp resizeOp = new ResampleOp(DimensionConstrain.createMaxDimension(size, -1));

			resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
			resizeOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);

			BufferedImage scaledImage = resizeOp.filter(in, null);

			ImageIO.write(scaledImage, ext, outputFile);
			logger.info("Write file complete");
		}

	}

	public List<ProfileImage> findPendProfileImageByFilename(String file) {
		return profileImageDAO.findPendImageByFilename(file);
	}

	public List<ProfileImage> findActiveProfileImageByUserId(Long userId) {
		return profileImageDAO.findActiveProfileImageByUserId(userId);
	}

	public List<PostImage> findPendPostImageByFilename(String file) {
		return postImageDAO.findPendImageByFilename(file);
	}

	public List<PostImage> findActivePostImageByFilename(String file) {
		return postImageDAO.findActiveImageByFilename(file);
	}

	public void saveProfileImage(ProfileImage image) {
		profileImageDAO.save(image);
	}

	public void savePostImage(PostImage image) {
		postImageDAO.save(image);
	}

	public void disableAllProfileImageByUserId(Long userId) {
		profileImageDAO.disableAllProfileImageByUserId(userId);
	}

	public Set<ProfileImage> findActiveProfileImageByFilename(String file) {
		return profileImageDAO.findActiveImageByFilename(file);
	}

}
