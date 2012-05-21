package org.ironrhino.core.service;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.model.Persistable;

@Singleton
@Named("entityManager")
public class EntityManagerImpl<T extends Persistable<?>> extends
		BaseManagerImpl<T> implements EntityManager<T> {

	private ThreadLocal<Class<T>> entityClassHolder = new ThreadLocal<Class<T>>();

	public void setEntityClass(Class<T> clazz) {
		entityClassHolder.set(clazz);
	}

	public Class<? extends Persistable<?>> getEntityClass() {
		return entityClassHolder.get();
	}

}
