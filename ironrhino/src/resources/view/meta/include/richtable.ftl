<#macro richtable columns entityName formid='' action='' actionColumnWidth='50px' actionColumnButtons='' bottomButtons='' resizable=true sortable=true readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' includeParameters=true showPageSize=true showCheckColumn=true multipleCheck=true columnfilterable=true>
<@rtstart formid=formid action=action entityName=entityName readonly=readonly resizable=resizable sortable=sortable includeParameters=includeParameters showCheckColumn=showCheckColumn multipleCheck=multipleCheck columnfilterable=columnfilterable>
<#nested/>
</@rtstart>
<#local index = 0>
<#local size = columns?keys?size>
<#list columns?keys as name>
<#local index = index+1>
<#local cellName=((columns[name]['trimPrefix']??)?string('',entityName+'.'))+name>
<@rttheadtd name=name alias=columns[name]['alias']! title=columns[name]['title']! class=columns[name]['class']! width=columns[name]['width']! cellName=cellName cellEdit=columns[name]['cellEdit'] readonly=readonly resizable=(readonly&&index!=size||!readonly)&&resizable excludeIfNotEdited=columns[name]['excludeIfNotEdited']!false/>
</#list>
<@rtmiddle width=actionColumnWidth readonly=readonly/>
<#local index=0>
<#if resultPage??><#local list=resultPage.result></#if>
<#list list as entity>
<#local index=index+1>
<@rttbodytrstart entity=entity readonly=readonly showCheckColumn=showCheckColumn multipleCheck=multipleCheck/>
<#list columns?keys as name>
	<#if columns[name]['value']??>
	<#local value=columns[name]['value']>
	<#else>
	<#if !name?contains('.')>
		<#local value=entity[name]!>
	<#else>
		<#local value=('entity.'+name)?eval!>
	</#if>
	</#if>
	<#assign dynamicAttributes=columns[name]['dynamicAttributes']!>
	<@rttbodytd entity=entity value=value celleditable=columns[name]['cellEdit']?? template=columns[name]['template']! dynamicAttributes=dynamicAttributes/>
</#list>
<@rttbodytrend entity=entity buttons=actionColumnButtons readonly=readonly/>
</#list>
<@rtend buttons=bottomButtons readonly=readonly createable=createable celleditable=celleditable deleteable=deleteable searchable=searchable searchButtons=searchButtons showPageSize=showPageSize/>
</#macro>

<#macro rtstart formid='',action='',entityName='',readonly=false,resizable=true,sortable=true,includeParameters=true showCheckColumn=true multipleCheck=true columnfilterable=true>
<#local action=action?has_content?string(action,request.requestURI?substring(request.contextPath?length))>
<form id="<#if formid!=''>${formid}<#else>${entityName}_form</#if>" action="${getUrl(action)}" method="post" class="richtable ajax view"<#if actionBaseUrl!=action> data-actionBaseUrl="${actionBaseUrl}"</#if><#if entityName!=action&&entityName!=''> data-entity="${entityName}"</#if>>
<#nested/>
<#if includeParameters>
<#list Parameters?keys as name>
<#if name!='_'&&name!='pn'&&name!='ps'&&!name?starts_with('resultPage.')&&name!='keyword'&&name!='check'>
<input type="hidden" name="${name}" value="${Parameters[name]}" />
</#if>
</#list>
</#if>
<table class="table table-hover table-striped table-bordered richtable<#if sortable> sortable</#if><#if columnfilterable> filtercolumn</#if><#if resizable> resizable</#if>"<#if resizable> data-minColWidth="60"</#if>>
<thead>
<tr>
<#if showCheckColumn>
<th class="nosort <#if multipleCheck>checkbox<#else>radio</#if>" style="width:40px;"><#if multipleCheck><input type="checkbox" class="checkbox custom"/></#if></th>
</#if>
</#macro>

<#macro rttheadtd name,alias='',title='',cellName='',cellEdit='',class='',width='',readonly=false,resizable=true,excludeIfNotEdited=false>
<th<#if title!=''> title="${action.getText(title)}"</#if><#if excludeIfNotEdited||class!=''> class="<#if excludeIfNotEdited> excludeIfNotEdited</#if><#if class!=''> ${class}</#if>"</#if><#if width!=''> style="width:${width};"</#if><#if !readonly> data-cellName="${cellName}"</#if><#if cellEdit!=''> data-cellEdit="${cellEdit}"</#if>>
<#if resizable>
<span class="resizeTitle"><#if alias!=''>${action.getText(alias)}<#else>${action.getText(name)}</#if></span>
<span class="resizeBar"></span>
<#else>
<#if alias!=''>${action.getText(alias)}<#else>${action.getText(name)}</#if>
</#if>
</th>
</#macro>
<#macro rtmiddle width='50px' readonly=false>
<#if !readonly>
<th class="nosort" style="width:${width};"></th>
</#if>
</tr>
</thead>
<tbody>
</#macro>

<#macro rttbodytrstart entity readonly=false showCheckColumn=true multipleCheck=true>
<tr<#if !showCheckColumn&&entity.id??> data-rowid="${entity.id?string}"</#if>>
<#if showCheckColumn><td class="<#if multipleCheck>checkbox<#else>radio</#if>"><input type="<#if multipleCheck>checkbox<#else>radio</#if>" name="check"<#if entity.id??> value="${entity.id?string}"</#if> class="custom"/></td></#if>
</#macro>

<#macro rttbodytd value,entity,celleditable=true,template='',dynamicAttributes...>
<td<#if celleditable><#if value??><#if value?is_boolean> data-cellvalue="${value?string}"</#if><#if value?is_hash&&value.displayName??> data-cellvalue="${value.name()}"</#if></#if></#if><#list dynamicAttributes?keys as attr><#if attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>><#rt>
<#if template==''>
<#if value??>
<#if value?is_boolean>
${action.getText(value?string)}<#t>
<#else>
<#if value?is_hash&&value.displayName??>
${value.displayName}<#t>
<#else>
${value?xhtml}<#t>
</#if>
</#if>
</#if>
<#else>
<#local temp=template?interpret>
<@temp/><#t>
</#if>
</td>
</#macro>

<#macro rttbodytrend entity buttons='' readonly=false>
<#if !readonly>
<td class="action">
<#if buttons!=''>
<#local temp=buttons?interpret>
<@temp/>
<#else>
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button>
</#if>
</td>
</#if>
</tr>
</#macro>

<#macro rtend buttons='' readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' showPageSize=true>
</tbody>
</table>
<div class="toolbar row-fluid">
<div class="pagination span4">
<#if resultPage??>
<ul>
<#if resultPage.first>
<li class="disabled firstPage"><a title="${action.getText('firstpage')}"><i class="icon-fast-backward"></i></a></li>
<li class="disabled"><a title="${action.getText('previouspage')}"><i class="icon-step-backward"></i></a></li>
<#else>
<li class="firstPage"><a title="${action.getText('firstpage')}" href="${resultPage.renderUrl(1)}"><i class="icon-fast-backward"></i></a></li>
<li class="prevPage"><a title="${action.getText('previouspage')}" href="${resultPage.renderUrl(resultPage.previousPage)}"><i class="icon-step-backward"></i></a></li>
</#if>
<#if resultPage.last>
<li class="disabled"><a title="${action.getText('nextpage')}"><i class="icon-step-forward"></i></a></li>
<li class="disabled lastPage"><a title="${action.getText('lastpage')}"><i class="icon-fast-forward"></i></a></li>
<#else>
<li class="nextPage"><a title="${action.getText('nextpage')}" href="${resultPage.renderUrl(resultPage.nextPage)}"><i class="icon-step-forward"></i></a></li>
<li class="lastPage"><a title="${action.getText('lastpage')}" href="${resultPage.renderUrl(resultPage.totalPage)}"><i class="icon-fast-forward"></i></a></li>
</#if>
<li>
<span class="input-append">
    <input type="text" name="resultPage.pageNo" value="${resultPage.pageNo}" class="inputPage" title="${action.getText('currentpage')}"/><span class="add-on totalPage"><span class="divider">/</span><strong title="${action.getText('totalpage')}">${resultPage.totalPage}</strong></span>
</span>
<#if showPageSize>
<li class="hidden-tablet">
<select name="resultPage.pageSize" class="pageSize" title="${action.getText('pagesize')}">
<#local array=[5,10,20,50,100,500]>
<#local selected=false>
<#list array as ps>
<option value="${ps}"<#if resultPage.pageSize==ps><#local selected=true> selected</#if>>${ps}</option>
</#list>
<#if resultPage.canListAll>
<option value="${resultPage.totalResults}"<#if !selected && resultPage.pageSize==resultPage.totalResults> selected</#if>>${action.getText('all')}</option>
</#if>
</select>
</li>
</#if>
</#if>
</ul>
</div>
<div class="action span4">
<#if buttons!=''>
<#local temp=buttons?interpret>
<@temp/>
<#else>
<#if !readonly>
<#if createable><button type="button" class="btn" data-view="input">${action.getText("create")}</button></#if>
<#if celleditable><button type="button" class="btn" data-action="save">${action.getText("save")}</button></#if>
<#if deleteable><button type="button" class="btn" data-action="delete">${action.getText("delete")}</button></#if>
</#if><button type="button" class="btn" data-action="reload">${action.getText("reload")}</button></#if>
</div>
<div class="search span2">
<#if searchable>
<span class="input-append">
    <input type="text" name="keyword" value="${keyword!?html}" class="focus" placeholder="${action.getText('search')}"/><span class="add-on hidden-tablet"><i class="icon-search"></i></span>
</span>
</#if>
<#if searchButtons!=''>
<#local temp=searchButtons?interpret>
<@temp/>
<#else>
</#if>
</div>
<div class="status span2">
<span>
<#if resultPage??>
${resultPage.totalResults}${action.getText('record')}
<#else>
${list?size}${action.getText('record')}
</#if>
</span>
</div>
</div>
</form>
</#macro>