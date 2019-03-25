package org.cosns.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.service.PostService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.DTO.PostFormDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.UploadImageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	PostService postService;

	@Value("${cosns.image.uploadFolder}")
	String uploadFolder;

	@Value("${cosns.image.uploadPattern}")
	String uploadPattern;

	@GetMapping(path = "/getPosts")
	public Set<Post> getPosts(HttpSession session) {

		User user = (User) session.getAttribute("user");
		Set<Post> postSet = null;

		if (user != null) {
			postSet = user.getPosts();
		}
		return postSet;
	}

	@GetMapping(path = "/getPost/{postId}")
	public Post getPost(@PathVariable("postId") Long postId, HttpSession session) {
		Optional<Post> post = postService.findById(postId);

		if (post.isPresent()) {
			return post.get();
		} else {
			return null;
		}
	}

	@GetMapping(path = "/getRandomPost")
	public Set<Post> getPost(HttpSession session) {
		Set<Post> post = postService.findRandomPost();

		return post;
	}

	@PostMapping(value = "/uploadImage", consumes = { "multipart/form-data" })
	public DefaultResult uploadImageContent(ImageUploadDTO imageInfo, HttpSession session) throws IOException {
		UploadImageResult result = new UploadImageResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(uploadPattern);
			String prefix = sdf.format(Calendar.getInstance().getTime()) + "_";

			logger.info("inside upload image");

			MultipartFile file = imageInfo.getFile();

			File uploadedFile = new File(uploadFolder + prefix + file.getOriginalFilename());
			file.transferTo(uploadedFile);

			logger.info("file : " + uploadedFile);

			postService.saveImage(prefix, file, user);

			result.setFilePath(prefix + file.getOriginalFilename());
			result.setStatus(ConstantsUtil.RESULT_SUCCESS);

		} else {
			result.setRemarks("Login Required");
			result.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return result;
	}

	@PostMapping(path = "/writePost")
	public DefaultResult writePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			postService.writePost(postDTO, user);
			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks("Login Required");
		}

		return dr;
	}

}
