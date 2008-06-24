<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
<script>
function viewPicture(id){
	if(!id)return;
	w=new Window('view_window'+id,{url:'product/picture?decorator=backend_simple&product.id='+id,className:'alphacube',destroyOnClose:true,zIndex:100,width:500,height:500,showEffect:Effect.Appear,hideEffect:Effect.SwitchOff,draggable:true});   
	w.showCenter(true);
}
</script>
</head>
<body>
<div id="list" style="width: 100%"><ec:table tableId="ec"
	items="recordList" var="product" editable="true" action="product"
	title="products" nearPageNum="0" resizeColWidth="true" width="100%"
	styleClass="sortable">
	<ec:row recordKey="${product.id}">
		<ec:column width="22px" cell="checkbox" headerCell="checkbox"
			alias="deleteFlag" value="1" viewsAllowed="html" headerClass="nosort" />
		<ec:column property="code" />
		<ec:column property="name" />
		<ec:column property="tagsAsString" title="Tags" cellEdit="input"
			styleClass="include_if_edited" />
		<ec:column property="rolesAsString" title="Roles" cellEdit="input"
			styleClass="include_if_edited" />
		<ec:column property="relatedProductsAsString" title="Related Products"
			cellEdit="input" styleClass="include_if_edited" />
		<ec:column property="action" width="380px" viewsAllowed="html"
			styleClass="include_if_edited" headerClass="nosort">
			<button type="button" onclick="ECSideX.save('${product.id}')">保存</button>
			<button type="button" onclick="ECSideX.input('${product.id}')">编辑</button>
			<button type="button"
				onclick="ECSideX.open(ECSideX.getUrl('picture','${product.id}'))">图片</button>
			<button type="button"
				onclick="ECSideX.open(ECSideX.getUrl('attribute','${product.id}'))">属性</button>
			<button type="button"
				onclick="ECSideX.open(ECSideX.getUrl('category','${product.id}'),true)">目录</button>
			<button type="button" onclick="ECSideX.del('${product.id}')">删除</button>
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
</textarea></div>
</body>
</html>
