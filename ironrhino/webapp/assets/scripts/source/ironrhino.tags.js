Observation.tags = function(container) {
	if (typeof $.fn.textext != 'undefined'
			&& (!$.browser.msie || $.browser.version > '8')) {
		$('input.tags', container).each(function() {
			var t = $(this);
			var options = {
				prompt : '...',
				autocomplete : {
					dropdownMaxHeight : '200px',
					render : function(suggestion) {
						if (typeof suggestion == 'string') {
							return suggestion;
						} else {
							if (!suggestion.label)
								return suggestion.value;
							else
								return '<div value="' + suggestion.value + '">'
										+ suggestion.label + '</div>';
						}
					}
				},
				ext : {
					core : {
						serializeData : function(data) {
							return data.join(',');
						}
					},
					itemManager : {
						itemToString : function(item) {
							var str = typeof item == 'string'
									? item
									: item.value;
							return str;
						}
					},
					autocomplete : {
						showDropdown : function() {
							if ($('.text-suggestion', this.containerElement()).length)
								this.containerElement().show();
						},
						renderSuggestions : function(suggestions) {
							var self = this;
							self.clearItems();
							var inputed = [];
							$('.text-tags .text-label', self.container).each(
									function() {
										inputed.push($(this).text())
									});
							var empty = true;
							$.each(suggestions || [], function(index, item) {
										var value = self.itemManager()
												.itemToString(item);
										var exists = false;
										for (var j = 0; j < inputed.length; j++)
											if (inputed[j] == value) {
												exists = true;
												break;
											}
										if (!exists) {
											self.addSuggestion(item);
											empty = false;
										}
									});
						}
					}
				}
			};
			var value = t.val();
			if (value) {
				if (value.indexOf("[\"") == 0)
					options.tagsItems = $.parseJSON(value);
				else
					options.tagsItems = value.split(',');
			}
			if (t.data('source')) {
				options.plugins = 'tags prompt focus autocomplete ajax arrow';
				options.ajax = {
					global : false,
					url : t.data('source'),
					cacheResults : false,
					dataCallback : function(q) {
						return {
							'keyword' : q
						};
					}
				};
			} else {
				options.plugins = 'tags prompt focus';
			}
			t.val('').textext(options).bind('isTagAllowed', function(e, data) {
						var inputed = [];
						$('.text-tags .text-label', this.container).each(
								function() {
									inputed.push($(this).text())
								});
						for (var i = 0; i < inputed.length; i++)
							if (inputed[i] == data.tag) {
								data.result = false;
								break;
							}
					});
		});
	}
};