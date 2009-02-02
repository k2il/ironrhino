<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="authz"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator"
	prefix="decorator"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><decorator:title default="ironrhino" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<meta name="context_path" content="${pageContext.request.contextPath}" />
<link rel="shortcut icon" href="<c:url value="/images/favicon.ico"/>" />
<link href="<c:url value="/styles/main.css"/>" media="all"
	rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="<c:url value="/styles/ie.css"/>" media="all"
		rel="stylesheet" type="text/css" />
	<![endif]-->
<link rel="stylesheet" href="<s:url value="/themes/base/ui.all.css"/>"
	type="text/css" media="screen" />
<script src="<c:url value="/scripts/jquery.js"/>" type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.form.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/datagridtable.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/application.js"/>"
	type="text/javascript"></script>
<script type="text/javascript" src="<c:url value="/scripts/ecside.js"/>"></script>
<decorator:head />
</head>
<body>
<div id="content">
<div id="message"><s:actionerror cssClass="action_error" /><s:actionmessage
	cssClass="action_message" /></div>
<decorator:body /></div>
</body>
</html>