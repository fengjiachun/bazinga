package org.bazinga.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
public @interface RpcService {
	
	public String serviceName() default "";
	
	public int weight() default 5;
	
	public String appName() default "";
	
	public String responsibilityName() default "";
	

}
