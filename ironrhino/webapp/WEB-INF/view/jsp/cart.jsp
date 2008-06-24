<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<form action="<s:url value="/cart/update"/>" method="post"
	class="ajax view" options="{replacement:'detail'}">
<div id="detail">
<table class="sortable">
	<thead>
		<tr>
			<th class="nosort"><input type="checkbox" /></th>
			<th>name</th>
			<th>price</th>
			<th class="nosort">quantity</th>
			<th>subtotal</th>
			<th class="nosort">remove</th>
		</tr>
	</thead>
	<tfoot>
		<tr>
			<td></td>
			<td>name</td>
			<td>price</td>
			<td>quantity</td>
			<td>subtotal</td>
			<td>remove</td>
		</tr>
	</tfoot>
	<tbody>
		<s:iterator value="cart.order.items" status="status">
			<tr class="row">
				<td><input type="checkbox" name="id"
					value="<s:property value="productCode" />" /></td>
				<td><a
					href="<s:url value="%{'/product/'+productCode+'.html'}"/>"> <s:property
					value="productName" /> </a></td>
				<td><s:property value="productPrice" /></td>
				<td><input type="hidden"
					name="items[<s:property value="#status.index" />].productCode"
					value="<s:property value="productCode" />" /> <input type="text"
					size="2"
					name="items[<s:property value="#status.index" />].quantity"
					value="<s:property value="quantity" />" /></td>
				<td><s:property value="subtotal" /></td>
				<td><a href="<s:url value="%{'/cart/remove/'+productCode}"/>"
					class="ajax view"
					options="{replacement:'detail',onprepare:'confirm(\'are you sure to remove <s:property value="productName" />\')'}">remove</a></td>
			</tr>
		</s:iterator>
	</tbody>
</table>
</div>
<div id="actions"><span class="link goback">继续购物</span> <span><input
	type="submit" value="update" class="button" /></span> <span><a
	href="<s:url value="/cart/remove"/>" class="ajax view delete_selected"
	options="{replacement:'detail'}">remove selected</a></span> <span><a
	id="clear" href="<s:url value="/cart/clear"/>" class="ajax view"
	options="{replacement:'detail',onprepare:'confirm(\'are you sure to clear cart\')'}">clear</a></span>
<span><a id="checkout"
	href="<s:url value="/account/order/addressee"/>">checkout</a> </span></div>
</form>
</body>
</html>
