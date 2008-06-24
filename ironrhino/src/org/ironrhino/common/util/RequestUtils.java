package org.ironrhino.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestUtils {

	public static String getBaseUrl(HttpServletRequest request) {
		StringBuffer sb = request.getRequestURL();
		sb.delete(sb.length() - request.getServletPath().length(), sb.length());
		return sb.toString();
	}

	public static String getBaseUrl(HttpServletRequest request, boolean secured) {
		String host = request.getServerName();
		String schema = request.getScheme();
		if ((schema.equalsIgnoreCase("https") && secured)
				|| (schema.equalsIgnoreCase("http") && !secured)) {
			String url = request.getRequestURL().toString();
			return url.substring(0, url.indexOf(request.getServletPath()));
		}
		int port = request.getServerPort();
		String contextPath = request.getContextPath();
		StringBuilder sb = new StringBuilder();
		sb.append(secured ? "https://" : "http://");
		sb.append(host);
		if (secured) {
			if (port == 8080)
				sb.append(":8443");
		} else {
			if (port == 8443)
				sb.append(":8080");
		}
		sb.append(contextPath);
		return sb.toString();
	}

	public static String getCookieValue(HttpServletRequest request,
			String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies)
			if (cookieName.equalsIgnoreCase(cookie.getName()))
				try {
					return URLDecoder.decode(cookie.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return cookie.getValue();
				}
		return null;
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String cookieValue) {
		saveCookie(request, response, cookieName, cookieValue, -1);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge) {
		saveCookie(request, response, cookieName, cookieValue, maxAge, ""
				.equals(request.getContextPath()) ? "/" : request
				.getContextPath());
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge, String path) {
		try {
			cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName) {
		deleteCookie(request, response, cookieName, "".equals(request
				.getContextPath()) ? "/" : request.getContextPath());
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String path) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		cookie.setPath(path);
		response.addCookie(cookie);
	}
}
