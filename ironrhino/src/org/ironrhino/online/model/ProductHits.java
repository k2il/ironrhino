package org.ironrhino.online.model;

import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class ProductHits extends BaseEntity {

	@NaturalId
	private String productCode;

	private int daily;

	private int monthly;

	private int yearly;

	private int total;

	public int getDaily() {
		return daily;
	}

	public void setDaily(int daily) {
		this.daily = daily;
	}

	public int getMonthly() {
		return monthly;
	}

	public void setMonthly(int monthly) {
		this.monthly = monthly;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getYearly() {
		return yearly;
	}

	public void setYearly(int yearly) {
		this.yearly = yearly;
	}

}
