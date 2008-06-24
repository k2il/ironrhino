package org.ironrhino.core.aspect;

import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.common.model.Record;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.model.Entity;
import org.springframework.core.Ordered;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.security.userdetails.UserDetails;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.Recordable
 */
@Aspect
public class Recording extends HibernateDaoSupport implements Ordered {

	private int order;

	@Around("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(org.ironrhino.core.annotation.Recordable)")
	public void save(ProceedingJoinPoint call, Entity entity) throws Throwable {
		boolean isNew = entity.isNew();
		try {
			call.proceed();
			record(entity, isNew ? "CREATE" : "UPDATE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(org.ironrhino.core.annotation.Recordable)")
	public void delete(Entity entity) {
		record(entity, "DELETE");
	}

	private void record(Entity entity, String action) {
		final Record record = new Record();
		UserDetails ud = AuthzUtils.getUserDetails(UserDetails.class);
		if (ud != null) {
			record.setOperatorId(ud.getUsername());
			record.setOperatorClass(ud.getClass().getName());
		}
		try {
			record.setEntityId(String.valueOf(BeanUtils.getProperty(entity,
					"id")));
		} catch (Exception e) {
		}
		record.setEntityClass(entity.getClass().getName());
		record.setEntityToString(String.valueOf(entity));
		record.setAction(action);
		record.setRecordDate(new Date());
		getHibernateTemplate().save(record);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
