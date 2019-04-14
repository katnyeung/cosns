package org.cosns.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.cosns.repository.Post;
import org.cosns.repository.PostReaction;
import org.cosns.repository.User;
import org.cosns.service.HashTagService;
import org.cosns.service.ImageService;
import org.cosns.service.PostService;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.ImageUploadDTO;
import org.cosns.web.DTO.PostFormDTO;
import org.cosns.web.DTO.PostReactionDTO;
import org.cosns.web.DTO.SearchPostDTO;
import org.cosns.web.result.DefaultResult;
import org.cosns.web.result.PostListResult;
import org.cosns.web.result.PostReactionResult;
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

	@Autowired
	ImageService imageService;

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
	public DefaultResult getPost(@PathVariable("postId") Long postId, HttpSession session) {
		Optional<Post> post = postService.getPost(postId);

		PostListResult plr = new PostListResult();

		if (post.isPresent()) {
			plr.setPostList(Stream.of(post.get()).collect(Collectors.toSet()));
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setRemarks("Post not found");
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		}
		return plr;
	}

	@PostMapping(path = "/searchPosts")
	public DefaultResult getPost(@RequestBody SearchPostDTO searchPost, HttpSession session) {
		PostListResult plr = new PostListResult();

		Set<Post> postList = postService.searchPosts(searchPost.getKeyword());

		plr.setPostList(postList);

		return plr;
	}

	@GetMapping(path = "/getRandomPosts")
	public DefaultResult getPost() {
		PostListResult plr = new PostListResult();

		Set<Post> postList = postService.findRandomPosts();

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

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
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return plr;
	}

	@PostMapping(value = "/uploadImage", consumes = { "multipart/form-data" })
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

				imageService.savePostImage(fileName, fromFile.getSize(), user);

				result.setFilePath(fileName);
				result.setStatus(ConstantsUtil.RESULT_SUCCESS);

			} catch (Exception ex) {
				ex.printStackTrace();

				result.setStatus(ConstantsUtil.RESULT_ERROR);
				result.setRemarks(ex.getLocalizedMessage());

			}

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

			hashTagService.saveHashToRedis(post, hashTagSet, ConstantsUtil.PHOTO_POST_PREFIX);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return dr;
	}

	@PostMapping(path = "/likePost")
	public DefaultResult likePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			PostReaction postReaction = postService.likePost(postReactionDTO.getPostId(), user);

			prr.setPostReaction(postReaction);
			prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return prr;
	}

	@PostMapping(path = "/retweetPost")
	public DefaultResult retweet(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.retweetPost(postReactionDTO.getPostId(), user);
			if (post != null) {
				dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				dr.setStatus(ConstantsUtil.RESULT_ERROR);
				dr.setRemarks("Retweet Failed");
			}

		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN);
		}

		return dr;
	}

}
