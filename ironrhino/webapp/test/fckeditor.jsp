<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>test</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
${param['content']}
<br />
<form action="test.jsp" method="post"><input type="hidden"
	id="content" name="content" value="default content" /> <input
	type="hidden" id="content___Config"
	value="FlashUploadURL=${pageContext.request.contextPath}/components/editor/upload?Type%3Dflash&FlashBrowserURL=${pageContext.request.contextPath}/components/editor/filemanager/browser/default/browser.html?Type%3Dflash%26Connector%3D${pageContext.request.contextPath}/components/editor/connect&ImageBrowserURL=${pageContext.request.contextPath}/components/editor/filemanager/browser/default/browser.html?Type%3Dimage%26Connector%3D${pageContext.request.contextPath}/components/editor/connect&ImageUploadURL=${pageContext.request.contextPath}/components/editor/upload?Type%3Dimage&LinkUploadURL=${pageContext.request.contextPath}/components/editor/upload?Type%3Dfile&LinkBrowserURL=${pageContext.request.contextPath}/components/editor/filemanager/browser/default/browser.html?Connector%3D${pageContext.request.contextPath}/components/editor/connect"
	disabled="disabled" /> <iframe id="content___Frame"
	src="<c:url value="/components/editor/fckeditor.html?InstanceName=content&Toolbar=Default"/>"
	width="100%" height="400" frameborder="no" scrolling="no"></iframe> <input
	type="submit" value="Submit" /></form>
</body>
</html>
