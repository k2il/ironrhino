<#include "ec-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<@ecstart action="${entityName}"/>
	<#list naturalIds?keys as name>
		<#call ectheadtd name="${name}" editable=!naturalIdsImmatuable/>
	</#list>
	<#list formElements?keys as name>
		<#if !(naturalIds?keys?seq_contains(name))>
			<#call ectheadtd name="${name}" editable=!formElements[name].readonly/>
		</#if>
	</#list>
<@ecmiddle/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<@ectbodytrstart recordKey="${entity.id}" odd=(index%2==1)/>
	<#list naturalIds?keys as name>
		<#if !naturalIdsImmatuable>
		<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="input"/>
		<#else>
		<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}"/>
		</#if>
	</#list>
	<#list formElements?keys as name>
		<#if !(naturalIds?keys?seq_contains(name))>
			<#if !formElements[name].readonly>
				<#if formElements[name].type=='input'||formElements[name].type=='textarea'>
					<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="input"/>
				</#if>
				<#if formElements[name].type=='checkbox'>
					<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="select" cellEditTemplate="select_template_boolean"/>
				</#if>
				<#if formElements[name].type=='select'>
					<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="select" cellEditTemplate="select_template_${name}"/>
				</#if>
			<#else>
				<#call ectbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}"/>
			</#if>
		</#if>
	</#list>	
<@ectbodytrend  recordKey="${entity.id}"/>
</#list>
<@ecend/>

<div style="display: none">
<#list formElements?keys as key>
	<#if formElements[key].type=='select'>
		<textarea id="select_template_${key}">
	<select onblur="ECSideUtil.updateCell(this,'select')"
			style="width: 100%;" name="${entityName}.${key}">
			<#list formElements[key].enumValues as en>
			<option value="${en.name}">${en.displayName}</option>
			</#list>
	</select>
	</textarea>
	</#if>
</#list></div>
</body>
</html>
