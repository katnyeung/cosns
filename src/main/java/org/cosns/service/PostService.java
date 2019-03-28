package org.cosns.service;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.cosns.dao.ImageDAO;
import org.cosns.dao.PostDAO;
import org.cosns.repository.Image;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.PostFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	ImageDAO imageDAO;

	@Autowired
	PostDAO postDAO;

	public void saveImage(String prefix, MultipartFile file, User user) {
		Image image = new Image();
		image.setFilename(prefix + file.getOriginalFilename());
		image.setSize(file.getSize());
		image.setStatus(ConstantsUtil.IMAGE_PEND);
		image.setUser(user);

		imageDAO.save(image);
	}

	public Post writePost(PostFormDTO postDTO, User user) {
		logger.info("Writing Post By User : " + user.getUserId());

		// create post
		Post post = new Post();
		post.setMessage(postDTO.getPostMessage());
		post.setStatus(ConstantsUtil.POST_ACTIVE);
		post.setUser(user);

		post = postDAO.save(post);

		int count = 1;
		for (String file : postDTO.getFileList()) {
			Set<Image> imageSet = imageDAO.findPendImageByFilename(file);
			for (Image image : imageSet) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setPost(post);
				image.setSeq(count++);
				imageDAO.save(image);
			}
		}
		
		return post;
	}

	public Set<Post> findRandomPosts() {
		return postDAO.findRandomPost();
	}

	public Set<Post> findTimelinePosts(User user) {
		return postDAO.findTimelinePosts(user.getUserId());
	}

	public Set<Post> getUserPosts(String uniqueName) {
		return postDAO.findPostByUniqueName(uniqueName);
	}

	public Set<Post> getUserPosts(Long userId) {
		return postDAO.findPostByUserId(userId);
	}

	public Optional<Post> getPost(Long postId) {
		return postDAO.findById(postId);
	}

}
