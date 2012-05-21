package org.ironrhino.core.session.impl;

import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.session.SessionCompressor;
import org.ironrhino.core.util.CodecUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Singleton
@Named
public class SecurityContextSessionCompressor implements
		SessionCompressor<SecurityContext> {

	@Inject
	private UserDetailsService userDetailsService;

	public boolean supportsKey(String key) {
		return HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
				.equals(key);
	}

	public String compress(SecurityContext sc) {
		if (sc != null) {
			Authentication auth = sc.getAuthentication();
			if (auth != null) {
				if (auth.getCredentials() instanceof X509Certificate)
					return null;
				if (auth.isAuthenticated()) {
					UserDetails ud = (UserDetails) auth.getPrincipal();
					return new StringBuilder(
							CodecUtils.md5Hex(ud.getPassword())).append(",")
							.append(ud.getUsername()).toString();
				}
			}
		}
		return null;
	}

	public SecurityContext uncompress(String string) {
		SecurityContext sc = SecurityContextHolder.getContext();
		if (StringUtils.isNotBlank(string))
			try {
				String[] arr = string.split(",", 2);
				UserDetails ud = userDetailsService.loadUserByUsername(arr[1]);
				if (CodecUtils.md5Hex(ud.getPassword()).equals(arr[0])
						&& ud.isEnabled() && ud.isAccountNonExpired()
						&& ud.isAccountNonLocked()
						&& ud.isCredentialsNonExpired())
					sc.setAuthentication(new UsernamePasswordAuthenticationToken(
							ud, ud.getPassword(), ud.getAuthorities()));
			} catch (UsernameNotFoundException e) {
				e.printStackTrace();
			}
		return sc;
	}
}
