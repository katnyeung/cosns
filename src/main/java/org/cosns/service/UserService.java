package org.cosns.service;

import java.util.List;

import org.cosns.dao.UserDAO;
import org.cosns.repository.User;
import org.cosns.web.DTO.UserFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	@Autowired
	UserDAO userDAO;

	public void registerUser(UserFormDTO userDTO) {

	}

	public User login(UserFormDTO userDTO) {
		List<User> userList = userDAO.findByEmail(userDTO.getEmail());
		User loggedUser = null;

		for (User user : userList) {
			if (userDTO.getPassword().equals(user.getPassword())) {
				loggedUser = user;
			}
		}

		return loggedUser;
	}
}
