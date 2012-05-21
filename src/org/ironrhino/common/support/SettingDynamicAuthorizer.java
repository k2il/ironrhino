package org.ironrhino.common.support;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.security.dynauth.DynamicAuthorizer;
import org.ironrhino.core.util.AuthzUtils;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named
public class SettingDynamicAuthorizer implements DynamicAuthorizer {

	@Inject
	protected SettingControl settingControl;

	public boolean authorize(UserDetails user, String resource) {
		String ifAnyGranted = settingControl.getStringValue("resource:"
				+ resource);
		return AuthzUtils.authorize(null, ifAnyGranted, null);
	}

}
