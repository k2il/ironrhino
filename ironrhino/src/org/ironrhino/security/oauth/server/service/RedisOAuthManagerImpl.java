package org.ironrhino.security.oauth.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.metadata.ClusterProfile;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;
import org.springframework.data.redis.core.RedisTemplate;

@Singleton
@Named("oauthManager")
@ClusterProfile
public class RedisOAuthManagerImpl implements OAuthManager {

	private RedisTemplate<String, Authorization> authorizationRedisTemplate;

	private RedisTemplate<String, Client> clientRedisTemplate;

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	private static final String NAMESPACE_AUTHORIZATION = "{oauth:authorization}";
	private static final String NAMESPACE_AUTHORIZATION_GRANTOR = "{oauth:authorization:grantor}";

	private static final String NAMESPACE_CLIENT = "{oauth:client}";
	private static final String NAMESPACE_CLIENT_OWNER = "{oauth:client:owner}";

	private long expireTime = DEFAULT_EXPIRE_TIME;

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.authorizationRedisTemplate = redisTemplate;
		this.clientRedisTemplate = redisTemplate;
	}

	public Authorization generate(Client client, String redirectUri,
			String scope, String responseType) {
		if (!client.supportsRedirectUri(redirectUri))
			throw new IllegalArgumentException("REDIRECT_URI_MISMATCH");
		Authorization auth = new Authorization();
		auth.setId(CodecUtils.nextId());
		auth.setClient(client);
		if (StringUtils.isNotBlank(scope))
			auth.setScope(scope);
		if (StringUtils.isNotBlank(responseType))
			auth.setResponseType(responseType);
		authorizationRedisTemplate.opsForValue().set(
				NAMESPACE_AUTHORIZATION + auth.getId(), auth, expireTime,
				TimeUnit.SECONDS);
		return auth;
	}

	public Authorization grant(String authorizationId, User grantor) {
		String key = NAMESPACE_AUTHORIZATION + authorizationId;
		Authorization auth = authorizationRedisTemplate.opsForValue().get(key);
		if (auth == null)
			throw new IllegalArgumentException("BAD_AUTH");
		auth.setGrantor(grantor);
		auth.setModifyDate(new Date());
		if (auth.isClientSide()) {
			authorizationRedisTemplate.delete(key);
			authorizationRedisTemplate.opsForValue().set(
					NAMESPACE_AUTHORIZATION + auth.getAccessToken(), auth,
					expireTime, TimeUnit.SECONDS);
			stringRedisTemplate.opsForValue().set(
					NAMESPACE_AUTHORIZATION + auth.getRefreshToken(),
					NAMESPACE_AUTHORIZATION + auth.getAccessToken());
		} else {
			auth.setCode(CodecUtils.nextId());
			authorizationRedisTemplate.delete(key);
			authorizationRedisTemplate.opsForValue().set(
					NAMESPACE_AUTHORIZATION + auth.getCode(), auth, expireTime,
					TimeUnit.SECONDS);
		}
		stringRedisTemplate.opsForList().leftPush(
				NAMESPACE_AUTHORIZATION_GRANTOR
						+ auth.getGrantor().getUsername(),
				auth.getAccessToken());
		return auth;
	}

	public void deny(String authorizationId) {
		authorizationRedisTemplate.delete(NAMESPACE_AUTHORIZATION
				+ authorizationId);
	}

	public Authorization authenticate(String code, Client client) {
		String key = NAMESPACE_AUTHORIZATION + code;
		Authorization auth = authorizationRedisTemplate.opsForValue().get(key);
		if (auth == null)
			throw new IllegalArgumentException("CODE_INVALID");
		if (auth.isClientSide())
			throw new IllegalArgumentException("NOT_SERVER_SIDE");
		if (auth.getGrantor() == null)
			throw new IllegalArgumentException("USER_NOT_GRANTED");
		Client orig = auth.getClient();
		if (!orig.getId().equals(client.getId()))
			throw new IllegalArgumentException("CLIENT_ID_MISMATCH");
		if (!orig.getSecret().equals(client.getSecret()))
			throw new IllegalArgumentException("CLIENT_SECRET_MISMATCH");
		if (!orig.supportsRedirectUri(client.getRedirectUri()))
			throw new IllegalArgumentException("REDIRECT_URI_MISMATCH");
		auth.setCode(null);
		auth.setRefreshToken(CodecUtils.nextId());
		auth.setModifyDate(new Date());
		authorizationRedisTemplate.delete(key);
		authorizationRedisTemplate.opsForValue().set(
				NAMESPACE_AUTHORIZATION + auth.getAccessToken(), auth,
				expireTime, TimeUnit.SECONDS);
		stringRedisTemplate.opsForValue().set(
				NAMESPACE_AUTHORIZATION + auth.getRefreshToken(),
				NAMESPACE_AUTHORIZATION + auth.getAccessToken());
		return auth;
	}

	public Authorization retrieve(String accessToken) {
		String key = NAMESPACE_AUTHORIZATION + accessToken;
		Authorization auth = authorizationRedisTemplate.opsForValue().get(key);
		authorizationRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
		if (auth != null) {
			if (auth.getExpiresIn() > 0) {
				long offset = System.currentTimeMillis()
						- auth.getModifyDate().getTime();
				if (offset > auth.getExpiresIn() * 1000)
					return null;
			}
		}
		return auth;
	}

	public Authorization refresh(String refreshToken) {
		String keyRefreshToken = NAMESPACE_AUTHORIZATION + refreshToken;
		Authorization auth = authorizationRedisTemplate.opsForValue().get(
				stringRedisTemplate.opsForValue().get(keyRefreshToken));
		String keyAccessToken = NAMESPACE_AUTHORIZATION + auth.getAccessToken();
		authorizationRedisTemplate.delete(keyAccessToken);
		auth.setAccessToken(CodecUtils.nextId());
		auth.setModifyDate(new Date());
		authorizationRedisTemplate.opsForValue().set(
				NAMESPACE_AUTHORIZATION + auth.getAccessToken(), auth,
				expireTime, TimeUnit.SECONDS);
		stringRedisTemplate.opsForValue().set(
				NAMESPACE_AUTHORIZATION + auth.getRefreshToken(),
				NAMESPACE_AUTHORIZATION + auth.getAccessToken());
		return auth;
	}

	public void revoke(String accessToken) {
		String key = NAMESPACE_AUTHORIZATION + accessToken;
		Authorization auth = authorizationRedisTemplate.opsForValue().get(key);
		authorizationRedisTemplate.delete(key);
		authorizationRedisTemplate.delete(NAMESPACE_AUTHORIZATION
				+ auth.getRefreshToken());
		stringRedisTemplate.opsForList().remove(
				NAMESPACE_AUTHORIZATION_GRANTOR
						+ auth.getGrantor().getUsername(), 0, accessToken);
	}

	public List<Authorization> findAuthorizationsByGrantor(User grantor) {
		String keyForList = NAMESPACE_AUTHORIZATION_GRANTOR
				+ grantor.getUsername();
		List<String> tokens = stringRedisTemplate.opsForList().range(
				keyForList, 0, -1);
		if (tokens == null || tokens.isEmpty())
			return Collections.EMPTY_LIST;
		List<String> keys = new ArrayList<String>(tokens.size());
		for (String token : tokens)
			keys.add(NAMESPACE_AUTHORIZATION + token);
		return authorizationRedisTemplate.opsForValue().multiGet(keys);
	}

	public void removeExpired() {
	}

	public void saveClient(Client client) {
		if (client.isNew())
			client.setId(CodecUtils.nextId());
		String key = NAMESPACE_CLIENT + client.getId();
		clientRedisTemplate.opsForValue().set(key, client);
		if (client.getOwner() != null)
			stringRedisTemplate.opsForSet().add(
					NAMESPACE_CLIENT_OWNER + client.getOwner().getUsername(),
					client.getId());
	}

	public void deleteClient(Client client) {
		if (client.isNew())
			return;
		String key = NAMESPACE_CLIENT + client.getId();
		clientRedisTemplate.delete(key);
		if (client.getOwner() != null)
			stringRedisTemplate.opsForSet().remove(
					NAMESPACE_CLIENT_OWNER + client.getOwner().getUsername(),
					client.getId());
	}

	public Client findClientById(String clientId) {
		String key = NAMESPACE_CLIENT + clientId;
		return clientRedisTemplate.opsForValue().get(key);
	}

	public List<Client> findClientByOwner(User owner) {
		String keyForSet = NAMESPACE_CLIENT_OWNER + owner.getUsername();
		Set<String> ids = stringRedisTemplate.opsForSet().members(keyForSet);
		if (ids == null || ids.isEmpty())
			return Collections.EMPTY_LIST;
		List<String> keys = new ArrayList<String>(ids.size());
		for (String id : ids)
			keys.add(NAMESPACE_CLIENT + id);
		List<Client> list = clientRedisTemplate.opsForValue().multiGet(keys);
		Collections.sort(list, new Comparator<Client>() {
			public int compare(Client o1, Client o2) {
				return o1.getCreateDate().compareTo(o2.getCreateDate());
			}
		});
		return list;
	}

}
