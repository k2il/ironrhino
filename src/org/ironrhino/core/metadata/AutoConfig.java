package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoConfig {

	String namespace() default "";

	String actionName() default "";

	String fileupload() default "";

	boolean readonly() default false;

	boolean searchable() default false;
	
	String order() default "";

}
