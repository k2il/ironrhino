package org.ironrhino.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.Advised;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

public class ReflectionUtils {

	public static ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	public static Class<?> getGenericClass(Class<?> clazz) {
		return getGenericClass(clazz, 0);
	}

	public static Class<?> getGenericClass(Class<?> clazz, int index) {
		Type genType = clazz.getGenericSuperclass();
		if (genType instanceof ParameterizedType) {
			ParameterizedType pramType = (ParameterizedType) genType;
			Type[] params = pramType.getActualTypeArguments();
			if ((params != null) && (params.length > index))
				return params[index] instanceof Class ? (Class<?>) params[index]
						: null;
		}
		return null;
	}

	public static String[] getParameterNames(Constructor<?> ctor) {
		return parameterNameDiscoverer.getParameterNames(ctor);
	}

	public static String[] getParameterNames(Method method) {
		return parameterNameDiscoverer.getParameterNames(method);
	}

	public static String[] getParameterNames(JoinPoint jp) {
		if (!jp.getKind().equals(JoinPoint.METHOD_EXECUTION))
			return null;
		Class<?> clz = jp.getTarget().getClass();
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Method method;
		try {
			method = clz.getDeclaredMethod(sig.getName(),
					sig.getParameterTypes());
			String[] array = parameterNameDiscoverer.getParameterNames(method);
			if (array == null) {
				method = narrow(clz, method);
				if (method != null)
					array = parameterNameDiscoverer.getParameterNames(method);
			}
			return array;
		} catch (Exception e) {
			return null;
		}
	}

	public static Method narrow(Class<?> clz, Method method) {
		Method[] methods = clz.getMethods();
		loop: for (Method m : methods) {
			if (!m.getName().equals(method.getName()) || m.equals(method))
				continue;
			Class<?>[] p1 = method.getParameterTypes();
			Class<?>[] p2 = m.getParameterTypes();
			if (p1.length != p2.length)
				continue loop;
			for (int i = 0; i < p1.length; i++) {
				if (!p1[i].isAssignableFrom(p2[i]))
					continue loop;
			}
			return m;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object o, String name, Class<T> c) {
		try {
			Field f = o.getClass().getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(o);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void setFieldValue(Object o, String name, Object value) {
		try {
			Field f = o.getClass().getDeclaredField(name);
			f.setAccessible(true);
			f.set(o, value);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Object getTargetObject(Object proxy) {
		while (proxy instanceof Advised) {
			try {
				return getTargetObject(((Advised) proxy).getTargetSource()
						.getTarget());
			} catch (Exception e) {
				e.printStackTrace();
				return proxy;
			}
		}
		return proxy;
	}

}
