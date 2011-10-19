package org.ironrhino.security.acl.model;

import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.security.model.UserRole;

@AutoConfig(order = "role asc")
@Authorize(ifAllGranted = UserRole.ROLE_ADMINISTRATOR)
public class Acl extends BaseEntity {

	@NaturalId(mutable = true)
	@UiConfig(displayOrder = 1)
	private String role;

	@NaturalId(mutable = true)
	@UiConfig(displayOrder = 2)
	private String resource;

	@UiConfig(displayOrder = 3)
	private boolean permitted;

	public Acl() {
	}

	public Acl(String role, String resource, boolean permitted) {
		this.role = role;
		this.resource = resource;
		this.permitted = permitted;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public boolean isPermitted() {
		return permitted;
	}

	public void setPermitted(boolean permitted) {
		this.permitted = permitted;
	}

}
