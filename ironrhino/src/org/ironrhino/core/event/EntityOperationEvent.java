package org.ironrhino.core.event;

import org.ironrhino.core.model.Entity;
import org.springframework.context.ApplicationEvent;


public class EntityOperationEvent extends ApplicationEvent {

	private Entity entity;

	private EntityOperationType type;

	public EntityOperationEvent(Entity entity, EntityOperationType type) {
		super(entity);
		this.entity = entity;
		this.type = type;
	}

	public Entity getEntity() {
		return entity;
	}

	public EntityOperationType getType() {
		return type;
	}

}
