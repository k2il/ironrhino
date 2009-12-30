package org.ironrhino.core.session;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

@Singleton
@Named("httpSessionFilter")
public class HttpSessionFilter implements Filter {

	private static final String APPLIED_KEY = "APPLIED."
			+ HttpSessionFilter.class.getName();

	public static final String KEY_EXCLUDE_PATTERNS = "excludePatterns";

	public static final String DEFAULT_EXCLUDE_PATTERNS = "/remoting/*";

	private ServletContext servletContext;

	@Inject
	private HttpSessionManager httpSessionManager;

	private String[] excludePatterns;

	public void init(FilterConfig filterConfig) {
		servletContext = filterConfig.getServletContext();
		String str = filterConfig.getInitParameter(KEY_EXCLUDE_PATTERNS);
		if (StringUtils.isBlank(str))
			str = DEFAULT_EXCLUDE_PATTERNS;
		excludePatterns = str.split(",");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		if (req.getAttribute(APPLIED_KEY) != null) {
			chain.doFilter(request, response);
			return;
		}
		request.setAttribute(APPLIED_KEY, true);

		for (String pattern : excludePatterns) {
			String path = req.getRequestURI();
			path = path.substring(req.getContextPath().length());
			if (org.ironrhino.core.util.StringUtils.matchesWildcard(path,
					pattern)) {
				chain.doFilter(request, response);
				return;
			}
		}
		WrappedHttpSession session = new WrappedHttpSession(
				(HttpServletRequest) request, (HttpServletResponse) response,
				servletContext, httpSessionManager);
		WrappedHttpServletRequest wrappedHttpRequest = new WrappedHttpServletRequest(
				req, session);
		WrappedHttpServletResponse wrappedHttpResponse = new WrappedHttpServletResponse(
				(HttpServletResponse) response, session);
		chain.doFilter(wrappedHttpRequest, wrappedHttpResponse);
		try {
			session.save();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			wrappedHttpResponse.commit();
		}
	}

	public void destroy() {

	}
}
