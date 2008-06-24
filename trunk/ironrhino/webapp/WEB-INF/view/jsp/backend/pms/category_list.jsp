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
	items="recordList" var="category" editable="true"
	toolbarLocation="none" action="category" title="categories"
	nearPageNum="0" resizeColWidth="true" width="100%"
	styleClass="sortable">
	<ec:row recordKey="${category.id}">
		<ec:column width="22px" cell="checkbox" headerCell="checkbox"
			alias="deleteFlag" value="1" viewsAllowed="html" headerClass="nosort" />
		<ec:column property="code" cellName="category.code" />
		<ec:column property="name" cellName="category.name" cellEdit="input" />
		<ec:column property="description" cellName="category.description"
			cellEdit="input" />
		<ec:column property="displayOrder" cellName="category.displayOrder"
			cellEdit="input" />
		<ec:column property="rolesAsString" title="Roles" cellEdit="input"
			styleClass="include_if_edited" />
		<ec:column property="action" width="320px" viewsAllowed="html"
			styleClass="include_if_edited" headerClass="nosort">
			<button type="button" onclick="ECSideX.save('${category.id}')">保存</button>
			<button type="button" onclick="ECSideX.enter('${category.id}')">进入</button>
			<button type="button"
				onclick="ECSideX.open(ECSideX.getUrl('tree','${category.id}'),true)">移动</button>
			<button type="button" onclick="ECSideX.del('${category.id}')">删除</button>
			<button type="button"
				onclick="ECSideX.enter('${category.id}','product?categoryId={parentId}')">产品</button>
		</ec:column>
	</ec:row>
	<ec:extend>
		<div style="text-align: center; width: 100%; padding: 3px" class="eXtremeTable">
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
</textarea></div>
</body>
</html>
