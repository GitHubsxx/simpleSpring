package main.java.com.sxx.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @author 作者 : sxx
 * @version 创建时间：2019-7-27 下午3:42:06 
 * 说明 :
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SxxService {
	String value() default "";
}
