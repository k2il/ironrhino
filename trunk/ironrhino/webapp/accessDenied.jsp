<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="org.springframework.security.context.SecurityContextHolder"%>
<%@ page import="org.springframework.security.Authentication"%>
<%@ page import="org.springframework.security.ui.AccessDeniedHandlerImpl"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Login</title>
</head>

<body>
<h1>Sorry, access is denied</h1>


<p><%=request
									.getAttribute(AccessDeniedHandlerImpl.SPRING_SECURITY_ACCESS_DENIED_EXCEPTION_KEY)%>
</p>
<p>
<%
			Authentication auth = SecurityContextHolder.getContext()
			.getAuthentication();
	if (auth != null) {
%> Authentication object as a String: <%=auth.toString()%> <%
 }
 %>
</p>
</body>
</html>
