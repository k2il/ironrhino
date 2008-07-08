ECSideX = {
	instance : null,
	id : 'ec',
	inputUrl : '',
	saveUrl : '',
	viewUrl : '',
	deleteUrl : '',
	enterUrl : '',
	getBaseUrl : function() {
		url = document.location.href;
		if (url.indexOf('?') > 0)
			url = url.substring(0, url.indexOf('?'));
		return url;
	},
	getUrl : function(type, id, includeParams) {
		url = eval('ECSideX.' + type + 'Url');
		if (!url)
			url = ECSideX.getBaseUrl() + '/' + type;
		if (includeParams)
			url += document.location.search;
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'decorator=backend_simple';
		if (id) {
			if (url.indexOf('{id}') > 0)
				url = url.replace('{id}', id);
			else
				url += '&id=' + id;
		}
		return url;
	},
	reload : function() {
		ECSideUtil.reload(ECSideX.id);
	},
	input : function(id) {
		ECSideX.open(ECSideX.getUrl('input', id, true), true);
	},
	view : function(id) {
		if (!id)
			return;
		ECSideX.open(ECSideX.getUrl('view', id));
	},

	open : function(url, reloadonclose) {
		if ($('#_window_').length == 0)
			$('<div id="_window_" class="flora" title=""><iframe style="width:600px;height:600px;"/></div>')
					.appendTo(document.body);
		$('#_window_ > iframe')[0].src = url;
		if ($('#_window_').attr('_dialoged_')) {
			$("#_window_").dialog('open');
			return;
		}
		$('#_window_').attr('_dialoged_', true);
		$("#_window_").dialog({
			width : 630,
			height : 660,
			close : (reloadonclose ? function() {
				ECSideX.reload();
			} : null)
		});
	},
	enter : function(parentId, url) {
		if (!url) {
			url = ECSideX.enterUrl;
			if (!url)
				url = ECSideX.getBaseUrl();
		}
		if (parentId) {
			if (url.indexOf('{parentId}') > 0)
				url = url.replace('{parentId}', parentId);
			else
				url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId='
						+ parentId;
		}
		document.location.href = url;
	},
	save : function(id) {
		arr = [];
		if (id)
			arr[0] = id;
		else
			$.each(ECSideUtil.getEditedRows(ECSideX.id), function() {
				arr.push($(this).attr('recordkey'))
			});
		$.each(arr, function() {
			rows = ECSideList[ECSideX.id].ECListBody.rows;
			var row;
			for (i = 0; i < rows.length; i++)
				if (rows[i].getAttribute('recordkey') == this)
					row = rows[i];
			if (row && row.getAttribute('edited') == 'true') {
				params = {};
				entity = ECSideX.getBaseUrl();
				entity = entity.substring(entity.lastIndexOf('/') + 1);
				params[entity + '.id'] = this;
				$.each(row.cells, function() {
					name = $(this).attr("cellName");
					if (!name || name == 'null'
							|| $(this).attr('edited') != 'true'
							&& $(this).hasClass('include_if_edited'))
						return;
					value = $(this).attr('cellValue');
					if (!value || value == 'null')
						value = window.isIE ? this.innerText : this.textContent;
					params[name] = value;
						// params.push(name+'='+encodeURIComponent(value));
				});
				url = ECSideX.saveUrl;
				if (!url)
					url = ECSideX.getBaseUrl() + '/save';
				ajax({
					url : url,
					type : 'POST',
					data : params,
					dataType : 'json'
				});
			}
		});
	},
	del : function(id) {
		url = ECSideX.deleteUrl;
		if (!url)
			url = ECSideX.getBaseUrl() + '/delete';
		if (id) {
			url += (url.indexOf('?') > 0 ? '&' : '?') + 'id=' + id;
		} else {
			arr = [];
			$('input[type="checkbox"]', ECSideList[ECSideX.id].ECForm)
					.each(function() {
						if (this.checked)
							arr.push('id='
									+ $(this).parents('tr').attr('recordkey'));
					});
			if (arr.length == 0)
				return;
			url += (url.indexOf('?') > 0 ? '&' : '?') + arr.join('&');
		}
		ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			complete : ECSideX.reload
		});
	},
	updatePasswordCell : function(cellEditObj) {
		var cellObj = cellEditObj.parentNode;
		cellObj.innerHTML = '********';
		cellObj.setAttribute("cellValue", ECSideUtil
				.trimString(cellEditObj.value));
		cellObj.setAttribute("edited", "true");
		cellObj.parentNode.setAttribute("edited", "true");
		cellObj.setAttribute("editing", "false");
		ECSideUtil.addClass(cellObj, "editedCell");
	}
}

Initialization.initECSideX = function() {
	if ($(ECSideX.id) && !ECSideX.instance) {
		var ec = new ECSide(ECSideX.id);
		ec.findAjaxZoneAtClient = true;
		ec.init();
		ECSideX.instance = ec
	}
}
