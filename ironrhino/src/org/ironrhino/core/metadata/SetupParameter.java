package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SetupParameter {

	String label() default "";

	String defaultValue() default "";

	String placeholder() default "";

	boolean required() default true;

	int displayOrder() default 0;

}
