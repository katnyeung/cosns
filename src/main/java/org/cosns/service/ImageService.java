package org.cosns.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.cosns.dao.PostImageDAO;
import org.cosns.dao.ProfileImageDAO;
import org.cosns.repository.User;
import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.ProfileImage;
import org.cosns.util.ConstantsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

@Service
public class ImageService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	PostImageDAO postImageDAO;

	@Autowired
	ProfileImageDAO profileImageDAO;

	public void savePostImage(String storedPath, String targetFileName, Long fileSize, User user) {
		PostImage image = new PostImage();
		image.setStoredPath(storedPath);
		image.setFilename(targetFileName);
		image.setSize(fileSize);
		image.setStatus(ConstantsUtil.IMAGE_PEND);

		postImageDAO.save(image);
	}

	public void saveProfileImage(String storedPath, String targetFileName, Long fileSize, User user) {
		ProfileImage image = new ProfileImage();
		image.setStoredPath(storedPath);
		image.setFilename(targetFileName);
		image.setSize(fileSize);
		image.setStatus(ConstantsUtil.IMAGE_PEND);
		image.setUser(user);

		profileImageDAO.save(image);
	}

	public File uploadImage(MultipartFile file, String targetPath, int size) throws IllegalStateException, IOException, NullPointerException {

		File targetFile = new File(targetPath);

		file.transferTo(targetFile);

		logger.info("file : " + targetFile);

		// resize image
		BufferedImage in = ImageIO.read(targetFile);

		BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

		ResampleOp resizeOp = new ResampleOp(DimensionConstrain.createMaxDimension(size, -1));
		resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
		BufferedImage scaledImage = resizeOp.filter(newImage, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(scaledImage, file.getContentType(), baos);

		return targetFile;
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
