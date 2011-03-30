package org.ironrhino.security.action;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.metadata.Redirect;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.spring.security.DefaultUsernamePasswordAuthenticationFilter;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(namespace = "/")
public class LoginAction extends BaseAction {

	public final static String COOKIE_NAME_LOGIN_USER = "U";

	private static final long serialVersionUID = 2783386542815083811L;

	private static Logger log = LoggerFactory.getLogger(LoginAction.class);

	private String password;

	private String username;

	@Inject
	private transient DefaultUsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

	@Autowired(required = false)
	private HttpSessionManager httpSessionManager;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	@Redirect
	@InputConfig(methodName = INPUT)
	@Captcha(threshold = 3)
	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		Authentication authResult = null;
		try {
			authResult = usernamePasswordAuthenticationFilter
					.attemptAuthentication(request, response);
		} catch (AuthenticationException failed) {
			if (failed instanceof DisabledException)
				addFieldError("username", getText("user.disabled"));
			else if (failed instanceof LockedException)
				addFieldError("username", getText("user.locked"));
			else if (failed instanceof AccountExpiredException)
				addFieldError("username", getText("user.expired"));
			else if (failed instanceof BadCredentialsException)
				addFieldError("password", getText("user.bad.credentials"));
			else if (failed instanceof CredentialsExpiredException)
				addFieldError("password", getText("user.bad.expired"));
			captchaManager.addCaptachaThreshold(request);
			try {
				usernamePasswordAuthenticationFilter.unsuccess(request,
						response, failed);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return SUCCESS;
		}
		if (authResult != null)
			try {
				usernamePasswordAuthenticationFilter.success(request, response,
						authResult);
				if (request.isRequestedSessionIdFromURL()
						&& httpSessionManager != null)
					response.setHeader(httpSessionManager
							.getSessionTrackerName(), request.getSession()
							.getId());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		return SUCCESS;
	}

	@Override
	public String input() {
		HttpServletRequest request = ServletActionContext.getRequest();
		if (StringUtils.isBlank(targetUrl)) {
			targetUrl = request.getHeader("Referer");
			if (StringUtils.isBlank(targetUrl))
				targetUrl = "/";
		}
		username = RequestUtils.getCookieValue(request,
				DefaultAuthenticationSuccessHandler.COOKIE_NAME_LOGIN_USER);
		return SUCCESS;
	}

}
