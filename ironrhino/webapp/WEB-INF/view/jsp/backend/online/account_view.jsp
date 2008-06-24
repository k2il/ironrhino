<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN"><head>
<title>ironrhino</title>
</head>
<body>
<table>
	<tr>
		<td><s:property value="getText('account.username')" /></td>
		<td><s:property value="account.username" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.name')" /></td>
		<td><s:property value="account.name" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.email')" /></td>
		<td><s:property value="account.email" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.enabled')" /></td>
		<td><s:property value="account.enabled" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.locked')" /></td>
		<td><s:property value="account.locked" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.createDate')" /></td>
		<td><s:property value="account.createDate" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.roles')" /></td>
		<td><s:property value="account.rolesAsString" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('account.groups')" /></td>
		<td><s:property value="account.groupsAsString" /></td>
	</tr>
</table>
</body>
</html>
