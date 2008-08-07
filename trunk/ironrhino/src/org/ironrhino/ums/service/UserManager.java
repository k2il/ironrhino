package org.ironrhino.ums.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.ums.model.User;
import org.springframework.security.userdetails.UserDetailsService;

public interface UserManager extends BaseManager<User>, UserDetailsService {

	public void save(User user);

	public User getUserByUsername(String username);
}
