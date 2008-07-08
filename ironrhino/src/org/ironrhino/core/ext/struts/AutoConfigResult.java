package org.ironrhino.core.ext.struts;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ServletDispatcherResult;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import com.opensymphony.module.sitemesh.util.ClassLoaderUtil;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigResult extends FreemarkerResult {

	private static ServletDispatcherResult servletDispatcherResult = new ServletDispatcherResult();

	private String pageLocation = "/WEB-INF/view/jsp";

	private String freemarkerPageLocation = "/resources/view";

	private String baseNamespace = "";

	@Inject(value = "ironrhino.autoconfig.base.namespace", required = false)
	public void setBaseNamespace(String val) {
		this.baseNamespace = val;
	}

	@Inject(value = "ironrhino.autoconfig.page.location", required = false)
	public void setPageLocation(String val) {
		this.pageLocation = val;
	}

	@Inject(value = "ironrhino.autoconfig.freemarker.page.location", required = false)
	public void setFreemarkerPageLocation(String val) {
		this.freemarkerPageLocation = val;
	}

	public void execute(ActionInvocation invocation) throws Exception {
		String finalLocation = conditionalParse(location, invocation);
		if (finalLocation.endsWith(".jsp"))
			servletDispatcherResult.doExecute(finalLocation, invocation);
		else
			doExecute(finalLocation, invocation);
	}

	protected String conditionalParse(String param, ActionInvocation invocation) {
		String result = invocation.getResultCode();
		String location = pageLocation + invocation.getProxy().getNamespace()
				+ "/" + invocation.getProxy().getActionName() + "_" + result
				+ ".jsp";
		ServletContext context = ServletActionContext.getServletContext();
		URL url = null;
		try {
			url = context.getResource(location);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null) {
			location = freemarkerPageLocation
					+ invocation.getProxy().getNamespace() + "/"
					+ invocation.getProxy().getActionName() + "_" + result
					+ ".ftl";
			url = ClassLoaderUtil.getResource(location, AutoConfigResult.class);
		}
		if (url == null) {
			location = pageLocation + baseNamespace + "/" + result + ".jsp";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null)
			location = freemarkerPageLocation + baseNamespace + "/" + result + ".ftl";
		return location;
	}
}
