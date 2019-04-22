package org.cosns.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConstantsUtil {
	public final static String USER_STATUS_ACTIVE = "A";
	public final static String USER_STATUS_PEND = "P";
	public final static String USER_STATUS_DISABLED = "D";
	public final static String USER_STATUS_REMOVED = "X";

	public final static String RESULT_SUCCESS = "success";
	public final static String RESULT_ERROR = "error";

	public static final String IMAGE_PEND = "P";
	public static final String IMAGE_ACTIVE = "A";
	public static final String IMAGE_DELETED = "D";

	public static final String POST_PEND = "P";
	public static final String POST_ACTIVE = "A";
	public static final String POST_DELETED = "D";

	public static final String EVENT_PEND = "P";
	public static final String EVENT_ACTIVE = "A";
	public static final String EVENT_DELETED = "D";

	public static final String POST_REACTION_ACTIVE = "A";
	public static final String POST_REACTION_CANCEL = "D";
	public static final String POST_REACTION_TYPE_INCREASE = "incr";
	public static final String POST_REACTION_TYPE_DECREASE = "decr";

	public static final String ERROR_MESSAGE_LOGIN_FAIL = "Either email not found or password incorrect";
	public static final String ERROR_MESSAGE_LOGIN_REQUIRED = "Please Login";
	public static final String ERROR_MESSAGE_ADD_FRIEND_FAIL = "NO SUCH FRIEND";

	public static final String PHOTO_POST_PREFIX = "photo";
	public static final String SELLING_POST_PREFIX = "sell";

	public static final String REDIS_HASHTAG_PREFIX = "hashtag";
	public static final String REDIS_USER_UNIQUENAME_PREFIX = "user";
	public static final String REDIS_POST_UNIQUENAME_PREFIX = "post";

	public static final String ERROR_MESSAGE_USER_NOT_FOUND = "User not found";
	public static final String ERROR_MESSAGE_USER_ALREADY_FOLLOWED = "User already followed";

	public static final String REDIS_POST_VIEW_PREFIX = "postview";
	
	public static final Map<String, String> mimeMap;
	static {
		Map<String, String> aMap = new HashMap<>();
		aMap.put("image/jpeg", "jpg");
		aMap.put("image/png", "png");
		mimeMap = Collections.unmodifiableMap(aMap);
	}

}
