<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="authz"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
<%@ include file="../commons/meta.jsp"%>

</head>
<body>
<authz:authorize ifAllGranted="ROLE_BUILTIN_USER">
	<div><s:url id="url" action="logout" /> <s:a href="%{url}">Logout</s:a></div>
</authz:authorize>
<authz:authorize ifAllGranted="ROLE_SUPERVISOR">
	<div><s:url id="url" action="switchUser" /> <s:a href="%{url}">Switch User</s:a></div>
</authz:authorize>
<authz:authorize ifAllGranted="ROLE_PREVIOUS_ADMINISTRATOR">
	<div><s:url id="url" action="exitUser" /> <s:a href="%{url}">Exit User</s:a></div>
</authz:authorize>
<authz:authorize ifAllGranted="ROLE_SUPERVISOR">
	<div><s:url id="url" action="securityConfig" /> <s:a
		href="%{url}">Security Config</s:a></div>
	<div><s:url id="url" action="controlPanel" /><s:a href="%{url}">Control Panel</s:a></div>
</authz:authorize>
</body>
</html>
