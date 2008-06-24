package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.opensymphony.xwork2.Action;

@Target(METHOD)
@Retention(RUNTIME)
public @interface PostMethod {

	String methodName() default Action.INPUT;

	String resultCode() default "";

}
