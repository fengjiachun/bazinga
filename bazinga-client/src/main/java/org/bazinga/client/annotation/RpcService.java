package org.bazinga.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RpcService {
	
	public String serviceName() default "";
	
	public int weight() default 50;
	
	public String appName() default "bazinga";
	
	public String responsibilityName() default "system";
	
	public int connCount() default 1;
	

}
