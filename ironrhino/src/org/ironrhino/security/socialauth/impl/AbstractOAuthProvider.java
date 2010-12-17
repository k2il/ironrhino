package org.ironrhino.security.socialauth.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;

public abstract class AbstractOAuthProvider extends AbstractAuthProvider {

	@Inject
	protected SettingControl settingControl;

	protected String consumerKey;
	protected String consumerSecret;

	protected OAuthServiceProvider serviceProvider;

	public abstract String getRequestTokenUrl();

	public abstract String getAuthorizeUrl();

	public abstract String getAccessTokenUrl();

	protected static ObjectMapper mapper = new ObjectMapper();

	protected static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	@PostConstruct
	public void init() {
		try {
			for (Field f : AbstractOAuthProvider.class.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (f.getType().equals(String.class)
						&& Modifier.isProtected(mod) && !Modifier.isStatic(mod)) {
					String settingValue = settingControl
							.getStringValue("oauth." + getName() + "."
									+ f.getName());
					if (StringUtils.isNotBlank(settingValue)) {
						f.setAccessible(true);
						f.set(this, settingValue);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(),
				getAuthorizeUrl(), getAccessTokenUrl());
	}

	public String getLoginRedirectURL(HttpServletRequest request,
			String returnToURL) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(returnToURL, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		Map<String, String> map = new HashMap<String, String>(4);
		map.put(OAuth.OAUTH_CALLBACK, returnToURL);
		client.getRequestToken(accessor, null, map.entrySet());
		map.clear();
		map.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
		map.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
		saveToken(request, map);
		map.remove(OAuth.OAUTH_TOKEN_SECRET);
		map.put(OAuth.OAUTH_CALLBACK, returnToURL);
		OAuthMessage response = client.invoke(accessor,
				serviceProvider.userAuthorizationURL, map.entrySet());
		return response.URL;
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		Map<String, String> map = restoreToken(request);
		accessor.requestToken = map.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = map.get(OAuth.OAUTH_TOKEN_SECRET);
		map.clear();
		String oauth_verifier = request.getParameter(OAuth.OAUTH_VERIFIER);
		if (oauth_verifier != null)
			map.put(OAuth.OAUTH_VERIFIER, oauth_verifier);
		client.getAccessToken(accessor, "GET", map.entrySet());
		map.clear();
		map.put(OAuth.OAUTH_TOKEN, accessor.accessToken);
		map.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
		saveToken(request, map);
		return doGetProfile(client, accessor);
	}

	protected abstract Profile doGetProfile(OAuthClient client,
			OAuthAccessor accessor) throws Exception;

	protected OAuthMessage sendRequest(HttpServletRequest request, Map map,
			String url) throws Exception {
		Map<String, String> token = restoreToken(request);
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		return client.invoke(accessor, "GET", url, map != null ? map.entrySet()
				: null);
	}

	protected void saveToken(HttpServletRequest request,
			Map<String, String> token) {
		request.getSession().setAttribute(getName() + "_token",
				JsonUtils.toJson(token));
	}

	protected Map<String, String> restoreToken(HttpServletRequest request)
			throws Exception {
		String key = getName() + "_token";
		String json = (String) request.getSession().getAttribute(key);
		return JsonUtils.fromJson(json,
				new TypeReference<Map<String, String>>() {
				});
	}

	protected String generateId(String uid) {
		return "(" + getName() + ")" + uid;
	}

}
