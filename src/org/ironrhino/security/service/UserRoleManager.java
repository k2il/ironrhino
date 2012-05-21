package org.ironrhino.security.service;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.util.ClassScaner;
import org.ironrhino.security.model.UserRole;
import org.springframework.context.ApplicationContext;

@Singleton
@Named
public class UserRoleManager {

	private Set<String> staticRoles;

	private Collection<UserRoleProvider> providers;

	@Inject
	private ApplicationContext ctx;

	@PostConstruct
	public void init() {
		providers = ctx.getBeansOfType(UserRoleProvider.class).values();
	}

	public Set<String> getStaticRoles() {
		if (staticRoles == null) {
			Set<String> temp = new LinkedHashSet<String>();
			Set<Class<?>> set = ClassScaner.scanAssignable(
					ClassScaner.getAppPackages(), UserRole.class);
			for (Class<?> c : set) {
				if (Enum.class.isAssignableFrom(c)) {
					for (Object en : c.getEnumConstants()) {
						temp.add(en.toString());
					}
				} else {
					Field[] fields = c.getDeclaredFields();
					for (Field f : fields) {
						if (f.getName().startsWith("ROLE_BUILTIN_"))
							continue;
						temp.add(f.getName());
					}
				}
			}
			staticRoles = temp;
		}
		return staticRoles;
	}

	public Map<String, String> getCustomRoles() {
		Map<String, String> customRoles = new LinkedHashMap<String, String>();
		for (UserRoleProvider p : providers) {
			Map<String, String> map = p.getRoles();
			if (map != null)
				customRoles.putAll(map);
		}
		return customRoles;
	}

	public Map<String, String> getAllRoles() {
		Set<String> staticRoles = getStaticRoles();
		Map<String, String> customRoles = getCustomRoles();
		Map<String, String> roles = new LinkedHashMap<String, String>(
				staticRoles.size() + customRoles.size());
		for (String role : staticRoles)
			roles.put(role, null);
		roles.putAll(customRoles);
		return roles;
	}

}
