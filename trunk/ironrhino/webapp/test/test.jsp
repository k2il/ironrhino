<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>test</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<div onclick="Element.remove(this)">remove me</div>
<form id="form" action="upload" method="post"
	enctype="multipart/form-data" class="ajax"><input type="text"
	name="name" /> <input type="file" name="file" /> <input type="submit" /></form>
</body>
</html>

