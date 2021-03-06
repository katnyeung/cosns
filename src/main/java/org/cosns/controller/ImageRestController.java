package org.cosns.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.cosns.auth.Auth;
import org.cosns.repository.User;
import org.cosns.service.ImageService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UploadImageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ImageService imageService;

	@Value("${cosns.image.uploadFolder}")
	String uploadFolder;

	@Value("${cosns.image.uploadPattern}")
	String uploadPattern;

	@GetMapping(path = "image/{imageFile}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getDirectImage(@PathVariable("imageFile") String imageFile, HttpSession session, Model model) throws IOException {

		File img = new File(uploadFolder + imageFile);

		return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));

	}

	@Auth
	@PostMapping(value = "post/uploadImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadImageContent(ImageUploadDTO imageInfo, HttpSession session) throws IOException, NullPointerException, SizeLimitExceededException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		try {
			String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "");

			logger.info("user : " + user.getUserId() + " upload image");

			MultipartFile fromFile = imageInfo.getFile();

			String ext = FilenameUtils.getExtension(fromFile.getOriginalFilename());

			String filename = uuidPrefix + "." + ext;
			String thumbnailFilename = uuidPrefix + ConstantsUtil.IMAGE_THUMBNAIL_POSTFIX + "." + ext;

			String targetFullPath = uploadFolder + filename;

			File targetFile = imageService.uploadImage(fromFile, targetFullPath);
			File thumbnailFile = new File(uploadFolder + thumbnailFilename);

			imageService.resizeImage(targetFile, targetFile, ext, 2048);
			imageService.resizeImage(targetFile, thumbnailFile, ext, 512);

			imageService.savePostImage(uploadFolder, filename, thumbnailFilename, targetFile.getTotalSpace(), user);

			result.setFilePath(filename);
			result.setStatus(ConstantsUtil.RESULT_SUCCESS);

		} catch (Exception ex) {
			ex.printStackTrace();

			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ex.getLocalizedMessage());

		}

		return result;
	}

	@Auth
	@PostMapping(value = "user/uploadProfileImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadProfileImage(ImageUploadDTO imageInfo, HttpSession session) throws IOException, NullPointerException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		try {

			String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "");

			logger.info("user : " + user.getUserId() + " upload profile image");


			MultipartFile fromFile = imageInfo.getFile();

			String ext = FilenameUtils.getExtension(fromFile.getOriginalFilename());

			String fileName = uuidPrefix + "." + ext;

			String targetPath = uploadFolder + fileName;

			File targetFile = imageService.uploadImage(fromFile, targetPath);

			imageService.resizeImage(targetFile, targetFile, ext, 200);

			imageService.saveProfileImage(uploadFolder, fileName, fromFile.getSize(), user);

			result.setFilePath(fileName);
			result.setStatus(ConstantsUtil.RESULT_SUCCESS);

		} catch (Exception ex) {
			ex.printStackTrace();

			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ex.getLocalizedMessage());

		}

		return result;
	}

	@Auth
	@PostMapping(value = "event/uploadImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadEventImageContent(ImageUploadDTO imageInfo, HttpSession session) throws IOException, NullPointerException, SizeLimitExceededException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		try {
			String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "");

			logger.info("user : " + user.getUserId() + " upload event image");

			MultipartFile fromFile = imageInfo.getFile();

			String ext = FilenameUtils.getExtension(fromFile.getOriginalFilename());

			String filename = uuidPrefix + "." + ext;
			String thumbnailFilename = uuidPrefix + ConstantsUtil.IMAGE_THUMBNAIL_POSTFIX + "." + ext;

			String targetFullPath = uploadFolder + filename;

			File targetFile = imageService.uploadImage(fromFile, targetFullPath);
			File thumbnailFile = new File(uploadFolder + thumbnailFilename);

			imageService.resizeImage(targetFile, targetFile, ext, 2048);
			imageService.resizeImage(targetFile, thumbnailFile, ext, 512);

			imageService.saveEventImage(uploadFolder, filename, thumbnailFilename, targetFile.getTotalSpace(), user);

			result.setFilePath(filename);
			result.setStatus(ConstantsUtil.RESULT_SUCCESS);

		} catch (Exception ex) {
			ex.printStackTrace();

			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ex.getLocalizedMessage());

		}

		return result;
	}
}
