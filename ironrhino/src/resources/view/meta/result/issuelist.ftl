<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText(name)}</title>
</head>
<body>
<ul class="breadcrumb">
	<li>
    	<a href="<@url value="/"/>">${action.getText('index')}</a> <span class="divider">/</span>
	</li>
	<li class="active">${action.getText(name)}</li>
</ul>
<div class="container-fluid issue ${name}">
<div id="list">
<ul class="unstyled">
<#list resultPage.result as page>
	<li><a href="<@url value="/${name}/p${page.path}"/>"><#if page.title??><#assign title=page.title?interpret><@title/></#if></a><span class="pull-right">${page.createDate?date}</span></li>
</#list>
</ul>
<@pagination class="ajax view history" replacement="list" cache="true"/>
</div>
</div>
</body>
</html></#escape>
