<#macro editAttributes schema={} schemaName="" attributes=[] parameterNamePrefix="">
	<#if schemaName!="" && !schema.name??>
		<#local schema=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('schemaManager').findByNaturalId(true,[schemaName])!>
	</#if>
	<table border="0" class="datagrid" style="width:100%;">
		<thead>
			<tr>
				<td>${action.getText('name')}</td>
				<td>${action.getText('value')}</td>
				<td></td>
			</tr>
		</thead>
		<tbody>
			<#if schema?? && schema.name??>
				<#local index = 0>
				<#list schema.fields as field>
				<#local persistValueExists=false>
				<#list attributes as attr>
					<#if attr.name==field.name>
						<#local persistValue=attr.value>
						<#local persistValueExists=true>
						<#break/>
					</#if>
				</#list>
				<tr>
					<td><@s.textfield theme="simple" name="${parameterNamePrefix}attributes[${index}].name" value="${field.name}" readonly=field.strict?string/></td>
					<td>
						<select name="${parameterNamePrefix}attributes[${index}].value" class="<#if field.required> required</#if><#if !field.strict> combox</#if>">
							<option value="">${action.getText('select')}</option>
							<#list field.values as value>
							<option value="${value}"<#if persistValueExists && persistValue=value> selected="selected"</#if>>${value}</option>
							</#list>
							<#if !field.strict && persistValueExists && persistValue!='' && !field.values?seq_contains(persistValue)>
							<option value="${persistValue}" selected="selected">${persistValue}</option>
							</#if>
						</select>
					</td>
					<td><#if !schema.strict><@button text="+" class="add"/><@button text="-" class="remove"/></#if><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/>
					</td>
				</tr>
				<#local index = index+1>
				</#list>
				<#if !schema.strict>
				<#list attributes as attr>
					<#local inschema=false>
					<#list schema.fields as field>
						<#if attr.name==field.name>
							<#local inschema=true>
							<#break/>
						</#if>
					</#list>
					<#if !inschema>
					<tr>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].name" value="${attr.name!}"/></td>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].value" value="${attr.value!}"/></td>
						<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
					</tr>
					<#local index = index+1>
					</#if>
				</#list>
				</#if>
				<#else>
					<#assign size = 0>
					<#local isnew=true>
					<#if attributes?? && attributes?size gt 0>
						<#assign size = attributes?size-1>
						<#local isnew=false>
					</#if>
					<#list 0..size as index>
					<tr>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].name"<#if !isnew> value="${attributes[index].name!}</#if>"/></td>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].value"<#if !isnew> value="${attributes[index].value!}</#if>"/></td>
						<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
					</tr>
					</#list>
				</#if>
		</tbody>
	</table>
</#macro>