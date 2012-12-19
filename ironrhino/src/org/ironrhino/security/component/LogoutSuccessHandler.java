package org.ironrhino.security.component;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.spring.security.DefaultLogoutSuccessHandler;
import org.ironrhino.security.event.LogoutEvent;
import org.ironrhino.security.model.User;
import org.springframework.security.core.Authentication;

public class LogoutSuccessHandler extends DefaultLogoutSuccessHandler {

	@Inject
	private transient EventPublisher eventPublisher;

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		super.onLogoutSuccess(request, response, authentication);
		Object principal = authentication.getPrincipal();
		if (principal instanceof User)
			eventPublisher.publish(new LogoutEvent((User) principal), false);
	}

}
