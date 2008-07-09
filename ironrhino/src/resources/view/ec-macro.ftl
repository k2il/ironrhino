<#macro ecstart action="" includeCheckbox=true>
<form id="ec"  action="${action}"  method="post"  canResizeColWidth="true"  maxRowsExported="30000"  minColWidth="35"  style="visibility :hidden;" >
<div class="eXtremeTable"  id="ec_main_content"  style="width:100%;" >
<!-- ECS_AJAX_ZONE_PREFIX__begin_ ec_ECS_AJAX_ZONE_SUFFIX -->
<div>
<input type="hidden"  name="ec_i"  value="ec" />
<#if resultPage?exists>
<input type="hidden"  name="ec_crd"  value="${resultPage.pageSize}" />
<input type="hidden"  name="ec_p"  value="${resultPage.pageNo}" />
<input type="hidden"  name="ec_totalpages"  value="${resultPage.totalPage}" />
<input type="hidden"  name="ec_totalrows"  value="${resultPage.totalRecord}" />
<#else>
<input type="hidden"  name="ec_crd"  value="1000" />
<input type="hidden"  name="ec_p"  value="1" />
<input type="hidden"  name="ec_totalpages"  value="1" />
<input type="hidden"  name="ec_totalrows"  value="${Request.totalRows}" />
</#if>
<#list Parameters?keys as name>
<input type="hidden"  name="${name}"  value="${Parameters[name]}" />
</#list>
</div>

<table id="ec_table"  border="0"  cellspacing="0"  cellpadding="0"  class="sortable"  style="table-layout:fixed;"  width="100%"    >
	<thead id="ec_table_head" >

	<tr>
		<#if includeCheckbox>
		<td class="nosort"  width="22px" ><input type="checkbox"  title="全选/全消"  class="checkbox"  onclick="ECSideUtil.checkAll('ec','deleteFlag',this)" /></td>
		</#if>
</#macro>
<#macro ectheadtd name editable=true>
<td class="tableHeader <#if editable>editableColumn</#if>" ><span class="resizeTitle">${name}</span>
<span class="resizeBar"  onmousedown="ECSideUtil.StartResize(event,this,'ec');" onmouseup="ECSideUtil.EndResize(event);"></span></td>
</#macro>
<#macro ecmiddle width="200px">
<td class="nosort"  width="${width}" >Action</td>
	</tr>
	</thead>
	<tbody id="ec_table_body" >
</#macro>
<#macro ectbodytrstart recordKey,odd,includeCheckbox=true>
<tr class="${odd?string("odd","even")}"  onclick="ECSideUtil.selectRow('ec',this);"  onmouseover="ECSideUtil.lightRow('ec',this);"  onmouseout="ECSideUtil.unlightRow('ec',this);"    recordKey="${recordKey}" >
		<#if includeCheckbox><td width="22px"><input type="checkbox"  name="deleteFlag"  value="1"  class="checkbox"/></td></#if>
</#macro>
<#macro ectbodytd cellName,value,cellEdit="",cellEditTemplate="ec_edit_template_input",cellEditAction="ondblclick",class="">
<td <#if class!="">class="${class}"</#if> <#if cellEdit!="">${(cellEditAction!="")?string(cellEditAction,"ondblclick")}="ECSideUtil.editCell(this,'${cellEdit}','${(cellEditTemplate!="")?string(cellEditTemplate,"ec_edit_template_input")}')"</#if>  cellName="${cellName}" >${value}</td>
</#macro>
<#macro ectbodytrend recordKey width="200px" buttons="">
<td class="include_if_edited"  width="${width}"  cellName="action"   >
			<#if buttons!="">
			${buttons?replace("#id",recordKey)}
			<#else>
			<button type="button" onclick="ECSideX.save('${recordKey}')">保存</button>
			<button type="button" onclick="ECSideX.input('${recordKey}')">编辑</button>
			<button type="button" onclick="ECSideX.del('${recordKey}')">删除</button>
			</#if>
		</td>
</tr>
</#macro>
<#macro ecend buttons="">
	</tbody>

</table><iframe style="border:0px;" marginwidth="0" marginheight="0" frameborder="0" border="0" width="0" height="0" id="ec_ecs_export_iframe" name="ec_ecs_export_iframe" ></iframe>
<div class="toolbar"  style="width:100%;" >
	<table class="toolbarTable"  cellpadding="0"  cellspacing="0" >
	<tr>
	<#if resultPage?exists>
	<td class="pageNavigationTool"  nowrap="nowrap" >
	<input type="button" <#if resultPage.pageNo==1>disabled="disabled"  class="pageNav firstPageD"<#else>class="pageNav firstPage"</#if>  onclick="ECSideUtil.gotoPage('ec',1);"  title="第一页" />
	<input type="button" <#if resultPage.pageNo==1>disabled="disabled"  class="pageNav prevPageD"<#else>class="pageNav prevPage"</#if> onclick="ECSideUtil.gotoPage('ec',${resultPage.pageNo-1});"  title="上一页" />
	<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled"  class="pageNav nextPageD"<#else>  class="pageNav nextPage"</#if>  onclick="ECSideUtil.gotoPage('ec',${resultPage.pageNo+1});"  title="下一页" />
	<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled"  class="pageNav lastPageD"<#else>class="pageNav lastPage"</#if>  onclick="ECSideUtil.gotoPage('ec',${resultPage.totalPage});"  title="最末页"/>
	</td>
	<td class="separatorTool" >&#160;</td>
	<td class="pageJumpTool"  nowrap="nowrap" >
	<input type="button"  class="pageNav jumpPage"  onclick="ECSideUtil.gotoPageByInput('ec',this);" />
	<input type="text"  name="ec_pg"  value="${resultPage.pageNo}"  class="jumpPageInput"  onkeydown="if (event.keyCode && event.keyCode==13 ) {ECSideUtil.gotoPageByInput('ec',this);;return false; } " />/${resultPage.totalPage}页</td>
	<td class="separatorTool" >&#160;</td>
	<td class="pageSizeTool"  nowrap="nowrap" >每页<select name="ec_rd"  onchange="ECSideUtil.changeRowsDisplayed('ec',this);" >
				<#assign array=[5,10,20,50,100,500]>
				<#list array as ps>
				<option value="${ps}" <#if resultPage.pageSize==ps>selected</#if>>${ps}</option>
				</#list> 
				<option value="${resultPage.totalRecord}" <#if resultPage.pageSize==resultPage.totalRecord>selected</#if>>全部</option>
				</select>条</td>
	<td class="separatorTool" >&#160;</td>
	<td class="exportTool" ></td>
	<td class="separatorTool" >&#160;</td>
	<#else>
	<td class="pageNavigationTool"></td>
	<td class="separatorTool"></td>
	<td class="pageJumpTool"></td>
	<td class="separatorTool"></td>
	<td class="pageSizeTool"></td>
	<td class="separatorTool" ></td>
	<td class="exportTool"></td>
	<td class="separatorTool"></td>
	</#if>
	<td class="extendTool" >
		<div style="text-align: center; width: 100%; padding: 3px"
			class="eXtremeTable">
			<#if buttons!="">
			${buttons}
			<#else>
				<button type="button" onclick="ECSideX.input()">新增</button>
				<button type="button" onclick="ECSideX.reload()">刷新</button>
				<button type="button" onclick="ECSideX.save()">保存</button>
				<button type="button" onclick="ECSideX.del()">删除</button>
			</#if>
		</div>
	</td>
	<td class="separatorTool" >&#160;</td>
	<#if resultPage?exists>
	<td class="statusTool" >共${resultPage.totalRecord}条记录,显示${resultPage.start+1}到${resultPage.start+resultPage.result?size}</td>
	<#else>
	<td class="statusTool" >共${Request.totalRows}条记录,显示1到${Request.totalRows}</td>	
	</#if>
	</tr>
	</table></div>
<!-- ECS_AJAX_ZONE_PREFIX_ _end_ec_ECS_AJAX_ZONE_SUFFIX -->
</div>
</form>
<div style="display: none;"><textarea
	id="ec_edit_template_input">
	<input type="text" class="inputtext" value=""
	onblur="ECSideUtil.updateCell(this,'input')" style="width: 100%;"
	 />
</textarea>
<textarea id="select_template_boolean">
	<select onblur="ECSideUtil.updateCell(this,'select')"
	style="width: 100%;">
	<option value="true">true</option>
	<option value="false">false</option>
</select>
</textarea>
</div>
</#macro>

<#macro ectable config entityName actionColumnWidth="200px" actionColumnButtons="" bottomButtons="">
<@ecstart action="${entityName}"/>
	<#list config?keys as name>
		<#call ectheadtd name="${name}" editable=(config[name]["cellEdit"]?exists)/>
	</#list>
<@ecmiddle width="${actionColumnWidth}"/>
<#assign index=0>
<#assign list=Request.recordList>
<#if !(list?exists)>
<#assign list=resultPage.result>
</#if>
<#list list as entity>
<#assign index=index+1>
<@ectbodytrstart recordKey="${entity.id}" odd=(index%2==1)/>
	<#list config?keys as name>
		<#if config[name]["cellEdit"]?exists>
		<#assign edit=config[name]["cellEdit"]?split(",")>
		<#call ectbodytd cellName="${entityName}.${name}" value="${config[name]['value']?exists?string(config[name]['value']?if_exists,entity[name]?if_exists?string)}"
		 cellEdit="${edit[0]}" cellEditTemplate="${edit[1]?if_exists}" cellEditAction="${edit[2]?if_exists}" class="${config[name]['class']?if_exists}"/>
		<#else>
		<#call ectbodytd cellName="${entityName}.${name}" value="${config[name]['value']?exists?string(config[name]['value']?if_exists,entity[name]?if_exists?string)}"  class="${config[name]['class']?if_exists}"/>
		</#if>
	</#list>
<@ectbodytrend  recordKey="${entity.id}" width="${actionColumnWidth}" buttons="${actionColumnButtons}"/>
</#list>
<@ecend buttons="${bottomButtons}"/>
</#macro>

