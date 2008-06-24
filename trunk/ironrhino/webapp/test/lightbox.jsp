<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>test</title>
<link rel="stylesheet" media="all" type="text/css"
	href="<c:url value="/components/lightbox/styles/lightbox.css"/>" />
<link rel="stylesheet" media="all" type="text/css"
	href="<c:url value="/components/lightbox/styles/lightbox_nav.css"/>" />
<!--[if lte IE 6]>
		<link rel="stylesheet" media="all" type="text/css" href="<c:url value="/components/lightbox/styles/lightbox_nav_ie.css"/>" />
		<![endif]-->
<script src="<c:url value="/components/lightbox/scripts/lightbox.js"/>"
	type="text/javascript"></script>
</head>
<body>
<div id="info">
<div class="lightbox_menu">
<ul>
	<li><a class="hide" href="#pics">view pics</a> <!--[if lte IE 6]>
					<a href="#pics">view pics
						<table><tr><td>
									<![endif]-->
	<ul>
		<li><a href="images/image-1.jpg" rel="lightbox[product]"><img
			src="images/thumb-1.jpg" width="100" height="40" alt="" /></a> <a
			href="images/image-2.jpg" rel="lightbox[product]"><img
			src="images/thumb-2.jpg" width="100" height="40" alt="" /></a> <a
			href="images/image-1.jpg" rel="lightbox[product]"><img
			src="images/thumb-1.jpg" width="100" height="40" alt="" /></a> <a
			href="images/image-1.jpg" rel="lightbox[product]"><img
			src="images/thumb-1.jpg" width="100" height="40" alt="" /></a></li>
	</ul>
	<!--[if lte IE 6]>
						</td></tr></table>
					</a>
					<![endif]--></li>
</ul>
</div>
</div>
<!-- end of info -->
</body>
</html>
