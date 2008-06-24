<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>

<div id="list" style="width: 100%"><ec:table tableId="ec"
	items="recordList" var="setting" editable="true" toolbarLocation="none"
	action="setting" title="settings" nearPageNum="0" resizeColWidth="true"
	width="100%" styleClass="sortable">
	<ec:row recordKey="${setting.id}">
		<ec:column width="22px" cell="checkbox" headerCell="checkbox"
			alias="deleteFlag" value="1" viewsAllowed="html" headerClass="nosort" />
		<ec:column property="key" cellName="setting.key" />
		<ec:column property="value" cellName="setting.value" cellEdit="input" />
		<ec:column property="action" width="140px" viewsAllowed="html"
			styleClass="include_if_edited" headerClass="nosort">
			<button type="button" onclick="ECSideX.save('${setting.id}')">保存</button>
			<button type="button" onclick="ECSideX.del('${setting.id}')">删除</button>
		</ec:column>
	</ec:row>
	<ec:extend>
		<div style="text-align: center; width: 100%; padding: 3px"
			class="eXtremeTable">
		<button type="button" onclick="ECSideX.input()">新增</button>
		<button type="button" onclick="ECSideX.reload()">刷新</button>
		<button type="button" onclick="ECSideX.save()">保存</button>
		<button type="button" onclick="ECSideX.del()">删除</button>
		</div>
	</ec:extend>
</ec:table></div>

<div id="template" style="display: none"><textarea
	id="ec_edit_template_input">
	<input type="text" class="inputtext" value=""
	onblur="ECSideUtil.updateCell(this,'input')" style="width: 100%;"
	name="" />
</textarea></div>
</body>
</html>
