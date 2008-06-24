<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<s:set var="entityName" value="entityName" scope="request" />
<s:set var="naturalIdsImmatuable" value="naturalIdsImmatuable"
	scope="request" />
<s:set var="naturalIds" value="naturalIds.keySet()" scope="request" />
<s:set var="formElements" value="formElements" scope="request" />
<div id="list" style="width: 100%"><ec:table tableId="ec"
	items="recordList" var="entity" editable="true" action="${entityName}"
	nearPageNum="0" resizeColWidth="true" width="100%"
	styleClass="sortable">
	<ec:row recordKey="${entity.id}">
		<ec:column width="22px" cell="checkbox" headerCell="checkbox"
			alias="deleteFlag" value="1" viewsAllowed="html" headerClass="nosort" />
		<c:forEach items="${naturalIds}" var="name">
			<c:if test="${naturalIdsImmatuable}">
				<ec:column property="${name}" cellName="${entityName}.${name}" />
			</c:if>
			<c:if test="${not naturalIdsImmatuable}">
				<ec:column property="${name}" cellName="${entityName}.${name}"
					cellEdit="input" />
			</c:if>
		</c:forEach>
		<c:forEach items="${formElements}" var="var">
			<c:if test="${!fn:contains(naturalIds,var.key)}">
				<c:if test="${var.value.type=='input'}">
					<c:if test="${var.value.readonly}">
						<ec:column property="${var.key}"
							cellName="${entityName}.${var.key}" />
					</c:if>
					<c:if test="${not var.value.readonly}">
						<ec:column property="${var.key}"
							cellName="${entityName}.${var.key}" cellEdit="input" />
					</c:if>
				</c:if>
				<c:if test="${var.value.type=='checkbox'||var.value.type=='select'}">
					<ec:column property="${var.key}"
						cellName="${entityName}.${var.key}"
						cellEdit="select,select_template_${var.key},onclick" />
				</c:if>
			</c:if>
		</c:forEach>
		<ec:column property="action" width="200px" viewsAllowed="html"
			styleClass="include_if_edited" headerClass="nosort">
			<button type="button" onclick="ECSideX.save('${entity.id}')">保存</button>
			<button type="button" onclick="ECSideX.input('${entity.id}')">编辑</button>
			<button type="button" onclick="ECSideX.del('${entity.id}')">删除</button>
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
</textarea> <c:forEach items="${formElements}" var="var">
	<c:if test="${var.value.type=='checkbox'}">
		<textarea id="select_template_${var.key}">
	<select onblur="ECSideUtil.updateCell(this,'select')"
			style="width: 100%;" name="${entityName}.${var.key}">
	<option value="true">true</option>
	<option value="false">false</option>
</select>
</textarea>
	</c:if>
	<c:if test="${var.value.type=='select'}">
		<textarea id="select_template_${var.key}">
	<select onblur="ECSideUtil.updateCell(this,'select')"
			style="width: 100%;" name="${entityName}.${var.key}">
			<c:forEach items="${var.value.enumValues}" var="en">
			<option value="${en.name}">${en.displayName}</option>
			</c:forEach>
	</select>
	</textarea>
	</c:if>
</c:forEach></div>
</body>
</html>
