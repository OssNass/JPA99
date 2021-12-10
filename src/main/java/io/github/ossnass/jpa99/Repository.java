package io.github.ossnass.jpa99;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotat a class extending @{link JPARepository},
 * if that class is not annotated with this annotation, an error will be thrown.
 * 
 * The only value to be passed the ID of the repository 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
    String value();
}
