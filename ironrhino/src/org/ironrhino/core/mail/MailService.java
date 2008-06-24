package org.ironrhino.core.mail;

import java.io.StringWriter;
import java.util.Map;

import org.ironrhino.common.support.TemplateProvider;
import org.ironrhino.core.jms.MessageProducer;
import org.springframework.mail.SimpleMailMessage;


import freemarker.template.Template;

/**
 * send asynchronized mail
 * 
 * @author zym
 * 
 */
public class MailService {

	private TemplateProvider templateProvider;

	private MessageProducer messageProducer;

	public void setMessageProducer(MessageProducer messageProducer) {
		this.messageProducer = messageProducer;
	}

	public void setTemplateProvider(TemplateProvider templateProvider) {
		this.templateProvider = templateProvider;
	}

	public void send(SimpleMailMessage smm) {
		send(smm, true);
	}

	public void send(SimpleMailMessage smm, boolean useHtmlFormat) {
		messageProducer
				.produce(new SimpleMailMessageWrapper(smm, useHtmlFormat));
	}

	public void send(SimpleMailMessage smm, String templateName, Map model) {
		send(smm, templateName, model, true);
	}

	public void send(SimpleMailMessage smm, String templateName, Map model,
			boolean useHtmlFormat) {
		model.put("sendmail", true);
		StringWriter writer = new StringWriter();
		try {
			Template template = templateProvider.getTemplate(templateName);
			template.process(model, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		smm.setText(writer.toString());
		messageProducer
				.produce(new SimpleMailMessageWrapper(smm, useHtmlFormat));
	}

}
