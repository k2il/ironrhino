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
	items="recordList" var="role" editable="true" action="role"
	title="roles" nearPageNum="0" resizeColWidth="true" width="100%"
	styleClass="sortable">
	<ec:row recordKey="${role.id}">
		<ec:column width="22px" cell="checkbox" headerCell="checkbox"
			alias="deleteFlag" value="1" viewsAllowed="html" headerClass="nosort" />
		<ec:column property="name" cellName="role.name" />
		<ec:column property="enabled" cellName="role.enabled"
			cellEdit="select,select_template_enabled,onclick" />
		<ec:column property="description" cellName="role.description"
			cellEdit="input" />
		<ec:column property="action" width="140px" viewsAllowed="html"
			styleClass="include_if_edited" headerClass="nosort">
			<button type="button" onclick="ECSideX.save('${role.id}')">保存</button>
			<button type="button" onclick="ECSideX.del('${role.id}')">删除</button>
		</ec:column>
	</ec:row>
	<ec:extend location="bottom">
		<div style="text-align: center; width: 100%; padding: 3px"
			class="eXtremeTable">
		<button type="button" onclick="ECSideX.input()">新增</button>
		<button type="button" onclick="ECSideX.reload()">刷新</button>
		<button type="button" onclick="ECSideX.save()">保存</button>
		<button type="button" onclick="ECSideX.del()">删除</button>
		</div>
	</ec:extend>
</ec:table></div>
<div id="template" style="display: none;"><textarea
	id="ec_edit_template_input">
	<input type="text" class="inputtext" value=""
	onblur="ECSideUtil.updateCell(this,'input')" style="width: 100%;"
	name="" />
</textarea> <textarea id="select_template_enabled">
	<select onblur="ECSideUtil.updateCell(this,'select')"
	style="width: 100%;" name="role.enabled">
	<option value="true">true</option>
	<option value="false">false</option>
</select>
</textarea></div>
</body>
</html>
