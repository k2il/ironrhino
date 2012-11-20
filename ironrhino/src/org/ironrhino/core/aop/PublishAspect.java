package org.ironrhino.core.aop;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.model.Persistable;

@Aspect
@Singleton
@Named
public class PublishAspect extends BaseAspect {

	public PublishAspect() {
		order = -1;
	}

	@Inject
	private EventPublisher eventPublisher;

	@AfterReturning(pointcut = "execution(java.util.List org.ironrhino.core.service.BaseManager.delete(*)) ", returning = "list")
	@SuppressWarnings("rawtypes")
	public void deleteBatch(List list) throws Throwable {
		if (!isBypass() && eventPublisher != null && list != null)
			for (Object entity : list) {
				PublishAware publishAware = entity.getClass().getAnnotation(
						PublishAware.class);
				if (publishAware != null)
					eventPublisher.publish(new EntityOperationEvent((Persistable)entity,
							EntityOperationType.DELETE), publishAware.global());
			}
	}

	@Around("execution(* org.ironrhino.core.service.BaseManager.save(*)) and args(entity) and @args(publishAware)")
	public Object save(ProceedingJoinPoint call, Persistable<?> entity,
			PublishAware publishAware) throws Throwable {
		boolean isNew = entity.isNew();
		Object result = call.proceed();
		if (!isBypass()) {
			if (eventPublisher != null)
				eventPublisher.publish(new EntityOperationEvent(entity,
						isNew ? EntityOperationType.CREATE
								: EntityOperationType.UPDATE), publishAware
						.global());
		}
		return result;
	}

	@AfterReturning("execution(* org.ironrhino.core.service.BaseManager.delete(*)) and args(entity) and @args(publishAware)")
	public void delete(Persistable<?> entity, PublishAware publishAware) {
		if (isBypass())
			return;
		if (eventPublisher != null)
			eventPublisher.publish(new EntityOperationEvent(entity,
					EntityOperationType.DELETE), publishAware.global());
	}

}
