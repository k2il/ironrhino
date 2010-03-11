package org.ironrhino.core.struts;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.ServletDispatcherResult;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import com.opensymphony.module.sitemesh.util.ClassLoaderUtil;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigResult extends FreemarkerResult {

	private static final long serialVersionUID = -2277156996891287055L;

	public static final String DEFAULT_JSP_LOCATION = "/WEB-INF/view/jsp";

	public static final String DEFAULT_FTL_LOCATION = "/WEB-INF/view/ftl";

	public static final String DEFAULT_FTL_CLASSPATH = "/resources/view";

	private static ServletDispatcherResult servletDispatcherResult = new ServletDispatcherResult();

	@Inject(value = "ironrhino.view.jsp.location", required = false)
	private String jspLocation = DEFAULT_JSP_LOCATION;

	@Inject(value = "ironrhino.view.ftl.location", required = false)
	private String ftlLocation = DEFAULT_FTL_LOCATION;

	@Inject(value = "ironrhino.view.ftl.classpath", required = false)
	private String ftlClasspath = DEFAULT_FTL_CLASSPATH;

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		if (invocation.getResultCode().equals(Action.NONE))
			return;
		if (invocation.getResultCode().equals(Action.SUCCESS)
				&& !invocation.getProxy().getMethod().equals("")
				&& !invocation.getProxy().getMethod().equals("execute")) {
			ActionContext ctx = invocation.getInvocationContext();
			HttpServletRequest request = (HttpServletRequest) ctx
					.get(StrutsStatics.HTTP_REQUEST);
			HttpServletResponse response = (HttpServletResponse) ctx
					.get(StrutsStatics.HTTP_RESPONSE);
			String namespace = invocation.getProxy().getNamespace();
			String url = namespace + (namespace.endsWith("/") ? "" : "/")
					+ invocation.getProxy().getActionName();
			response.sendRedirect(request.getContextPath() + url);
			return;
		}
		String finalLocation = conditionalParse(location, invocation);
		if (finalLocation.endsWith(".jsp"))
			servletDispatcherResult.doExecute(finalLocation, invocation);
		else
			doExecute(finalLocation, invocation);
	}

	@Override
	protected String conditionalParse(String param, ActionInvocation invocation) {
		String result = invocation.getResultCode();
		String namespace = invocation.getProxy().getNamespace();
		String actionName = invocation.getProxy().getActionName();
		if (namespace.equals("/"))
			namespace = "";
		String location = jspLocation + namespace + "/"
				+ getTemplateName(actionName, result) + ".jsp";
		ServletContext context = ServletActionContext.getServletContext();
		URL url = null;
		try {
			url = context.getResource(location);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null) {
			location = ftlLocation + namespace + "/"
					+ getTemplateName(actionName, result) + ".ftl";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null) {
			location = ftlClasspath + namespace + "/"
					+ getTemplateName(actionName, result) + ".ftl";
			url = ClassLoaderUtil.getResource(location.substring(1),
					AutoConfigResult.class);
		}
		if (url == null) {
			location = jspLocation + "/meta/result/" + result + ".jsp";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null)
			location = ftlClasspath + "/meta/result/" + result + ".ftl";
		return location;
	}

	private String getTemplateName(String actionName, String resultName) {
		if (resultName.equals(Action.SUCCESS))
			return actionName;
		return actionName + "_" + resultName;
	}
}
