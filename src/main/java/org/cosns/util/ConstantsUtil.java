package org.cosns.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConstantsUtil {

	public static final String HASHTAG_PATTERN = "#([^\\s][^#^\\n]*)";

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
	public static final String ERROR_MESSAGE_USER_NOT_FOUND = "User not found";
	public static final String ERROR_MESSAGE_USER_ALREADY_FOLLOWED = "User already followed";

	public static final String REDIS_POST_TAG_GROUP = "posttag";
	public static final String REDIS_POST_TAG_TYPE_PHOTO = "photo";
	public static final String REDIS_POST_TAG_TYPE_SELLING = "sell";
	public static final String REDIS_POST_TAG_TYPE_ALL = "all";

	public static final String REDIS_POST_GROUP = "postset";

	public static final String REDIS_POST_NAME_GROUP = "postname";
	public static final String REDIS_POST_ID = "id";

	public static final String REDIS_POST_LIKE_GROUP = "postlike";
	public static final String REDIS_POST_RETWEET_GROUP = "postretweet";

	public static final String REDIS_USER_GROUP = "user";
	public static final String REDIS_USER_TYPE_ID = "id";

	public static final String REDIS_POST_VIEW_GROUP = "postview";
	public static final String REDIS_POST_VIEW_TYPE_TOTAL = "total";
	public static final String REDIS_POST_VIEW_TYPE_TODAY = "today";

	public static final int USER_DATE_AVAILABLE_TO_ASSIGN_UNIQUE_NAME = 20;

	public static final int POST_KEY_HASHTAG_LENGTH = 10;
	public static final int POST_KEY_HASHTAG_NUMBER = 3;

	public static final Map<String, String> mimeMap;

	static {
		Map<String, String> aMap = new HashMap<>();
		aMap.put("image/jpeg", "jpg");
		aMap.put("image/png", "png");
		mimeMap = Collections.unmodifiableMap(aMap);
	}

}
