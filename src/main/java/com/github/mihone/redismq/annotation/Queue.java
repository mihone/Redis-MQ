package com.github.mihone.redismq.annotation;

import java.lang.annotation.*;
/**
 * <p>Annotation used on the method to definite a listening method.
 * {@code value()} must be definited as queue name
*  @author mihone
*  @since  2019/10/6
*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Queue {
    String value();

}
