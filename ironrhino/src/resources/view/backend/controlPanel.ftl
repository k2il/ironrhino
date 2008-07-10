<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Control Panel</title>
<script>
Initialization.init = function(){
$('#rebuild_category_tree').click(function(){ApplicationContextConsole.call('categoryTreeControl.buildCategoryTree()',null,function(){alert('success')})});
$('#generate_static_page').click(function(){ApplicationContextConsole.call('productPageGenerator.generate()',null,function(){alert('success');});});
$('#send_newArrived_product').click(function(){ApplicationContextConsole.call('newArrivedProductNotifier.send()',null,function(){alert('success');});});
$('#compile_rulebase').click(function(){ApplicationContextConsole.call('ruleProvider.compileRuleBase()',null,function(){alert('success');});});
$('#compass_index').click(function(){ApplicationContextConsole.call('compassGps.index()',null,function(){alert('success');});});
$('#execute').click(function(){ApplicationContextConsole.execute($('cmd').value,function(result){alert(result);});});
}

</script>
</head>
<body>
<div><input type="button" id="rebuild_category_tree"
	value="rebuild category tree" /><input type="button"
	id="generate_static_page" value="generate static page" /><input
	type="button" id="send_newArrived_product"
	value="send newArrived product" /><input type="button"
	id="compile_rulebase" value="compile rulebase" /><input type="button"
	id="compass_index" value="compass index" /></div>
<div><input id="cmd" type="text" name="cmd" size="80" /><input
	type="button" id="execute" value="execute" /></div>
</body>
</html>
