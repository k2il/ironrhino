package org.ironrhino.core.ext.struts;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ServletDispatcherResult;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import com.opensymphony.module.sitemesh.util.ClassLoaderUtil;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigResult extends FreemarkerResult {

	private static ServletDispatcherResult servletDispatcherResult = new ServletDispatcherResult();

	private String pageLocation = "/WEB-INF/view/jsp";

	private String freemarkerPageLocation = "/resources/view";

	@Inject(value = "ironrhino.autoconfig.page.location", required = false)
	public void setPageLocation(String val) {
		this.pageLocation = val;
	}

	@Inject(value = "ironrhino.autoconfig.freemarker.page.location", required = false)
	public void setFreemarkerPageLocation(String val) {
		this.freemarkerPageLocation = val;
	}

	public void execute(ActionInvocation invocation) throws Exception {
		if (invocation.getResultCode().equals(Action.SUCCESS)
				&& !invocation.getProxy().getMethod().equals("")
				&& !invocation.getProxy().getMethod().equals("execute")) {
			ActionContext ctx = invocation.getInvocationContext();
			HttpServletResponse response = (HttpServletResponse) ctx
					.get(ServletActionContext.HTTP_RESPONSE);
			String url = invocation.getProxy().getNamespace() + "/"
					+ invocation.getProxy().getActionName();
			response.sendRedirect(url);
		}
		String finalLocation = conditionalParse(location, invocation);
		if (finalLocation.endsWith(".jsp"))
			servletDispatcherResult.doExecute(finalLocation, invocation);
		else
			doExecute(finalLocation, invocation);
	}

	protected String conditionalParse(String param, ActionInvocation invocation) {
		String result = invocation.getResultCode();
		String location = pageLocation
				+ invocation.getProxy().getNamespace()
				+ "/"
				+ getTemplateName(invocation.getProxy().getActionName(), result)
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
					+ invocation.getProxy().getNamespace()
					+ "/"
					+ getTemplateName(invocation.getProxy().getActionName(),
							result) + ".ftl";
			url = ClassLoaderUtil.getResource(location.substring(1),
					AutoConfigResult.class);
		}
		if (url == null) {
			location = pageLocation + "/" + result + ".jsp";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null)
			location = freemarkerPageLocation + "/" + result + ".ftl";
		return location;
	}

	private String getTemplateName(String actionName, String resultName) {
		if (resultName.equals(Action.SUCCESS))
			return actionName;
		return actionName + "_" + resultName;
	}
}
