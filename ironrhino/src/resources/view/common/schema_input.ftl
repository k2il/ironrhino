<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if schema.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('schema')}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax" cssStyle="text-align:center;">
	<#if !schema.new>
		<@s.hidden name="schema.id" />
	</#if>
	<#if Parameters.brief??>
		<@s.hidden name="schema.name"/>
		<@s.hidden name="schema.description" />
	<@s.hidden name="schema.strict" />
		<#else>
		<div style="float:left;width:30%;"><span>${action.getText('name')}: </span><@s.textfield theme="simple" name="schema.name" cssClass="required checkavailable"/></div>
		<div style="float:left;width:40%;"><span>${action.getText('description')}: </span><@s.textfield theme="simple" name="schema.description" /></div>
		<div style="float:left;width:30%;"><span>${action.getText('strict')}: </span><@s.checkbox theme="simple" name="schema.strict" /></div>
	</#if>
	<table border="0" class="datagrid" style="width:100%;padding-top:10px;">
		<thead>
			<tr>
				<td>${action.getText('name')}</td>
				<td>${action.getText('value')}</td>
				<td>${action.getText('type')}</td>
				<td>${action.getText('required')}</td>
				<td>${action.getText('strict')}</td>
				<td></td>
			</tr>
		</thead>
		<tbody>
			<#assign size = 0>
			<#if schema.fields?? && schema.fields?size gt 0>
				<#assign size = schema.fields?size-1>
			</#if>
			<#list 0..size as index>
			<tr style="background-color:#F5F5F5;">
				<td><@s.textfield theme="simple" name="schema.fields[${index}].name" cssClass="required" cssStyle="width:120px;"/></td>
				<td>
					<table border="0" class="datagrid">
						<tbody>
							<#assign size = 0>
							<#if schema.fields[index]?? && schema.fields[index].values?? && schema.fields[index].values?size gt 0>
								<#assign size = schema.fields[index].values?size-1>
							</#if>
							<#list 0..size as index2>
							<tr>
								<td><@s.textfield theme="simple" name="schema.fields[${index}].values[${index2}]" cssStyle="width:150px;"/></td>
								<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
							</tr>
							</#list>
						</tbody>
					</table>
				</td>
				<td><@s.select theme="simple" name="schema.fields[${index}].type" cssStyle="width:60px;" list="@org.ironrhino.common.model.SchemaFieldType@values()" listKey="name" listValue="displayName"/></td>
				<td><@s.checkbox theme="simple" name="schema.fields[${index}].required"/></td>
				<td><@s.checkbox theme="simple" name="schema.fields[${index}].strict"/></td>
				<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


