<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<table>
	<tr>
		<td><s:property value="getText('user.username')" /></td>
		<td><s:property value="user.username" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.name')" /></td>
		<td><s:property value="user.name" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.email')" /></td>
		<td><s:property value="user.email" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.description')" /></td>
		<td><s:property value="user.description" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.enabled')" /></td>
		<td><s:property value="user.enabled" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.locked')" /></td>
		<td><s:property value="user.locked" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.createDate')" /></td>
		<td><s:property value="user.createDate" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.accountExpireDate')" /></td>
		<td><s:property value="user.accountExpireDate" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('user.passwordExpireDate')" /></td>
		<td><s:property value="user.passwordExpireDate" /></td>
	</tr>
</table>
<div><s:property value="getText('user.roles')" /> <br />
<s:iterator value="user.roles">
	<s:property value="name" />
</s:iterator></div>
<div><s:property value="getText('user.groups')" /><br />
<s:iterator value="user.groups">
	<s:property value="name" />
</s:iterator></div>
</body>
</html>
