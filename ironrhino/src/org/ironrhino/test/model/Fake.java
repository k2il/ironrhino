package org.ironrhino.test.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.FormElement;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.NotInCopy;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Customizable;

@AutoConfig
public class Fake extends BaseEntity implements Customizable {

	// key is unique as a natrual id and case insensitive,and cannot modify when
	// edit this entity
	@NaturalId(immutable = true, caseInsensitive = true)
	private String key;

	private String stringValue;

	// use textarea install input
	@FormElement(type = "textarea")
	private String content;

	private boolean booleanValue = true;

	private int intValue;

	private double doubleValue;

	private Date dateValue = new Date();

	// not in page
	@NotInCopy
	private Date createDate;

	public Fake() {
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
	
	private Map<String, Serializable> customProperties;

	public Map<String, Serializable> getCustomProperties() {
		if (customProperties == null)
			customProperties = new HashMap<String, Serializable>();
		return customProperties;
	}

	public void setCustomProperties(Map<String, Serializable> customProperties) {
		this.customProperties = customProperties;
	}


}
