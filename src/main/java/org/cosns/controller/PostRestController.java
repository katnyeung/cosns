package org.cosns.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

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
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}
		return plr;
	}

	@GetMapping(path = "/getPost/{postId}")
	public DefaultResult getPost(@PathVariable("postId") Long postId, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.getPost(postId, user.getUserId());
		} else {
			postList = postService.getPost(postId);
		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		return plr;
	}

	@PostMapping(path = "/searchPosts")
	public DefaultResult getPost(@RequestBody SearchPostDTO searchPost, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.searchPosts(searchPost.getKeyword().toLowerCase(), ConstantsUtil.REDIS_POST_TAG_TYPE_ALL, user);
		} else {
			postList = postService.searchPosts(searchPost.getKeyword().toLowerCase(), ConstantsUtil.REDIS_POST_TAG_TYPE_ALL);
		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return plr;
	}

	@GetMapping(path = "/getLatestPost")
	public DefaultResult getPost(HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.findLatestPosts(user.getUserId());
		} else {
			postList = postService.findLatestPosts();
		}

		plr.setPostList(postList);
		plr.setStatus(ConstantsUtil.RESULT_SUCCESS);

		return plr;
	}

	@GetMapping(path = "/getTimelinePosts/{startFrom}")
	public DefaultResult getTimelinePosts(@PathVariable("startFrom") int startFrom, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");

		if (user != null) {

			List<Post> postList = postService.findTimelinePosts(user.getUserId(), startFrom);
			plr.setPostList(postList);
			plr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			plr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
			plr.setStatus(ConstantsUtil.RESULT_ERROR);
		}

		return plr;
	}

	@GetMapping(path = "/getUserPosts/{userId}")
	public DefaultResult getUserPost(@PathVariable("userId") Long userId, HttpSession session) {
		PostListResult plr = new PostListResult();

		User user = (User) session.getAttribute("user");
		List<Post> postList = null;

		if (user != null) {
			postList = postService.getUserPosts(userId, user.getUserId());

		} else {
			postList = postService.getUserPosts(userId);
		}

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

	@PostMapping(path = "/writePost")
	@Transactional
	public DefaultResult writePost(@RequestBody PostFormDTO postDTO, HttpSession session) {
		DefaultResult dr = new DefaultResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.writePhotoPost(postDTO, user);

			Set<String> hashTagSet = hashTagService.parseHash(post);

			logger.info("writing hash : " + hashTagSet);

			hashTagService.saveHash(post, hashTagSet);

			hashTagService.saveHashToRedis(post, hashTagSet, ConstantsUtil.REDIS_POST_TAG_GROUP, ConstantsUtil.REDIS_POST_TAG_TYPE_PHOTO);

			dr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			dr.setStatus(ConstantsUtil.RESULT_ERROR);
			dr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return dr;
	}

	@PostMapping(path = "/likePost")
	public DefaultResult likePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			PostReaction postReaction = postService.likePost(postReactionDTO.getPostId(), user);

			if (postReaction.getStatus().equals(ConstantsUtil.POST_REACTION_ACTIVE)) {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
			} else {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_DECREASE);
			}

			prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}

	@PostMapping(path = "/retweetPost")
	public DefaultResult retweet(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.retweetPost(postReactionDTO.getPostId(), user);
			if (post != null) {
				prr.setType(ConstantsUtil.POST_REACTION_TYPE_INCREASE);
				prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				prr.setStatus(ConstantsUtil.RESULT_ERROR);
				prr.setRemarks("Retweet Failed");
			}

		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}

	@PostMapping(path = "/removePost")
	public DefaultResult removePost(@RequestBody PostReactionDTO postReactionDTO, HttpSession session) {
		PostReactionResult prr = new PostReactionResult();
		User user = (User) session.getAttribute("user");

		if (user != null) {
			Post post = postService.removePost(postReactionDTO.getPostId(), user);
			
			if (post != null) {
				prr.setType(ConstantsUtil.POST_REACTION_CANCEL);
				prr.setStatus(ConstantsUtil.RESULT_SUCCESS);
			} else {
				prr.setStatus(ConstantsUtil.RESULT_ERROR);
				prr.setRemarks("Remove Failed");
			}

		} else {
			prr.setStatus(ConstantsUtil.RESULT_ERROR);
			prr.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		}

		return prr;
	}
}
