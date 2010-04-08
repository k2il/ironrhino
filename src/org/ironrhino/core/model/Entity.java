package org.ironrhino.core.model;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.util.AnnotationUtils;

public abstract class Entity<PK extends Serializable> implements
		Persistable<PK> {

	private static final long serialVersionUID = 5366738895214161098L;

	@Override
	public int hashCode() {
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(this, NaturalId.class);
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.getId());
		for (Object value : map.values())
			builder.append(value);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (!(this.getClass().equals(object.getClass())))
			return false;
		Entity that = (Entity) object;
		return this.toIdentifiedString().equals(that.toIdentifiedString());
	}

	private String toIdentifiedString() {
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(this, NaturalId.class);
		return getClass().getName() + "{id=" + getId() + ",naturalId="
				+ map.toString() + "}";
	}

	@Override
	public String toString() {
		return toIdentifiedString();
	}

}
