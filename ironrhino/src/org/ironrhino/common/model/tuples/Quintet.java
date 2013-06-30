package org.ironrhino.common.model.tuples;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Quintet<A, B, C, D, E> implements Serializable {

	private static final long serialVersionUID = 1820545026759463994L;

	private A a;

	private B b;

	private C c;

	private D d;

	private E e;

	public Quintet() {

	}

	public Quintet(A a, B b, C c, D d, E e) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

	public C getC() {
		return c;
	}

	public void setC(C c) {
		this.c = c;
	}

	public D getD() {
		return d;
	}

	public void setD(D d) {
		this.d = d;
	}

	public E getE() {
		return e;
	}

	public void setE(E e) {
		this.e = e;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that, false);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}