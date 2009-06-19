package org.ironrhino.online.support;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.online.model.Order;
import org.ironrhino.online.model.OrderItem;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.pms.model.Product;

public class Cart implements Serializable {

	private Order order;

	private transient ProductFacade productFacade;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	public boolean contains(String productCode) {
		if (StringUtils.isBlank(productCode))
			return false;
		for (OrderItem var : order.getItems())
			if (productCode.equals(var.getProductCode()))
				return true;
		return false;
	}

	public void put(String productCode, int quantity) {
		if (StringUtils.isBlank(productCode) || quantity <= 0)
			return;
		if (!contains(productCode)) {
			Product product = productFacade.getProductByCode(productCode);
			if (product != null && order != null)
				doPut(product, quantity);
		} else {
			update(productCode, quantity + getQuantity(productCode));
		}
	}

	public void remove(String productCode) {
		for (OrderItem var : order.getItems())
			if (productCode.equals(var.getProductCode())) {
				order.getItems().remove(var);
				break;
			}

	}

	public void update(String productCode, int quantity) {
		for (OrderItem var : order.getItems())
			if (productCode.equals(var.getProductCode())) {
				if (quantity <= 0)
					order.getItems().remove(var);
				else
					var.setQuantity(quantity);
				break;
			}
	}

	public void clear() {
		order.setDescription(null);
		order.setDiscount(null);
		order.setShipcost(null);
		order.setOrderDate(null);
		order.getItems().clear();
	}

	public BigDecimal getTotalPrice() {
		BigDecimal totalPrice = new BigDecimal(0.00);
		for (OrderItem var : order.getItems())
			totalPrice = totalPrice.add(var.getProductPrice().multiply(
					new BigDecimal(var.getQuantity())));
		return totalPrice;
	}

	public String getProductName(String productCode) {
		for (OrderItem var : order.getItems())
			if (var.getProductCode().equals(productCode))
				return var.getProductName();
		return "";
	}

	public int getQuantity(String productCode) {
		for (OrderItem var : order.getItems())
			if (productCode.equals(var.getProductCode()))
				return var.getQuantity();
		return -1;
	}

	private void doPut(Product product, int quantity) {
		boolean exists = false;
		for (OrderItem var : order.getItems()) {
			if (product.getCode().equals(var.getProductCode())) {
				exists = true;
				var.setQuantity(var.getQuantity() + quantity);
				break;
			}
		}
		if (!exists) {
			OrderItem item = new OrderItem();
			item.setProductCode(product.getCode());
			item.setProductName(product.getName());
			item.setProductPrice(product.getPrice());
			item.setQuantity(quantity);
			order.getItems().add(item);
		}
	}
}
