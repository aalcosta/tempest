package tempest.hbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface HBColumn {

    String value() default "";
    String family() default "";

    HBFormatter formatter() default HBFormatter.NONE;
    int key() default -1;

}