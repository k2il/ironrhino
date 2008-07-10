<#include "../../ec-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{"cellEdit":"input"},"tagsAsString":{"cellEdit":"input","class":"include_if_edited"},"relatedProductsAsString":{"cellEdit":"input","class":"include_if_edited"},"rolesAsString":{"cellEdit":"input","class":"include_if_edited"}}>
<@ectable entityName="product" config=config actionColumnWidth="380px" actionColumnButtons='<button type="button" onclick="ECSideX.save(\'#id\')">保存</button><button type="button" onclick="ECSideX.input(\'#id\')">编辑</button><button type="button" onclick="ECSideX.open(ECSideX.getUrl(\'picture\',\'#id\'))">图片</button><button type="button" onclick="ECSideX.open(ECSideX.getUrl(\'attribute\',\'#id\'))">属性</button><button type="button" onclick="ECSideX.open(ECSideX.getUrl(\'category\',\'#id\'),true)">目录</button><button type="button" onclick="ECSideX.del(\'#id\')">删除</button>'/>
</body>
</html>
