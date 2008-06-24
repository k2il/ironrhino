<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
<link rel="stylesheet" href="<s:url value="/themes/ui.datepicker.css"/>"
	type="text/css" media="screen" />
<script type="text/javascript"
	src="<s:url value="/scripts/ui.datepicker.js"/>"></script>
<script type="text/javascript"
	src="<s:url value="/scripts/ui.datepicker-zh-CN.js"/>"></script>
</head>
<body>
<s:set var="isNew" value="%{isNew()}" />
<s:form action="save" method="post" cssClass="ajax">
	<s:hidden name="%{entityName+'.id'}" />
	<s:iterator value="%{naturalIds.keySet()}">
		<s:set var="config" value="%{formElements[top]}" />
		<s:textfield label="%{top}" name="%{entityName+'.'+top}"
			readonly="%{naturalIdsImmatuable&&!#isNew}"
			cssClass="%{#config.cssClass}"
			size="%{#config.size>0?#config.size:20}" />
	</s:iterator>

	<s:iterator value="%{formElements}">
		<s:if test="%{!naturalIds.containsKey(top.key)}">
			<s:set var="config" value="%{top.value}" />
			<s:if test="%{#config.type=='input'}">
				<s:textfield label="%{top.key}" name="%{entityName+'.'+top.key}"
					readonly="#config.readonly" cssClass="%{#config.cssClass}"
					size="%{#config.size>0?#config.size:20}" />
			</s:if>
			<s:if test="%{#config.type=='textarea'}">
				<s:textarea label="%{top.key}" name="%{entityName+'.'+top.key}"
					readonly="%{#config.readonly}" cssClass="%{#config.cssClass}"
					cols="50" rows="10" />
			</s:if>
			<s:if test="%{#config.type=='checkbox'}">
				<s:checkbox label="%{top.key}" name="%{entityName+'.'+top.key}"
					cssClass="%{#config.cssClass}" />
			</s:if>
			<s:if test="%{#config.type=='select'}">
				<s:select label="%{top.key}" name="%{entityName+'.'+top.key}"
					list="%{#config.enumValues}" listKey="name" listValue="displayName" />
			</s:if>
		</s:if>
	</s:iterator>
	<s:submit value="Save" />

</s:form>
</body>
</html>


