package org.ironrhino.core.session;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public interface HttpSessionManager extends HttpSessionStore {

	public static final String REQUEST_ATTRIBUTE_KEY_SESSION_MAP = "_session_map_in_request_";

	public static final String REQUEST_ATTRIBUTE_SESSION_TRACKER_IN_URL = "_session_tracker_in_url_";

	public static final String DEFAULT_SESSION_TRACKER_NAME = "T";

	public static final String DEFAULT_COOKIE_NAME_LOCALE = "locale";

	public String getSessionTracker(WrappedHttpSession session);

	public String getSessionTrackerName();

	public boolean supportSessionTrackerFromURL();

	public String getLocaleCookieName();

	public Locale getLocale(HttpServletRequest request);

}
