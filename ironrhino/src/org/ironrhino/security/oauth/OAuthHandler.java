package org.ironrhino.security.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.servlet.AccessHandler;
import org.ironrhino.core.session.impl.DefaultHttpSessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Order(Integer.MIN_VALUE + 1)
public class OAuthHandler implements AccessHandler {

	@Value("${api.pattern:/api/*}")
	private String apiPattern;

	@Inject
	private OAuthManager oauthManager;

	@Override
	public String getPattern() {
		return apiPattern;
	}

	@Override
	public boolean handle(HttpServletRequest request,
			HttpServletResponse response) {
		String errorMessage = null;
		String token = request.getParameter("oauth_token");
		if (token == null) {
			String header = request.getHeader("Authorization");
			if (header != null) {
				header = header.trim();
				if (header.startsWith("OAuth ")) {
					header = header.substring("OAuth ".length());
					int i = header.indexOf("oauth_token=");
					if (i < 0) {
						// oauth 2.0
						token = header;
					} else {
						// oauth 1.0a
						header = header.substring(header.indexOf("\"", i) + 1);
						token = header.substring(0, header.indexOf("\""));
					}
				} else {
					errorMessage = "invalid Authorization header,must starts with OAuth ";
				}

			}
		}
		if (StringUtils.isNotBlank(token)) {
			Authorization authorization = oauthManager.getAuthorization(token);
			if (authorization != null) {
				String[] scopes = authorization.getScopes();
				boolean authorized = (scopes == null);
				if (!authorized && scopes != null) {
					for (String s : scopes) {
						String requestURL = request.getRequestURL().toString();
						if (requestURL.startsWith(s)) {
							authorized = true;
							break;
						}
					}
				}
				if (authorized) {
					UserDetails ud = authorization.getUser();
					SecurityContext sc = SecurityContextHolder.getContext();
					Authentication auth = new UsernamePasswordAuthenticationToken(
							ud, ud.getPassword(), ud.getAuthorities());
					sc.setAuthentication(auth);
					Map<String, Object> sessionMap = new HashMap<String, Object>();
					sessionMap
							.put(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
									sc);
					request.setAttribute(
							DefaultHttpSessionManager.REQUEST_ATTRIBUTE_KEY_SESSION_MAP,
							sessionMap);
					return false;
				} else {
					errorMessage = "Unauthorized Scope";
				}
			} else {
				errorMessage = "token is invalid or expired";
			}
		} else {
			errorMessage = "missing oauth_token parameter or Authorization header";
		}
		try {
			if (errorMessage != null)
				response.getWriter().write(errorMessage);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
