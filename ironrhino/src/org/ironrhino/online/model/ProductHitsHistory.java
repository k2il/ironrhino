package org.ironrhino.online.model;

import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class ProductHitsHistory extends BaseEntity {
	@NaturalId
	private String productCode;

	@NaturalId
	private int month;

	@NaturalId
	private int year;

	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

}
