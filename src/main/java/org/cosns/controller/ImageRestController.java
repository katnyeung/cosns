package org.cosns.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.cosns.repository.User;
import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.ProfileImage;
import org.cosns.service.ImageService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UploadImageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class ImageRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	ImageService imageService;

	@Value("${cosns.image.uploadFolder}")
	String uploadFolder;

	@Value("${cosns.image.uploadPattern}")
	String uploadPattern;

	@GetMapping(path = "images/{imageFile}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getPostImage(@PathVariable("imageFile") String imageFile, HttpSession session, Model model) throws IOException {

		List<PostImage> postImageSet = imageService.findActivePostImageByFilename(imageFile);
		if (postImageSet.iterator().hasNext()) {
			PostImage postImage = postImageSet.iterator().next();

			File img = new File(postImage.getStoredPath() + postImage.getFilename());
			return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));

		} else {
			return null;
		}

	}

	@GetMapping(path = "pimages/{imageFile}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getProfileImage(@PathVariable("imageFile") String imageFile, HttpSession session, Model model) throws IOException {

		Set<ProfileImage> postImageSet = imageService.findActiveProfileImageByFilename(imageFile);
		if (postImageSet.iterator().hasNext()) {
			ProfileImage postImage = postImageSet.iterator().next();

			File img = new File(postImage.getStoredPath() + postImage.getFilename());
			return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));

		} else {
			return null;
		}

	}

	@PostMapping(value = "images/uploadImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadImageContent(ImageUploadDTO imageInfo, HttpSession session) throws IOException, NullPointerException, SizeLimitExceededException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			try {
				String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "");

				logger.info("inside upload image");

				MultipartFile fromFile = imageInfo.getFile();

				String fileName = uuidPrefix + "." + FilenameUtils.getExtension(fromFile.getOriginalFilename());

				String targetPath = uploadFolder + fileName;

				logger.info("targetPath : " + targetPath);

				imageService.uploadImage(fromFile, targetPath, 2048);

				imageService.savePostImage(uploadFolder, fileName, fromFile.getSize(), user);

				result.setFilePath(fileName);
				result.setStatus(ConstantsUtil.RESULT_SUCCESS);

			} catch (Exception ex) {
				ex.printStackTrace();

				result.setStatus(ConstantsUtil.RESULT_ERROR);
				result.setRemarks(ex.getLocalizedMessage());

			}

		} else {
			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return result;
	}

}
