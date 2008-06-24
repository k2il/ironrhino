package org.ironrhino;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.service.CategoryManager;
import org.ironrhino.pms.service.ProductManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ImportProduct {
	static ClassPathXmlApplicationContext ctx;

	static DataSource dataSource;

	static CategoryManager categoryManager;

	static ProductManager productManager;

	public static void main(String... strings) throws Exception {
		ctx = new ClassPathXmlApplicationContext(new String[] {
				"resources/spring/applicationContext-base.xml",
				"resources/spring/applicationContext-hibernate.xml",
				"resources/spring/applicationContext-service-pms.xml",
				"resources/spring/applicationContext-aop.xml" });
		dataSource = (DataSource) ctx.getBean("dataSource");
		categoryManager = (CategoryManager) ctx.getBean("categoryManager");
		productManager = (ProductManager) ctx.getBean("productManager");
		importProduct();
	}

	public static void importProduct() throws Exception {
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt
				.executeQuery("select FId from xiang.TCategory where FParentId='4028808b0e1b5bb6010e1b5d42870001'");
		List<String> ids = new ArrayList<String>();
		while (rs.next())
			ids.add(rs.getString("FId"));
		rs.close();
		stmt.close();
		conn.close();

		for (String id : ids)
			importCategory(null, id);

	}

	public static void importCategory(Category parent, String categoryId)
			throws Exception {
		Connection connection = dataSource.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt
				.executeQuery("select FName,FDesc from xiang.TCategory where FId='"
						+ categoryId + "'");
		rs.next();
		String name = rs.getString("FName");
		String desc = rs.getString("FDesc");
		rs.close();
		stmt.close();
		Category category = new Category();
		category.setName(name);
		category.setDescription(desc);
		category.setParent(parent);
		categoryManager.save(category);
		stmt = connection.createStatement();
		rs = stmt
				.executeQuery("select * from xiang.TProduct where FCategoryId='"
						+ categoryId + "'");
		Connection connection2 = dataSource.getConnection();
		Statement stmt2 = connection2.createStatement();
		if (rs.next()) {
			StringBuilder sb = new StringBuilder();
			sb
					.append("insert into pms.products(id,code,name,description,spec,material,inventory,price,pictured,categoryId)");
			sb.append(" values (");
			sb.append("'" + rs.getString("FId") + "',");
			sb.append("'" + rs.getString("FCode") + "',");
			sb.append("'" + rs.getString("FName") + "',");
			sb.append("'" + rs.getString("FDesc") + "',");
			sb.append("'" + rs.getString("FSpec") + "',");
			sb.append("'" + rs.getString("FMaterial") + "',");
			sb.append(rs.getInt("FInventory") + ",");
			sb.append(rs.getDouble("FUnitPrice") + ",");
			if (!"".equals(rs.getString("FActualSizePath")))
				sb.append("1,");
			else
				sb.append("0,");
			sb.append("'" + rs.getString("FCategoryId") + "'");
			sb.append(")");
			stmt2.execute(sb.toString());
		}
		stmt2.close();
		connection2.close();
		rs.close();
		stmt.close();
		stmt = connection.createStatement();
		rs = stmt
				.executeQuery("select FId from xiang.TCategory where FParentId='"
						+ categoryId + "'");
		List<String> ids = new ArrayList<String>();
		while (rs.next())
			ids.add(rs.getString("FId"));
		rs.close();
		stmt.close();
		connection.close();
		for (String id : ids)
			importCategory(category, id);
	}

}
