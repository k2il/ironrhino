<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('page')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"pagepath":{"alias":"path","width":"200px","template":"<a href=\"${getUrl(cmsPath)}$"+"{value}\" target=\"_blank\">$"+"{value}</a>"},"title":{},"displayOrder":{"width":"90px"},"tag":{"template":r"<#list entity.tags as tag><a href='<@url value='/common/page?keyword=tags:${tag}'/>'>${tag}</a>&nbsp;&nbsp;</#list>"},"createDate":{"width":"150px","template":r"<#if entity.createDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"},"modifyDate":{"width":"150px","template":r"<#if entity.modifyDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-view="input" data-windowoptions="{\'iframe\':true,\'width\':\'900px\'}">${action.getText("edit")}</button>
'>
<#assign bottomButtons='
<button type="button" class="btn" data-view="input" data-windowoptions="{\'iframe\':true,\'width\':\'900px\'}">${action.getText("create")}</button>
<button type="button" class="btn" data-action="delete">${action.getText("delete")}</button>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button>
'>
<@richtable entityName="page" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons celleditable=false searchable=true/>
</body>
</html></#escape>
