package org.ironrhino.pms.service;

import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.ironrhino.pms.model.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:resources/spring/applicationContext-base.xml",
		"classpath:resources/spring/applicationContext-hibernate.xml",
		"classpath:resources/spring/applicationContext-service-pms.xml",
		"classpath:resources/spring/applicationContext-aop.xml" })
public class ProductManagerTest {
	Product product;

	@Autowired
	ProductManager productManager;

	@Before
	public void doSetUp() {
		product = new Product();
		product.setCode("productCode");
		product.setName("productName");
		product.setNewArrival(true);
		product.setNewArrivalTimeLimit(new Date());
		productManager.save(product);
	}

	@After
	public void doTearDown() {
		productManager.delete(product);
	}

	@Test
	public void testUpdateNewArrival() {
		productManager.updateNewArrival();
		productManager.clear();
		product = productManager.get(product.getId());
		assertFalse(product.isNewArrival());
	}

}
