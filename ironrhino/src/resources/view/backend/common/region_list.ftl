<#include "../../ec-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<#assign config={"name":{"cellEdit":"input"},"displayOrder":{"cellEdit":"input"},"rolesAsString":{"cellEdit":"input","class":"include_if_edited"}}>
<@ectable entityName="region" config=config actionColumnButtons='<button type="button" onclick="ECSideX.enter(\'#id\')">进入</button><button type="button" onclick="ECSideX.save(\'#id\')">保存</button><button type="button" onclick="ECSideX.del(\'#id\')">删除</button>'/>
</body>
</html>
