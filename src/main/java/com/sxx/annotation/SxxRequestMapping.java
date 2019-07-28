package main.java.com.sxx.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-27 ����3:44:12 
 * ˵�� :
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SxxRequestMapping {
	String value() default "";
}
