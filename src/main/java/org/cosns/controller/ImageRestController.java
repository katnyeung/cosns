package org.cosns.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpSession;

import org.cosns.repository.extend.PostImage;
import org.cosns.repository.extend.ProfileImage;
import org.cosns.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ImageRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	ImageService imageService;

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
}
