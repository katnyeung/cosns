package org.cosns.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

import org.cosns.dao.FriendRequestDAO;
import org.cosns.dao.UserDAO;
import org.cosns.repository.FriendRequest;
import org.cosns.repository.Post;
import org.cosns.repository.User;
import org.cosns.repository.extend.ProfileImage;
import org.cosns.util.ConstantsUtil;
import org.cosns.web.DTO.UserFormDTO;
import org.cosns.web.DTO.UserSettingDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ImageService imageService;

	@Autowired
	UserDAO userDAO;

	@Autowired
	FriendRequestDAO friendRequestDAO;

	@Autowired
	RedisService redisService;

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

	public List<Post> getPosts(User user) {
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
		Set<User> userSet = userDAO.findAllUserByUniqueName(uniqueName);

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

	public User addFriendRequest(User fromUser, Long targetUserId) {
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

	@Transactional
	public User follow(User userInDB, Long targetUserId) {
		logger.info(" adding follow form " + userInDB.getUserId() + " to targetUser : " + targetUserId);
		Set<User> targetUserSet = userDAO.findActiveUserById(targetUserId);

		if (targetUserSet.iterator().hasNext()) {
			User targetUser = targetUserSet.iterator().next();

			logger.info("found target user: " + targetUser.getEmail());

			userInDB.getFollowers().add(targetUser);
			targetUser.getFollowedBy().add(userInDB);

			userDAO.save(targetUser);
			userDAO.save(userInDB);

			return userInDB;
		} else {
			return null;
		}
	}

	@Transactional
	public User unfollow(User userInDB, Long targetUserId) {
		logger.info(" removing follow form " + userInDB.getUserId() + " to targetUser : " + targetUserId);
		Set<User> targetUserSet = userDAO.findActiveUserById(targetUserId);

		if (targetUserSet.iterator().hasNext()) {
			User targetUser = targetUserSet.iterator().next();

			logger.info("found target user: " + targetUser.getEmail());

			userInDB.getFollowers().remove(targetUser);
			targetUser.getFollowedBy().remove(userInDB);

			userDAO.save(userInDB);
			userDAO.save(targetUser);

			return userInDB;
		} else {
			return null;
		}
	}

	public void setKeysInRedis(String prefix, String uniqueName, String type, Long id) {
		redisService.setHashValue(prefix + ":" + uniqueName, type, "" + id);
	}

	public void deleteKeysInRedis(String uniqueName, String prefix) {
		redisService.deleteKey(prefix + ":" + uniqueName);
	}

	public User updateUser(User user) {
		return userDAO.save(user);
	}

	@Transactional
	public Object updateSetting(User user, UserSettingDTO userSettingDTO) {

		// check uniqueName
		if (userSettingDTO.getUniqueName() != null && !userSettingDTO.getUniqueName().equals(user.getUniqueName())) {
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DATE, ConstantsUtil.USER_DATE_AVAILABLE_TO_ASSIGN_UNIQUE_NAME);

			if (user.getLastUpdateUniqueNameDate() != null && user.getLastUpdateUniqueNameDate().compareTo(now.getTime()) < 0) {
				return ConstantsUtil.USER_DATE_AVAILABLE_TO_ASSIGN_UNIQUE_NAME + " days before next uniqueName change";
			} else {
				User checkUser = getUserByUniqueName(userSettingDTO.getUniqueName());
				if (checkUser == null) {

					if (userSettingDTO.getUniqueName() != null) {
						String oldUniqueName = user.getUniqueName();

						if (oldUniqueName != null) {
							logger.info("update setting : delete old uniqueName : " + oldUniqueName);
							deleteKeysInRedis(oldUniqueName, ConstantsUtil.REDIS_USER_GROUP);
						}
						logger.info("update setting : setting new uniqueName : " + oldUniqueName);

						user.setUniqueName(userSettingDTO.getUniqueName());
					}

					if (userSettingDTO.getMessage() != null) {
						user.setMessage(userSettingDTO.getMessage());
					}

					setKeysInRedis(ConstantsUtil.REDIS_USER_GROUP, userSettingDTO.getUniqueName(), ConstantsUtil.REDIS_USER_TYPE_ID, user.getUserId());

					user.setLastUpdateUniqueNameDate(Calendar.getInstance().getTime());

				} else {
					return "Unique Name already in use";
				}
			}

		}

		// profileImage
		if (userSettingDTO.getImage() != null) {
			// deactive current image
			imageService.disableAllProfileImageByUserId(user.getUserId());

			List<ProfileImage> imageSet = imageService.findPendProfileImageByFilename(userSettingDTO.getImage());
			for (ProfileImage image : imageSet) {
				image.setStatus(ConstantsUtil.IMAGE_ACTIVE);
				image.setUser(user);
				image.setSeq(1);
				imageService.saveProfileImage(image);
			}
		}

		// message
		if (userSettingDTO.getMessage() != null) {
			user.setMessage(userSettingDTO.getMessage());
		}

		if (userSettingDTO.getDisplayName() != null) {
			user.setDisplayName(userSettingDTO.getDisplayName());
		}

		user = updateUser(user);

		return user;
	}

	public Map<Long, User> getFollowerMapByUser(User user) {
		Map<Long, User> followerMap = new HashMap<>();

		for (User follower : user.getFollowers()) {
			followerMap.put(follower.getUserId(), follower);
		}

		return followerMap;
	}

	public List<User> searchEvents(Map<Long, Integer> map, String orderBy, User user) {
		return userDAO.findActiveUserByIdList(map.keySet());
	}
}
