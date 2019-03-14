package org.cosns.service;

import java.util.Set;

import org.cosns.dao.UserDAO;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.UserFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	@Autowired
	UserDAO userDAO;

	public User registerUser(UserFormDTO userDTO) {
		User registeredUser = null;

		Set<User> userSet = userDAO.findActiveUserByEmail(userDTO.getEmail());
		if (userSet.size() == 0) {
			// create user

			User user = new User();
			user.setEmail(userDTO.getEmail());
			user.setPassword(userDTO.getPassword());
			user.setStatus(ConstantsUtil.USER_STATUS_ACTIVE);

			registeredUser = userDAO.save(user);
		}

		return registeredUser;
	}

	public User verifyUser(UserFormDTO userDTO) {
		Set<User> userList = userDAO.findActiveUserByEmail(userDTO.getEmail());
		User loggedUser = null;

		for (User user : userList) {
			if (userDTO.getPassword().equals(user.getPassword())) {
				loggedUser = user;
			}
		}

		return loggedUser;
	}

	public Set<Post> getPosts(User user) {
		return user.getPosts();
	}
}
