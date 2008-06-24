package org.ironrhino.core.mail;

import org.ironrhino.core.jms.MessageConsumer;
import org.springframework.beans.factory.annotation.Required;


public class SimpleMailMessageConsumer implements MessageConsumer {

	private MailSender mailSender;

	@Required
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void consume(Object object) {
		SimpleMailMessageWrapper smmw = (SimpleMailMessageWrapper) object;
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

	public boolean supports(Class clazz) {
		return (SimpleMailMessageWrapper.class.isAssignableFrom(clazz));
	}

}
