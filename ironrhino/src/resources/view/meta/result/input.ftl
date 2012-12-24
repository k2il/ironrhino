<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if !entity??><#assign entity=entityName?eval></#if><#if entity.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText(entityName)}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax form-horizontal">
	<#if !entity.new>
	<@s.hidden name="${entityName}.id" />
	</#if>
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#assign label=key>
		<#if config.alias??>
			<#assign label=config.alias>
		</#if>
		<#if naturalIds?keys?seq_contains(key)>
			<#assign readonly=!naturalIdMutable&&!action.isNew()>
		<#else>
			<#assign readonly=config.readonly>
		</#if>
		<#if config.type=='textarea'>
			<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cssStyle="width:400px;height:150px;" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes/>
		<#elseif config.type=='checkbox'>
			<#if !readonly>
				<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass+config.cssClass?has_content?string(' ','')}custom" dynamicAttributes=config.dynamicAttributes />
			<#else>
				<@s.hidden name="${entityName}.${key}" />
				<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" disabled="true" dynamicAttributes=config.dynamicAttributes />
			</#if>
		<#elseif config.type=='select'>
			<#if !readonly>
				<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" dynamicAttributes=config.dynamicAttributes/>
			<#else>
				<@s.hidden name="${entityName}.${key}" value="%{${entityName+'.'+key+'.id'}}"/>
				<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" disabled="true" dynamicAttributes=config.dynamicAttributes />
			</#if>
		<#elseif config.type=='listpick'>
			<#if !readonly>
				<div class="control-group listpick" data-options="{'url':'<@url value="${config.pickUrl}"/>','name':'#${key}','id':'#${key}Id'}">
				<#if config.required>
					<@s.hidden id="${key}Id" name="${entityName}.${key}.id" cssClass="required"/>
				<#else>
					<@s.hidden id="${key}Id" name="${entityName}.${key}.id"/>
				</#if>
					<label class="control-label" for="${key}">${action.getText(label)}</label>
					<div class="controls">
					<span id="${key}"><#if entity[key]??><#if entity[key].fullname??>${entity[key].fullname!}<#else>${entity[key]!}</#if><a class="remove" href="#">&times;</a><#else>...</#if></span>
					</div>
				</div>
			<#else>
				<div class="control-group">
					<label class="control-label" for="${key}">${action.getText(label)}</label>
					<div class="controls">
					<span id="${key}">${entity[key]!}</span>
					</div>
				</div>
			</#if>
		<#elseif config.type=='dictionary' && selectDictionary??>
			<div class="control-group">
			<label class="control-label" for="${key}">${action.getText(label)}</label>
			<div class="controls">
			<#if !readonly>
				<@selectDictionary dictionaryName=evalTemplate(config.templateName) id=key name="${entityName}.${key}" value="${entity[key]!}" required=config.required class="${config.cssClass}" dynamicAttributes=config.dynamicAttributes/>
			<#else>
				<@s.hidden name="${entityName}.${key}"/>
				<span id="${key}"><@displayDictionaryLabel dictionaryName=evalTemplate(config.templateName) value="${entity[key]!}"/></span>
			</#if>
			</div>
			</div>
		<#elseif config.type=='schema'>
			<#if editAttributes??>
				<div id="editAttributes">
				<@editAttributes schemaName=evalTemplate(config.templateName) attributes=entity.attributes parameterNamePrefix=entityName+'.'/>
				</div>
			</#if>
		<#else>
			<#if config.maxlength gt 0>
				<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" type="${(config.inputType!)}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" maxlength="${(config.maxlength)}" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes />
			<#else>
				<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" type="${(config.inputType!)}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes />
			</#if>
		</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>