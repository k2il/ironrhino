package org.ironrhino.core.hibernate;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.ironrhino.core.util.CodecUtils;

public class StringIdGenerator implements IdentifierGenerator {

	public Serializable generate(SessionImplementor session, Object obj)
			throws HibernateException {
		return CodecUtils.nextId();
	}

}
