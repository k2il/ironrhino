<%@ page contentType="text/html; charset=utf-8" language="java"%><%@ taglib uri="/struts-tags" prefix="s"%><%
response.setHeader("Cache-Control", "max-age=86400");
%><s:iterator value="suggestions">
<s:property value="principal" />
</s:iterator>