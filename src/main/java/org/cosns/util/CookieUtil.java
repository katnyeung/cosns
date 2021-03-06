package org.cosns.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cosns.repository.User;

public class CookieUtil {
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	public static Map<String, String> readCookieMap(HttpServletRequest request) {
		Map<String, String> cookieMap = new HashMap<String, String>();
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}
		return cookieMap;
	}

	public static String handleCookie(HttpServletRequest request, HttpServletResponse response, User user) {
		Map<String, String> cookieMap = CookieUtil.readCookieMap(request);
		String userKey = cookieMap.get("userKey");

		if (userKey == null) {
			String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			// set cookie
			CookieUtil.addCookie(response, "userKey", uuid, 7 * 24 * 60 * 60);
		}
		return userKey;
	}

}
