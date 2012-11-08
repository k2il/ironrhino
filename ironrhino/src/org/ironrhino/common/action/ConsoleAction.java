package org.ironrhino.common.action;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig
public class ConsoleAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	private static Logger log = LoggerFactory.getLogger(ConsoleAction.class);

	private String expression;

	private boolean global = false;

	private Object result;

	@Inject
	private transient ApplicationContextConsole applicationContextConsole;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public Object getResult() {
		return result;
	}

	@Override
	@InputConfig(resultName = "success")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "expression", trim = true, key = "validation.required") })
	public String execute() {
		try {
			result = applicationContextConsole.execute(expression, global);
			addActionMessage(getText("operate.success") + ":"
					+ JsonUtils.toJson(result));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			addActionError(getText("error") + ":" + e.getMessage());
		}
		return SUCCESS;
	}

	@InputConfig(resultName = "success")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "expression", trim = true, key = "validation.required") })
	@JsonConfig(root = "result")
	public String executeJson() {
		try {
			result = applicationContextConsole.execute(expression, global);
		} catch (Exception e) {
			Throwable throwable = e;
			if (e instanceof InvocationTargetException)
				throwable = ((InvocationTargetException) e)
						.getTargetException();
			log.error(throwable.getMessage(), throwable);
			addActionError(getText("error") + ":" + throwable.getMessage());
			Map<String, String[]> map = new HashMap<String, String[]>();
			map.put("actionErrors", new String[] { throwable.getMessage() });
			result = map;
		}
		return JSON;
	}
}
