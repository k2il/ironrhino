package org.ironrhino.security.socialauth.impl;

import java.io.InputStream;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;

@Named("douban")
@Singleton
public class DoubanImpl extends AbstractOAuthProvider {

	@Value("${douban.requestTokenUrl:http://www.douban.com/service/auth/request_token}")
	private String requestTokenUrl;

	@Value("${douban.authorizeUrl:http://www.douban.com/service/auth/authorize}")
	private String authorizeUrl;

	@Value("${douban.accessTokenUrl:http://www.douban.com/service/auth/access_token}")
	private String accessTokenUrl;

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	@Override
	protected Profile doGetProfile(Map<String, String> token) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new HttpClient4());
		OAuthMessage message = client.invoke(accessor,
				"http://api.douban.com/people/%40me", null);
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		InputStream is = message.getBodyAsStream();
		Document doc = db.parse(is);
		is.close();
		String uid = doc.getElementsByTagName("db:uid").item(0)
				.getTextContent();
		String displayName = doc.getElementsByTagName("title").item(0)
				.getTextContent();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setDisplayName(displayName);
		return p;
	}

}
