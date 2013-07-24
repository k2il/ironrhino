<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('user')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"username":{},"name":{},"email":{},"enabled":{"width":"80px"}}>
<#assign bottomButtons=r'
<button type="button" class="btn" data-action="delete" data-shown="selected" data-filterselector="[data-enabled='+'\'false\''+r'"]">${action.getText("delete")}</button>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button>
'>
<@richtable entityName="user" columns=columns searchable=true celleditable=false bottomButtons=bottomButtons rowDynamicAttributes=r"{'data-enabled':'${entity.enabled?string}'}"/>
</body>
</html></#escape>