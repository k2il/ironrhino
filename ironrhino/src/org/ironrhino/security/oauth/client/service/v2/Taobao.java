package org.ironrhino.security.oauth.client.service.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth2Provider;
import org.springframework.beans.factory.annotation.Value;

@Named("taobao")
@Singleton
public class Taobao extends OAuth2Provider {

	@Value("${taobao.logo:http://img01.taobaocdn.com/tps/i1/T1T2RZXf8nXXXXXXXX-140-35.png}")
	private String logo;

	@Value("${taobao.authorizeUrl:https://oauth.taobao.com/authorize}")
	private String authorizeUrl;

	@Value("${taobao.accessTokenEndpoint:https://oauth.taobao.com/token}")
	private String accessTokenEndpoint;

	@Value("${taobao.scope:}")
	private String scope;

	@Value("${taobao.profileUrl:http://gw.api.taobao.com/router/rest?format=json&v=2.0&method=taobao.user.get&fields=user_id,nick,sex}")
	private String profileUrl;

	@Override
	public String getLogo() {
		return logo;
	}

	@Override
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public String getProfileUrl() {
		StringBuilder sb = new StringBuilder(profileUrl);
		sb.append("&app_key=").append(getClientId()).append("&timestamp=");
		try {
			sb.append(URLEncoder.encode(DateUtils.formatDatetime(new Date()),
					"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	@Override
	public boolean isUseAuthorizationHeader() {
		return false;
	}

	protected String getAccessTokenParameterName() {
		return "session";
	}

	@Override
	protected Profile getProfileFromContent(String content) throws Exception {
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		data = data.get("user_get_response");
		data = data.get("user");
		String uid = data.get("user_id").getTextValue();
		Profile p = new Profile();
		p.setUid(generateUid(uid));
		p.setDisplayName(data.get("nick").getTextValue());
		p.setName(data.get("nick").getTextValue());
		p.setGender(data.get("sex").getTextValue());
		return p;
	}

	@Override
	protected String invoke(String protectedURL, Map<String, String> params,
			Map<String, String> headers) {
		if (params == null)
			params = new HashMap<String, String>();
		params.put("sign_method", "md5");
		params.put("sign", sign(protectedURL, params));
		return super.invoke(protectedURL, params, headers);
	}

	private String sign(String protectedURL, Map<String, String> params) {
		Map<String, String> map = new TreeMap<String, String>();
		if (protectedURL.indexOf('?') > 0) {
			String queryString = protectedURL.substring(protectedURL
					.indexOf('?') + 1);
			String[] arr = queryString.split("&");
			for (String s : arr) {
				String[] arr2 = s.split("=");
				String key = arr2[0];
				String value = "";
				if (arr2.length > 1) {
					try {
						value = URLDecoder.decode(arr2[1], "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				map.put(key, value);
			}
		}
		if (params != null)
			map.putAll(params);
		StringBuilder sb = new StringBuilder(getClientSecret());
		for (Map.Entry<String, String> entry : map.entrySet())
			sb.append(entry.getKey()).append(entry.getValue());
		sb.append(getClientSecret());
		return CodecUtils.md5Hex(sb.toString()).toUpperCase();
	}

}
