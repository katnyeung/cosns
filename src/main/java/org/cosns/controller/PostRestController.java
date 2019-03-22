package org.cosns.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.cosns.dao.PostDAO;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.web.DTO.ImageUploadInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	PostDAO postDAO;

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
		Optional<Post> post = postDAO.findById(postId);

		if (post.isPresent()) {
			return post.get();
		} else {
			return null;
		}
	}

	@PostMapping(value = "/uploadImage", consumes = { "multipart/form-data" })
	public String uploadImageContent(ImageUploadInfoDTO imageInfo) throws IOException {
		logger.info("inside upload image");

		MultipartFile file = imageInfo.getFile();
		
		File uploadedFile = new File("d:/ChungYeung/" + file.getOriginalFilename());
		file.transferTo(uploadedFile);
		logger.info("file : " + imageInfo);

		return "{status:\"success\"}";
	}

	@GetMapping(path = "/writePost/{postId}")
	public String writePost(@PathVariable("postId") String postId) {

		return "index";
	}

}
