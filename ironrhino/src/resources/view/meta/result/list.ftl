<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List ${entityName?cap_first}</title>
</head>
<body>
<@rtstart action=entityName readonly=readonly/>
	<#list naturalIds?keys as key>
		<#assign config=uiConfigs[key]>
		<#assign label=key>
		<#if config.displayName??>
			<#assign label=config.displayName>
		</#if>
		<@rttheadtd name=label editable=!(readonly||naturalIdsImmatuable)/>
	</#list>
	<#list uiConfigs?keys as key>
		<#if !(naturalIds?keys?seq_contains(key))>
			<#assign config=uiConfigs[key]>
			<#assign label=key>
			<#if config.displayName??>
				<#assign label=config.displayName>
			</#if>
			<@rttheadtd name=label editable=!(readonly||uiConfigs[key].readonly)/>
		</#if>
	</#list>
<@rtmiddle readonly=readonly/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<@rttbodytrstart rowid=entity.id! odd=(index%2==1) readonly=readonly/>
	<#list naturalIds?keys as key>
		<#if !(readonly||naturalIdsImmatuable)>
		<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key] template='${uiConfigs[key].template}' cellEdit='input'/>
		<#else>
		<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key] template='${uiConfigs[key].template}'/>
		</#if>
	</#list>
	<#list uiConfigs?keys as key>
		<#if !(naturalIds?keys?seq_contains(key))>
			<#if !(readonly||uiConfigs[key].readonly)>
				<#if uiConfigs[key].type=='input'||uiConfigs[key].type=='textarea'>
					<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key]! template='${uiConfigs[key].template}' cellEdit='input'/>
				</#if>
				<#if uiConfigs[key].type=='checkbox'>
					<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key]! template='${uiConfigs[key].template}' cellEdit='select,select_template_boolean'/>
				</#if>
				<#if uiConfigs[key].type=='select'>
					<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key]! template='${uiConfigs[key].template}' cellEdit='select,select_template_${key}'/>
				</#if>
			<#else>
				<@rttbodytd entity=entity cellName='${entityName}.${key}' value=entity[key]! template='${uiConfigs[key].template}'/>
			</#if>
		</#if>
	</#list>	
<@rttbodytrend  rowid=entity.id readonly=readonly/>
</#list>
<@rtend readonly=readonly/>
<#if !readonly>
<div style="display: none">
<#list uiConfigs?keys as key>
	<#if uiConfigs[key].type=='select'>
		<textarea id="select_template_${key}">
	<select onblur="ECSideUtil.updateCell(this,'select')"
			style="width: 100%;" name="${entityName}.${key}">
			<#list uiConfigs[key].enumValues as en>
			<option value="${en.getName()}">${en.displayName}</option>
			</#list>
	</select>
	</textarea>
	</#if>
</#list></div>
</#if>
</body>
</html></#escape>
