package org.cosns.auth;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cosns.repository.User;
import org.cosns.service.RedisService;
import org.cosns.service.UserService;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.CookieUtil;
import org.cosns.web.result.ResultFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthInterceptor implements HandlerInterceptor {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	RedisService redisService;

	@Autowired
	UserService userSerivce;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}

		if (!request.getRequestURI().contains("/js/") && !request.getRequestURI().contains("/css/")) {
			logger.info("IP [" + ipAddress + "] : " + request.getRequestURI());
		}

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (handler instanceof HandlerMethod) {
			// login for user if cookie available
			Map<String, String> cookieMap = CookieUtil.readCookieMap(request);
			if (user == null) {
				if (cookieMap.get("userKey") != null) {
					String userIdString = redisService.getAndRefreshUserIdCache(cookieMap.get("userKey"));
					if (userIdString != null) {
						Long userId = Long.parseLong(userIdString);
						user = userSerivce.getUserById(userId);
						session.setAttribute("user", user);
					} else {
						// remove the cooke if no redis key found
						CookieUtil.addCookie(response, "userKey", cookieMap.get("userKey"), 0);
					}

				}
			}

			HandlerMethod hm = (HandlerMethod) handler;
			Method method = hm.getMethod();

			if (method.isAnnotationPresent(Auth.class)) {

				ObjectMapper om = new ObjectMapper();

				if (user == null) {
					if (method.getReturnType() == String.class) {
						response.sendRedirect("/");
						return false;
					} else {

						response.setContentType("application/json; charset=utf-8");
						ServletOutputStream sos = response.getOutputStream();
						sos.print(om.writeValueAsString(ResultFactory.getErrorResult(ConstantsUtil.RESULT_ERROR, ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED)));

						sos.close();

						return true;
					}
				} else {
					Auth auth = method.getAnnotation(Auth.class);
					if (auth.type().equals("ADMIN")) {
						logger.info("User : " + user.getEmail() + ":" + user.getUserRole() + " access admin service ");
						if (user.getUserRole().equals("ADMIN")) {
							return true;
						} else {
							response.setContentType("application/json; charset=utf-8");
							ServletOutputStream sos = response.getOutputStream();
							sos.print(om.writeValueAsString(ResultFactory.getErrorResult(ConstantsUtil.RESULT_ERROR, ConstantsUtil.ERROR_MESSAGE_USER_NOT_FOUND)));

							sos.close();
							return false;
						}
					}

				}
			}
		}

		return true;
	}

}