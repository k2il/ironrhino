<#include "../../ec-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<#assign config={"username":{},"name":{},"email":{},"password":{"value":"********","cellEdit":"input,ec_edit_template_password"},"rolesAsString":{"cellEdit":"input","class":"include_if_edited"},"groupsAsString":{"cellEdit":"input","class":"include_if_edited"}}>
<@ectable entityName="user" config=config/>
<div style="display: none;">
<textarea id="ec_edit_template_password">
	<input type="password" class="inputtext" value=""
	onblur="ECSideX.updatePasswordCell(this)" style="width: 100%;" name="" />
</textarea></div>
</body>
</html>
