package org.ironrhino.core.monitor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

public class Value implements Serializable {

	private final AtomicLong longValue;

	private final AtomicLong doubleValue;

	public static final int PRECISION = 1000;

	// private double doubleValue;

	public Value(long c) {
		longValue = new AtomicLong(c);
		doubleValue = new AtomicLong(0);
	}

	public Value(double d) {
		longValue = new AtomicLong(0);
		doubleValue = new AtomicLong((long) (d * PRECISION));
	}

	public Value(long c, double d) {
		longValue = new AtomicLong(c);
		doubleValue = new AtomicLong((long) (d * PRECISION));
	}

	public Number[] add(long c, double d) {
		c = addLong(c);
		d = addDouble(d);
		return new Number[] { c, d };
	}

	public long addLong(long value) {
		return longValue.addAndGet(value);
	}

	public long getLong() {
		return longValue.get();
	}

	public double addDouble(double value) {
		return ((double) doubleValue.getAndAdd((long) (value * PRECISION)))
				/ PRECISION;
	}

	public double getDouble() {
		return ((double) doubleValue.get()) / PRECISION;
	}

	public String toString() {
		return String.valueOf(getLong()) + "," + String.valueOf(getDouble());
	}

	public static Value fromString(String s) {
		if (StringUtils.isBlank(s))
			return null;
		String[] array = s.split(",");
		return new Value(Long.valueOf(array[0]), Double.valueOf(array[1]));
	}

	public void cumulate(Value value) {
		this.addLong(value.getLong());
		this.addDouble(value.getDouble());
	}

}
