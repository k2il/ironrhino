<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<s:if test="%{authRequest!=null}">
	<form id="openid-form-redirection"
		action="<s:property value="authRequest.OPEndpoint"/>" method="post"
		accept-charset="utf-8"><s:iterator
		value="authRequest.parameterMap">
		<input type="hidden" name="<s:property value="key"/>"
			value="<s:property value="value"/>" />
	</s:iterator>
	<button type="submit">Continue...</button>
	</form>
	<script>document.getElementById('openid-form-redirection').submit();</script>
</s:if>
</body>
</html>
