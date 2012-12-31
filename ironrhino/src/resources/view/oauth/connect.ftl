<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('login')}</title>
</head>
<body>
<#if providers??>
<div class="row">
<div class="span4 offset4">
<div class="row">
<#list providers as var>
<div class="span2" style="height:100px;">
<a href="${request.requestURL}?id=${var.name}<#if targetUrl??>&targetUrl=${targetUrl?url}</#if>">
	<img src="${var.logo}" alt="${action.getText(var.name)}" title="${action.getText(var.name)}"/>
</a>
</div>
</#list>
</div>
</div>
</div>
</#if>
</body>
</html></#escape>
