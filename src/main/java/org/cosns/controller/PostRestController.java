package org.cosns.controller;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.cosns.dao.PostDAO;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostRestController {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	PostDAO postDAO;

	@RequestMapping(path = "/getPosts", method = RequestMethod.GET)
	public Set<Post> getPosts(HttpSession session) {

		User user = (User) session.getAttribute("user");
		Set<Post> postSet = null;

		if (user != null) {
			postSet = user.getPosts();
		}
		return postSet;
	}

	@RequestMapping(path = "/getPost/{postId}", method = RequestMethod.GET)
	public Post getPost(@PathVariable("postId") Long postId, HttpSession session) {
		Optional<Post> post = postDAO.findById(postId);

		if (post.isPresent()) {
			return post.get();
		} else {
			return null;
		}
	}

	@RequestMapping(path = "/writePost/{postId}", method = RequestMethod.GET)
	public String writePost(@PathVariable("postId") String postId) {

		return "index";
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public void uploadImage(@RequestParam String dto, @RequestParam(required = false) MultipartFile file,
			Authentication authentication) {

	}
}
