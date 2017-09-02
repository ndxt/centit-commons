package com.centit.support.database.orm;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by codefan on 17-8-30.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Lazy {
}
