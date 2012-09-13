package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Validatable;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableComponent;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.ironrhino.core.struts.ValidationException;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.model.UserRole;

import com.fasterxml.jackson.core.type.TypeReference;

@AutoConfig(searchable = true, order = "name asc")
@Searchable(type = "schema")
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
public class Schema extends BaseEntity implements Validatable {

	private static final long serialVersionUID = -8352037604269012984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 1)
	private String name;

	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 2)
	private String description;

	@UiConfig(displayOrder = 3)
	private boolean strict;

	@SearchableComponent
	// @UiConfig(displayOrder = 4, cellEdit = "none", excludeIfNotEdited = true,
	// template = "${entity.getFieldsAsString()!}")
	@UiConfig(displayOrder = 4, hiddenInList = true)
	private List<SchemaField> fields = new ArrayList<SchemaField>();

	public Schema() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public List<SchemaField> getFields() {
		return fields;
	}

	public void setFields(List<SchemaField> fields) {
		this.fields = fields;
	}

	@NotInCopy
	public String getFieldsAsString() {
		return JsonUtils.toJson(fields);
	}

	public void setFieldsAsString(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				fields = JsonUtils.fromJson(str,
						new TypeReference<List<SchemaField>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public Schema merge(Schema other) {
		if (other != null)
			fields.addAll(other.getFields());
		Set<String> names = new HashSet<String>();
		Iterator<SchemaField> it = fields.iterator();
		while (it.hasNext()) {
			SchemaField f = it.next();
			if (names.contains(f.getName()) || StringUtils.isBlank(f.getName())) {
				it.remove();
				continue;
			}
			names.add(f.getName());
		}
		return this;
	}

	public void validate() {
		if (fields == null || fields.size() == 0) {
			ValidationException ve = new ValidationException();
			ve.addActionError("validation.required");
			throw ve;
		} else {
			boolean hasGroup = false;
			boolean needApendBlankGroup = false;
			Set<String> names = new HashSet<String>();
			for (int i = 0; i < fields.size(); i++) {
				SchemaField f = fields.get(i);
				if (f.getType().equals(SchemaFieldType.GROUP)) {
					hasGroup = true;
					continue;
				}
				if (StringUtils.isBlank(f.getName())) {
					ValidationException ve = new ValidationException();
					ve.addFieldError("schema.fields[" + i + "].name",
							"validation.required");
					throw ve;
				} else {
					if (names.contains(f.getName())) {
						ValidationException ve = new ValidationException();
						ve.addFieldError("schema.fields[" + i + "].name",
								"validation.already.exists");
						throw ve;
					} else {
						names.add(f.getName());
					}
				}
				if (f.getType().equals(SchemaFieldType.SELECT)
						|| f.getType().equals(SchemaFieldType.CHECKBOX)) {
					List<String> values = f.getValues();
					if (values == null || values.size() == 0
							|| values.size() == 1
							&& StringUtils.isBlank(values.get(0))) {
						ValidationException ve = new ValidationException();
						ve.addFieldError("schema.fields[" + i + "].values[0]",
								"validation.required");
						throw ve;
					} else {
						Set<String> set = new HashSet<String>();
						for (int j = 0; j < values.size(); j++) {
							String value = values.get(j);
							if (StringUtils.isBlank(value)) {
								ValidationException ve = new ValidationException();
								ve.addFieldError("schema.fields[" + i
										+ "].values[" + j + "]",
										"validation.required");
								throw ve;
							} else {
								if (set.contains(value)) {
									ValidationException ve = new ValidationException();
									ve.addFieldError("schema.fields[" + i
											+ "].values[" + j + "]",
											"validation.already.exists");
									throw ve;
								} else {
									set.add(value);
								}
							}
						}
					}
				} else {
					f.setValues(null);
				}
				if (hasGroup && i == fields.size() - 1)
					needApendBlankGroup = hasGroup && i == fields.size() - 1;
			}
			if (needApendBlankGroup) {
				SchemaField f = new SchemaField();
				f.setName("");
				f.setType(SchemaFieldType.GROUP);
				fields.add(f);
			}
		}
	}

}
