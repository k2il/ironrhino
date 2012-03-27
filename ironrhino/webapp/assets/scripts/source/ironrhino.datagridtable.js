(function($) {
	$.fn.datagridTable = function(options) {
		options = options || {};
		$(this).each(function() {
			if ($(this).hasClass('datagrided'))
				return;
			$(this).addClass('datagrided');
			if ($(this).parents('.datagrided').length)
				return;
			$('tbody input:last', this).keydown(function(event) {
						if (event.keyCode == 13) {
							event.preventDefault();
							addRow(event, options);
						}
					});
			$('tbody input:first', this).keydown(function(event) {
						if (event.keyCode == 8 && !$(event.target).val()) {
							event.preventDefault();
							removeRow(event, options);
						}
					});
			$('thead .add', this).click(function(event) {
				var rows = $(event.target).closest('table.datagrided')
						.children('tbody').children().not('.nontemplate');
				if (rows.length > 0)
					addRow(event, options, rows.eq(0), true);
			});
			$('tbody .add', this).click(function(event) {
						addRow(event, options)
					});
			$('tbody .remove', this).click(function(event) {
						removeRow(event, options)
					});
			$('tbody .moveup', this).click(function(event) {
						moveupRow(event, options)
					});
			$('tbody .movedown', this).click(function(event) {
						movedownRow(event, options)
					});
		})

		return this;
	};

	var addRow = function(event, options, row, first) {
		var row = row || $(event.target).closest('tr');
		var r = row.clone(true);
		$('*', r).removeAttr('id');
		$('span.info', r).html('');
		$(':input[type!=checkbox][type!=radio]', r).val('');
		$('input[type=checkbox],input[type=radio]', r).prop('checked', false);
		$(':input').prop('readonly', false).removeAttr('keyupValidate');
		$('input.filterselect', this).next('select').html(function() {
					return $(this).data('innerHTML')
				});
		$('.datagrided tr', r).each(function(i) {
					if (i > 0)
						$(this).remove();
				});
		if (first)
			row.parent().prepend(r);
		else
			row.after(r);
		rename(row.closest('tbody'));
		$('select.textonadd', r).each(function() {
			$(this).replaceWith('<input type="text" name="'
					+ $(this).attr('name') + '">');
		});
		var checkboxname = '';
		$('input.textonadd[type=checkbox]', r).each(function() {
			if (!checkboxname || checkboxname != $(this).attr('name')) {
				$(this).replaceWith('<input type="text" name="'
						+ $(this).attr('name') + '">');
				checkboxname = $(this).attr('name');
			} else {
				$(this).remove();
			}
		});
		$('.removeonadd', r).remove();
		$('.hideonadd', r).hide();
		$('.showonadd', r).show();
		$(':input', r).eq(0).focus();
		if (options.onadd)
			options.onadd.apply(r.get(0));
	};
	var removeRow = function(event, options) {
		var row = $(event.target).closest('tr');
		var tbody = row.closest('tbody');
		var table = tbody.closest('table.datagrided');
		if (!table.hasClass('nullable') && $('tr', tbody).length == 1
				|| options.onbeforeremove
				&& options.onbeforeremove.apply(row.get(0)) === false)
			return;
		$(':input', row.prev()).eq(0).focus();
		row.remove();
		rename(tbody);
		if (options.onremove)
			options.onremove();
	};
	var moveupRow = function(event, options) {
		var row = $(event.target).closest('tr');
		if (row.closest('tbody').children().length > 1) {
			$(row).fadeOut(function() {
						if ($(this).prev().length)
							$(this).insertBefore($(this).prev()).fadeIn();
						else
							$(this).insertAfter($(this).siblings(':last'))
									.fadeIn();
						rename($(this).closest('tbody'));
						if (options.onmoveup)
							options.onmoveup.apply(this);
					});
		}
	};
	var movedownRow = function(event, options) {
		var row = $(event.target).closest('tr');
		if (row.closest('tbody').children().length > 1) {
			$(row).fadeOut(function() {
						if ($(this).next().length)
							$(this).insertAfter($(this).next()).fadeIn();
						else
							$(this).insertBefore($(this).siblings(':first'))
									.fadeIn();
						rename($(this).closest('tbody'));
						if (options.onmovedown)
							options.onmovedown.apply(this);
					});
		}
	};
	var rename = function(tbody) {
		var level = $(tbody).parents('table.datagrided').length;
		$(tbody).children('tr').each(function(i) {
			$(':input', this).each(function() {
				var name = $(this).prop('name');
				var j = -1;
				for (var k = 0; k < level; k++)
					j = name.indexOf('[', j + 1);
				if (j < 0)
					return;
				name = name.substring(0, j + 1) + i
						+ name.substring(name.indexOf(']', j));
				$(this).prop('name', name);
			});
		}).closest('form').addClass('dirty');
	}
})(jQuery);

Observation.datagridTable = function(container) {
	$('table.datagrid', container).datagridTable();
};