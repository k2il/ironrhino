package org.ironrhino.common.action;

import org.ironrhino.core.metadata.AutoConfig;

import com.opensymphony.xwork2.ActionSupport;

@AutoConfig(namespace = DirectPageAction.NAMESPACE, actionName = DirectPageAction.ACTION_NAME)
public class DirectPageAction extends ActionSupport {

	private static final long serialVersionUID = -5865373753328653067L;

	public static final String NAMESPACE = "/";
	public static final String ACTION_NAME = "_direct_page_";

	@Override
	public String execute() {
		return "directPage";
	}

}
