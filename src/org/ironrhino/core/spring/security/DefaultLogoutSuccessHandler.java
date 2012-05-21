package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

public class DefaultLogoutSuccessHandler extends
		AbstractAuthenticationTargetUrlRequestHandler implements
		LogoutSuccessHandler {
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		String targetUrl = getDefaultTargetUrl();
		if (StringUtils.hasText(request.getParameter("targetUrl"))) {
			targetUrl = request.getParameter("targetUrl");
		} else if (request.getParameter("referer") != null) {
			String temp = request.getHeader("Referer");
			if (StringUtils.hasText(temp))
				targetUrl = temp;
		}
		return targetUrl;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		super.handle(request, response, authentication);
	}

}
