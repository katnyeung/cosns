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
import org.cosns.service.HashTagService;
import org.cosns.service.PostService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.DTO.PostFormDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.PostListResult;
import org.cosns.web.result.UploadImageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
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

	@Autowired
	HashTagService hashTagService;

	@Value("${cosns.image.uploadFolder}")
	String uploadFolder;

	@Value("${cosns.image.uploadPattern}")
	String uploadPattern;

	@GetMapping(path = "/getPosts")
	public DefaultResult getPosts(HttpSession session) {

		User user = (User) session.getAttribute("user");

		PostListResult plr = new PostListResult();

		if (user != null) {
			plr.setPostList(user.getPosts());
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setStatus(ConstantsUtil.RESULT_ERROR);
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}
		return plr;
	}

	@GetMapping(path = "/getPost/{postId}")
	public Post getPost(@PathVariable("postId") Long postId, HttpSession session) {
		Optional<Post> post = postService.getPost(postId);

		if (post.isPresent()) {
			return post.get();
		} else {
			return null;
		}
	}

	@GetMapping(path = "/getRandomPosts")
	public DefaultResult getPost() {
		PostListResult plr = new PostListResult();

		Set<Post> postList = postService.findRandomPosts();

		plr.setPostList(postList);

		return plr;
	}

	@GetMapping(path = "/getTimelinePosts")
	public DefaultResult getTimelinePosts(HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			Set<Post> postList = postService.findTimelinePosts(user);
			plr.setPostList(postList);
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
			plr.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return plr;
	}

	@GetMapping(path = "/getUserPosts/{userId}")
	public DefaultResult getUserPost(@PathVariable("userId") Long userId) {
		PostListResult plr = new PostListResult();

		Set<Post> postList = postService.getUserPosts(userId);

		plr.setPostList(postList);

		return plr;
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
			result.setStatus(ConstantsUtil.RESULT_ERROR);
			result.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return result;
	}

	@PostMapping(path = "/writePost")
	public DefaultResult writePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.writePost(postDTO, user);

			Set<String> hashTagSet = hashTagService.parseHash(post);
			
			logger.info("writing hash : " + hashTagSet);
			
			hashTagService.saveHash(post, hashTagSet);

			hashTagService.saveHashToRedis(post, hashTagSet);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return dr;
	}

}
