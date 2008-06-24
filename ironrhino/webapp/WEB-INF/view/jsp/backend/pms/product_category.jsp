<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>product category</title>
<link href="<c:url value="/styles/jquery.treeview.css"/>" media="screen"
	rel="stylesheet" type="text/css" />
<script type="text/javascript"
	src="<c:url value="/scripts/jquery.treeview.js"/>"></script>
<s:if test="async">
	<script type="text/javascript"
		src="<c:url value="/scripts/jquery.treeview.async.my.js"/>"></script>
</s:if>
<script type="text/javascript">
	function _click(){
		var id=$(this).parents('li')[0].id;
		var name=$(this).text();
		if(!confirm('change to '+name+'?'))
		return;
		var url='<c:url value="/backend/pms/product/category/${param['id']}?categoryId="/>'+id;
		ajax({url:url,dataType:'json'});
	}
	Initialization.treeview= function(){
		$("#treeview").treeview({
			<s:if test="async">
			url: "<c:url value="/backend/pms/category/children"/>",
			click:_click,
			</s:if>
			collapsed: true,
			unique: true

		});
		<s:if test="!async">
			$("#treeview span").click(_click);
			<s:if test="product.category!=null">
			$("#<s:property value="product.category.id"/>").parents("li.expandable").find(">div.hitarea").click();
			</s:if>
		</s:if>
	};
</script>
</head>
<body>
<s:action var="category" name="category!tree" executeResult="false"/>
<s:property value="#attr.category.treeViewHtml" escape="false" />
</body>
</html>