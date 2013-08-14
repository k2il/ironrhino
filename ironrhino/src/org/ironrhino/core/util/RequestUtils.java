package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class RequestUtils {

	public static String serializeData(HttpServletRequest request) {
		if (request.getMethod().equalsIgnoreCase("POST")
				|| request.getMethod().equalsIgnoreCase("PUT")) {
			StringBuilder sb = new StringBuilder();
			Map<String, String[]> map = request.getParameterMap();
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				if (entry.getKey().toLowerCase().contains("password"))
					continue;
				for (String value : entry.getValue()) {
					sb.append(entry.getKey())
							.append('=')
							.append(value.length() > 256 ? value.substring(0,
									256) : value).append('&');
				}
			}
			return sb.toString();
		}
		String queryString = request.getQueryString();
		return queryString != null ? queryString : "";
	}

	public static Map<String, String> getParametersMap(
			HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : request.getParameterMap()
				.entrySet()) {
			String name = entry.getKey();
			String[] value = entry.getValue();
			if (value != null && value.length > 0)
				map.put(name, value[0]);
		}
		return map;
	}

	public static String getRemoteAddr(HttpServletRequest request) {
		String addr = request.getHeader("X-Real-IP");
		if (StringUtils.isBlank(addr)) {
			addr = request.getHeader("X-Forwarded-For");
			int index = 0;
			if (StringUtils.isNotBlank(addr) && (index = addr.indexOf(',')) > 0)
				addr = addr.substring(0, index);
		}
		addr = StringUtils.isNotBlank(addr) ? addr : request.getRemoteAddr();
		addr = addr != null ? addr : "";
		return addr;
	}

	public static String trimPathParameter(String url) {
		if (url == null)
			return null;
		int i = url.indexOf(';');
		return i > -1 ? url.substring(0, i) : url;
	}

	public static String getBaseUrl(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		String ctxPath = request.getContextPath();
		return url.substring(
				0,
				url.indexOf(StringUtils.isBlank(ctxPath) ? "/" : ctxPath,
						url.indexOf("://") + 3)
						+ ctxPath.length());
	}

	public static String getBaseUrl(HttpServletRequest request, boolean secured) {
		return getBaseUrl(request, secured, true);
	}

	public static String getBaseUrl(HttpServletRequest request,
			boolean secured, boolean includeContextPath) {
		String host = "localhost";
		String protocol = "http";
		int port = 80;
		URL url = null;
		try {
			url = new URL(request.getRequestURL().toString());
			host = url.getHost();
			protocol = url.getProtocol();
			port = url.getPort();
			if (port <= 0)
				port = url.getDefaultPort();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if ((protocol.equalsIgnoreCase("https") && secured)
				|| (protocol.equalsIgnoreCase("http") && !secured)) {
			return getBaseUrl(request);
		}
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
		if (includeContextPath)
			sb.append(request.getContextPath());
		return sb.toString();
	}

	public static String getRequestUri(HttpServletRequest request) {
		// handle http dispatcher includes.
		String uri = (String) request
				.getAttribute("javax.servlet.include.servlet_path");
		if (uri == null) {
			uri = request.getRequestURI();
			uri = uri.substring(request.getContextPath().length());
		}
		return trimPathParameter(uri);
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
		saveCookie(request, response, cookieName, cookieValue, false);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, boolean global) {
		saveCookie(request, response, cookieName, cookieValue, -1, global,
				false);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, boolean global, boolean httpOnly) {
		saveCookie(request, response, cookieName, cookieValue, -1, global,
				httpOnly);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge, boolean global, boolean httpOnly) {
		String domain = null;
		String path = "".equals(request.getContextPath()) ? "/" : request
				.getContextPath();
		if (global) {
			domain = parseGlobalDomain(request.getServerName());
			path = "/";
		}
		saveCookie(request, response, cookieName, cookieValue, maxAge, domain,
				path, httpOnly);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge, String domain, String path,
			boolean httpOnly) {
		try {
			cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		Cookie cookie = new Cookie(cookieName, cookieValue);
		try {
			cookie.setHttpOnly(httpOnly);
		} catch (NoSuchMethodError e) {
			// for below servlet 3.0
		}
		if (StringUtils.isNotBlank(domain))
			cookie.setDomain(domain);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName) {
		deleteCookie(request, response, cookieName, false);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, boolean global) {
		deleteCookie(request, response, cookieName, "".equals(request
				.getContextPath()) ? "/" : request.getContextPath(), global);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String path) {
		deleteCookie(request, response, cookieName, path, false);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String path,
			boolean global) {
		String domain = null;
		if (global) {
			domain = parseGlobalDomain(request.getServerName());
			path = "/";
		}
		Cookie cookie = new Cookie(cookieName, null);
		if (StringUtils.isNotBlank(domain))
			cookie.setDomain(domain);
		cookie.setMaxAge(0);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static boolean isSameOrigin(String a, String b) {
		if (StringUtils.isBlank(a) || StringUtils.isBlank(b))
			return false;
		if (b.indexOf("://") < 0 || a.indexOf("://") < 0)
			return true;
		try {
			String host1 = new URL(a).getHost();
			if (host1 == null)
				host1 = "localhost";
			String host2 = new URL(b).getHost();
			if (host2 == null)
				host2 = "localhost";
			return host1.equalsIgnoreCase(host2)
					|| parseGlobalDomain(host1, host1).equalsIgnoreCase(
							parseGlobalDomain(host2, host2));
		} catch (MalformedURLException e) {
			return false;
		}

	}

	public static String getValueFromQueryString(String queryString, String name) {
		if (StringUtils.isBlank(queryString))
			return null;
		String[] arr = queryString.split("&");
		for (String s : arr) {
			String[] arr2 = s.split("=", 2);
			if (arr2[0].equals(name)) {
				if (arr2.length == 1)
					return null;
				String value = arr2[1];
				value = org.ironrhino.core.util.StringUtils.decodeUrl(value);
				return value;
			}
		}
		return null;
	}

	private static String parseGlobalDomain(String host) {
		if (host.matches("^(\\d+\\.){3}\\d+$"))
			return host;
		boolean topDouble = false;
		for (String s : topDoubleDomains) {
			if (host.endsWith(s)) {
				topDouble = true;
				break;
			}
		}
		String[] array = host.split("\\.");
		if (!topDouble && array.length >= 2) {
			StringBuilder sb = new StringBuilder();
			sb.append('.');
			sb.append(array[array.length - 2]);
			sb.append('.');
			sb.append(array[array.length - 1]);
			return sb.toString();
		} else if (topDouble && array.length >= 3) {
			StringBuilder sb = new StringBuilder();
			sb.append('.');
			sb.append(array[array.length - 3]);
			sb.append('.');
			sb.append(array[array.length - 2]);
			sb.append('.');
			sb.append(array[array.length - 1]);
			return sb.toString();
		}
		return null;
	}

	private static String parseGlobalDomain(String host, String _default) {
		host = parseGlobalDomain(host);
		return host != null ? host : _default;
	}

	private static String[] topDoubleDomains = new String[] { ".com.cn",
			".edu.cn", ".org.cn", ".net.cn", ".co.uk", "co.kr", "co.jp" };

}
