package org.ironrhino.core.security.captcha;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton@Named("captchaFilter")
public class CaptchaFilter implements Filter {

	public static final String DEFAULT_IMAGE_CAPTCHA_URL = "/captcha.jpg";

	@Inject
	private transient CaptchaManager captchaManager;

	private String imageCaptchaUrl = DEFAULT_IMAGE_CAPTCHA_URL;

	public void setImageCaptchaUrl(String imageCaptchaUrl) {
		this.imageCaptchaUrl = imageCaptchaUrl;
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String token = request.getParameter("token");
		String path = request.getServletPath();
		if (path.equals(imageCaptchaUrl)) {
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			ImageIO.write(new ImageCaptcha(captchaManager.getChallenge(request,
					token)).getImage(), "JPEG", response.getOutputStream());
			response.getOutputStream().close();
			return;
		}
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {

	}

}
