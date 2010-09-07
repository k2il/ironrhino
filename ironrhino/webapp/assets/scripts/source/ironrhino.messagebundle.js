MessageBundle = {
	'en' : {
		'ajax.loading' : 'loading...',
		'ajax.error' : 'network error,please try later',
		'required' : 'please input value',
		'selection.required' : 'please select',
		'email' : 'must be a valid email',
		'integer' : 'must be a integer',
		'integer.positive' : 'must be a positive integer',
		'double' : 'must be a decimal',
		'double.positive' : 'must be a positive decimal',
		'save.and.create' : 'save and add',
		'no.selection' : 'no selection',
		'no.modification' : 'no modification',
		'select' : 'please select',
		'confirm.delete' : 'are sure to delete?',
		'confirm.save' : 'are sure to save?',
		'confirm.exit' : 'you have unsaved modification,are sure to exit?'
	},
	'zh-cn' : {
		'ajax.loading' : '正在加载...',
		'ajax.error' : '错误,请稍后再试',
		'required' : '请填写',
		'selection.required' : '请选择',
		'email' : 'email不合法',
		'integer' : '请填写整数',
		'integer.positive' : '请填写正整数',
		'double' : '请填写数字',
		'double.positive' : '请填写大于零的数字',
		'save.and.create' : '保存并新建',
		'no.selection' : '没有选中',
		'no.modification' : '没有更改',
		'add' : '添加',
		'remove' : '删除',
		'browse' : '浏览文件',
		'select' : '请选择',
		'save' : '保存',
		'restore' : '还原',
		'cancel' : '取消',
		'error' : '错误',
		'success' : '操作成功',
		'confirm' : '确定',
		'confirm.delete' : '确定要删除?',
		'confirm.save' : '确定要保存?',
		'confirm.exit' : '有改动未保存,确定要离开?'
	},
	get : function() {
		var key = arguments[0];
		var lang = MessageBundle.lang();
		var msg = MessageBundle[lang][key];
		if (typeof(msg) == 'undefined')
			msg = key;
		for (var i = 1; i < arguments.length; i++)
			msg = msg.replace('{' + i + '}', arguments[i]);
		return msg;
	},
	lang : function() {
		var lang = (navigator.language || navigator.browserLanguage || '')
				.toLowerCase();
		if (!MessageBundle[lang])
			lang = 'en';
		return lang;
	},
	shortLang : function() {
		var lang = MessageBundle.lang();
		var i = lang.indexOf('-');
		if (i > 0)
			lang = lang.substring(0, i);
		return lang;
	}
};