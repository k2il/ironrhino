package org.ironrhino.core.ext.spring;

import java.util.Map;

public interface ApplicationContextConsole {

	public Object execute(String cmd) throws Exception;

	public void set(String path, String value) throws Exception;

	public Object get(String path) throws Exception;

	public Object call(String path, String[] params) throws Exception;

	public Object callWithMap(String path, Map<String, String> properties)
			throws Exception;

}