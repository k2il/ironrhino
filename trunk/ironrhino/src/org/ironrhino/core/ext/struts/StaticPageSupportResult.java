package org.ironrhino.core.ext.struts;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import com.opensymphony.xwork2.ActionContext;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class StaticPageSupportResult extends FreemarkerResult {

	private boolean generateStaticPage;

	private String staticPageLocation;

	public boolean isGenerateStaticPage() {
		return generateStaticPage;
	}

	public void setGenerateStaticPage(boolean generateStaticPage) {
		this.generateStaticPage = generateStaticPage;
	}

	public String getStaticPageLocation() {
		return staticPageLocation;
	}

	public void setStaticPageLocation(String staticPageLocation) {
		this.staticPageLocation = staticPageLocation;
	}

	protected void postTemplateProcess(Template template, TemplateModel model)
			throws IOException {
		if (!generateStaticPage)
			return;
		if (StringUtils.isBlank(staticPageLocation)
				|| !staticPageLocation.endsWith(".html"))
			return;
		try {
			template.process(model, getFileWriter());
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}

	protected Writer getFileWriter() throws IOException {
		ActionContext ctx = invocation.getInvocationContext();
		ServletContext sc = (ServletContext) ctx
				.get(ServletActionContext.SERVLET_CONTEXT);
		String filename = sc.getRealPath(conditionalParse(staticPageLocation,
				invocation));
		return new FileWriter(filename);
	}

}
