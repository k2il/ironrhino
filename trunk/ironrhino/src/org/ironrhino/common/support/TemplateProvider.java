package org.ironrhino.common.support;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateHashModelEx;

public class TemplateProvider {

	private String templateDirectory = "/WEB-INF/view/ftl";

	private Configuration configuration;

	@Autowired
	private ResourceLoader resourceLoader;

	private Map allSharedVariables;

	public void setAllSharedVariables(Map allSharedVariables) {
		this.allSharedVariables = allSharedVariables;
	}

	public String getTemplateDirectory() {
		return templateDirectory;
	}

	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		configuration = new Configuration();
		configuration.setDirectoryForTemplateLoading(resourceLoader
				.getResource(templateDirectory).getFile());
		DefaultObjectWrapper wrapper = new DefaultObjectWrapper();
		configuration.setObjectWrapper(wrapper);
		configuration.setDefaultEncoding("UTF-8");
		if (allSharedVariables != null && allSharedVariables.size() > 0) {
			TemplateHashModelEx hash = new SimpleMapModel(allSharedVariables,
					wrapper);
			configuration.setAllSharedVariables(hash);
		}
	}

	public Template getTemplate(String templateName) throws IOException {
		return configuration.getTemplate(templateName);
	}

	public Template getTemplate(String arg0, Locale arg1, String arg2,
			boolean arg3) throws IOException {
		return configuration.getTemplate(arg0, arg1, arg2, arg3);
	}

	public Template getTemplate(String arg0, Locale arg1, String arg2)
			throws IOException {
		return configuration.getTemplate(arg0, arg1, arg2);
	}

	public Template getTemplate(String arg0, Locale arg1) throws IOException {
		return configuration.getTemplate(arg0, arg1);
	}

	public Template getTemplate(String arg0, String arg1) throws IOException {
		return configuration.getTemplate(arg0, arg1);
	}

}
