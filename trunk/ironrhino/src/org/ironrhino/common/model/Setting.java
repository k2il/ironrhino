package org.ironrhino.common.model;

import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.Publishable;
import org.ironrhino.core.annotation.Recordable;
import org.ironrhino.core.model.BaseEntity;

@Recordable
@Publishable
@AutoConfig
public class Setting extends BaseEntity {

	@NaturalId
	private String key = "";

	private String value = "";

	public Setting() {

	}

	public Setting(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
