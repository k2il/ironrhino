<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('signup')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta name="decorator" content="simple" />
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
<div class="row">
	<div class="span6 offset3">
	<div class="hero-unit">
	<@s.form method="post" action="signup" cssClass="ajax focus form-horizontal well">
		<@s.textfield label="%{getText('email')}" name="email" type="email" cssClass="required email span2" />
		<@s.textfield label="%{getText('username')}" name="username" cssClass="span2" />
		<@s.password label="%{getText('password')}" name="password" cssClass="required span2"/>
		<@s.password label="%{getText('confirmPassword')}" name="confirmPassword" cssClass="required span2"/>
		<@s.submit value="%{getText('signup')}" cssClass="btn-primary">
		<@s.param name="after"> <a class="btn hidden-pad hidden-tablet hidden-phone" href="${getUrl('/signup/forgot')}">${action.getText('signup.forgot')}</a> <a class="btn" href="${getUrl('/login')}">${action.getText('login')}</a></@s.param>
		</@s.submit>
	</@s.form>
	</div>
	</div>
</div>
</@authorize>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<div class="modal">
	<div class="modal-body">
		<div class="progress progress-striped active">
			<div class="bar" style="width: 50%;"></div>
		</div>
	</div>
</div>
</@authorize>
</body>
</html></#escape>