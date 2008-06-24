<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>

</head>
<body>
<table>
	<tr>
		<td><s:property value="getText('product.code')" /></td>
		<td><s:property value="product.code" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.name')" /></td>
		<td><s:property value="product.name" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.spec')" /></td>
		<td><s:property value="product.spec" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.material')" /></td>
		<td><s:property value="product.material" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.shortDescription')" /></td>
		<td><s:property value="product.shortDescription" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.description')" /></td>
		<td><s:property value="product.description" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.inventory')" /></td>
		<td><s:property value="product.inventory" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.price')" /></td>
		<td><s:property value="product.price" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.status')" /></td>
		<td><s:property value="product.status.displayName" /></td>
	</tr>
	<tr>
		<td><s:property value="getText('product.category')" /></td>
		<td><s:property value="product.category.name" /></td>
	</tr>
</table>



<s:url id="url" action="product" method="input" includeParams="none">
	<s:param name="parentId" value="product.id" />
</s:url>

<s:a href="%{url}">Create a new sub Product</s:a>
<table>
	<tr>
		<th><s:property value="getText('product.code')" /></th>
		<th><s:property value="getText('product.name')" /></th>
		<th><s:property value="getText('product.type')" /></th>
		<th><s:property value="getText('product.status')" /></th>
		<th>Actions</th>
	</tr>
</table>

<table>
	<s:iterator value="product.relatedProducts">
		<tr>
			<td><s:property value="code" /></td>
			<td><s:property value="name" /></td>
		</tr>
	</s:iterator>
</table>
</body>
</html>
