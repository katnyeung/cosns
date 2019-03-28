package org.cosns.service;

import java.util.Set;
import java.util.logging.Logger;

import org.cosns.dao.FriendRequestDAO;
import org.cosns.dao.UserDAO;
import org.cosns.repository.FriendRequest;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.UserFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	UserDAO userDAO;

	@Autowired
	FriendRequestDAO friendRequestDAO;

	@Transactional
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

	public User getUserById(Long userId) {
		Set<User> userSet = userDAO.findActiveUserById(userId);

		if (userSet.iterator().hasNext()) {
			return userSet.iterator().next();
		} else {
			return null;
		}
	}

	public User getUserByEmail(String email) {
		Set<User> userSet = userDAO.findActiveUserByEmail(email);

		if (userSet.iterator().hasNext()) {
			return userSet.iterator().next();
		} else {
			return null;
		}
	}

	public User getUserByUniqueName(String uniqueName) {
		Set<User> userSet = userDAO.findActiveUserByEmail(uniqueName);

		if (userSet.iterator().hasNext()) {
			return userSet.iterator().next();
		} else {
			return null;
		}
	}

	public FriendRequest findFriendRequest(User fromUser, Long targetUserId) {
		Set<FriendRequest> frSet = friendRequestDAO.findRequest(fromUser.getUserId(), targetUserId);

		if (frSet.iterator().hasNext()) {
			return frSet.iterator().next();
		} else {
			return null;
		}
	}

	public User addFriend(User fromUser, Long targetUserId) {
		logger.info(" adding friend form " + fromUser.getUserId() + " to targetUser : " + targetUserId);
		Set<User> targetUserSet = userDAO.findActiveUserById(targetUserId);

		if (targetUserSet.iterator().hasNext()) {
			User targetUser = targetUserSet.iterator().next();

			logger.info("found target user: " + targetUser.getEmail());
			if (targetUser.getUserId() != fromUser.getUserId()) {
				FriendRequest fr = new FriendRequest();
				fr.setFromUser(fromUser);
				fr.setTargetUser(targetUser);

				friendRequestDAO.save(fr);

				return targetUser;
			} else {
				return null;
			}

		} else {
			return null;
		}

	}
}
