<%@ page session="true" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>ironrhino</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
</head>
<body>
<div id="signin-main">
<form method="post"
	action="<%=response.encodeRedirectURL("login" + (request.getQueryString()!=null ? "?" + request.getQueryString() : ""))%>">

<!-- Begin error message generating Server-Side tags -->
<spring:hasBindErrors name="credentials">
	<c:forEach var="error" items="${errors.allErrors}">
		<br />
		<spring:message code="${error.code}" text="${error.defaultMessage}" />
	</c:forEach>
</spring:hasBindErrors>
<!-- End error message generating Server-Side tags -->

<p>Userid <input id="username" name="username" size="32"
	tabindex="1" accesskey="n" /></p>

<p>Password <input type="password" id="password" name="password"
	size="32" tabindex="2" accesskey="p" /></p>

<p><input type="checkbox" id="warn" name="warn" value="false"
	tabindex="3" /> Warn me before logging me into other sites. <!-- The following hidden field must be part of the submitted Form -->
<input type="hidden" name="lt" value="${flowExecutionKey}" /> <input
	type="hidden" name="_eventId" value="submit" /></p>

<p><input type="submit" class="button" accesskey="l" value="LOGIN"
	tabindex="4" /></p>
</form>
</div>
</body>
</html>
